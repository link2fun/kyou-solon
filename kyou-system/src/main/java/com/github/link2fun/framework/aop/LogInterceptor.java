package com.github.link2fun.framework.aop;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.threadlocal.NamedThreadLocal;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.link2fun.framework.auth.AuditSink;
import com.github.link2fun.framework.tool.LogTool;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.enums.BusinessStatus;
import com.github.link2fun.support.enums.HttpMethod;
import com.github.link2fun.support.utils.ip.IpUtils;
import com.github.link2fun.system.modular.operlog.model.SysOperLog;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.core.wrap.MethodHolder;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

@Slf4j
public class LogInterceptor implements Interceptor {

  /** 序列化失败的统一占位输出：能区分「空对象」和「序列化失败」，日志失败绝不影响业务 */
  static final String UNSERIALIZABLE = "[unserializable]";

  /** 入日志的集合/Map 最多保留的元素个数，超出先截断再序列化（防大报文全量序列化后被 sub(2000) 丢弃） */
  private static final int MAX_LOG_ITEMS = 100;

  /** 入日志的字符串最大长度，超出先截断再序列化 */
  private static final int MAX_LOG_STRING_LEN = 4096;

  /** 截断深度上限：只走 Map/集合这类可廉价计量的结构，不反射进 Bean 字段（防循环引用） */
  private static final int MAX_TRUNCATE_DEPTH = 3;

  private static final String TRUNCATED = "…(已截断)";

  /** 路径中长度达到该值的单段视为会话凭据：sa-token 的 uuid 风格 token 为 32 位，24 是留余量的下界 */
  private static final int PATH_TOKEN_MIN_LEN = 24;

  /** 路径里的凭据保留的首尾位数：够核对是哪一次会话，又不足以重放 */
  private static final int PATH_KEEP_PREFIX = 4;
  private static final int PATH_KEEP_SUFFIX = 4;

  /**
   * 路径里的长字符段（{@code /monitor/online/{tokenId}} 之类）。
   *
   * <p>会话 token 是「持有即可用」的凭据，落进 {@code oper_url} 等于把可重放的密钥写进审计表，
   * 而 {@code @Log(mask=true)} 只作用于参数与响应体，管不到 URL。
   */
  private static final Pattern PATH_SECRET = Pattern.compile("(?<=/)[^/?#]{" + PATH_TOKEN_MIN_LEN + ",}(?=/|$)");


  /**
   * 计算操作消耗时间
   */
  private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<>("Cost Time");

  /** 审计写入端；由 KyouApp 在 AuditSink bean 注册后注入 */
  private AuditSink auditSink;

  /**
   * 日志用的对象映射器。默认与 API 响应同一份（Solon 的 {@code JacksonStringSerializer}），
   * 日期格式、Long 转换等与接口输出保持一致；未注入时退回独立实例（单元测试用）。
   */
  private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  /** 由 KyouApp 在 AuditSink bean 注册后注入 */
  public void setAuditSink(AuditSink auditSink) {
    this.auditSink = auditSink;
  }

  /**
   * 注入全局映射器。由 KyouApp 在 Solon 序列化器初始化完成后调用，
   * 使日志里的日期、Long 呈现与 API 响应逐字节一致。
   */
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /** 当前日志使用的映射器（测试与诊断用） */
  ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  /**
   * 序列化并脱敏（{@code @Log(mask=true)} 的方法专用）。
   *
   * <p>先序列化成 JSON 树，再对<b>实际序列化出的内容</b>逐字段判定——Map 的 key、
   * 运行时子类多出的字段、自定义序列化器的输出都在覆盖范围内，不存在
   * 「声明类型干净就跳过脱敏」的旁路。序列化失败统一返回 {@link #UNSERIALIZABLE}。
   */
  String toMaskedJson(Object obj, Set<String> excludeNames) {
    return toTreeJson(obj, LogMasker::mask, excludeNames);
  }

  /**
   * 序列化并整体剔除指定字段（不脱敏）。{@code @Log(mask=false)} 但声明了
   * {@code excludeParamNames} 的方法使用。
   */
  private String toFilteredJson(Object obj, Set<String> excludeNames) {
    return toTreeJson(obj, LogMasker::removeNames, excludeNames);
  }

  /** 共享的「建树 → 变换 → 回写」管线；null 写 "null"，失败统一 {@link #UNSERIALIZABLE} */
  private String toTreeJson(Object obj, BiConsumer<JsonNode, Set<String>> transformer, Set<String> excludeNames) {
    if (obj == null) {
      return "null";
    }
    try {
      JsonNode node = objectMapper.valueToTree(truncateForLog(obj));
      transformer.accept(node, excludeNames);
      return objectMapper.writeValueAsString(node);
    } catch (Exception ignored) {
      return UNSERIALIZABLE;
    }
  }

  /** 序列化入日志；失败统一返回 {@link #UNSERIALIZABLE}，日志失败绝不影响业务 */
  private String toJsonQuietly(Object obj) {
    try {
      return objectMapper.writeValueAsString(truncateForLog(obj));
    } catch (Exception ignored) {
      return UNSERIALIZABLE;
    }
  }

  /**
   * 按方法注解序列化入参/响应：{@code mask=true} 脱敏，否则原样输出
   * （声明了 {@code excludeParamNames} 时仅剔除指定字段）。
   */
  private String serializeForLog(Object value, Log apiLog) {
    Set<String> excludeNames = excludeNamesOf(apiLog);
    if (apiLog.mask()) {
      return toMaskedJson(value, excludeNames);
    }
    if (!excludeNames.isEmpty()) {
      return toFilteredJson(value, excludeNames);
    }
    return toJsonQuietly(value);
  }

  /** 注解里的排除名单转 Set；空数组短路，绝大多数方法在这里直接返回空集合 */
  private static Set<String> excludeNamesOf(Log apiLog) {
    String[] names = apiLog.excludeParamNames();
    if (ArrayUtil.isEmpty(names)) {
      return Collections.emptySet();
    }
    // 不能用 Set.of(...)：注解里写了重复名字会抛 IllegalArgumentException
    return new HashSet<>(Arrays.asList(names));
  }

  /**
   * 日志序列化前的体积预截断：超长字符串、超大集合/Map 先裁到上限再序列化，
   * 避免「全量序列化后再 sub(2000)」的结构性浪费。
   *
   * <p>只处理字符串与集合/Map 结构（能廉价计量体积的部分），不反射进入 Bean 字段。
   */
  static Object truncateForLog(Object value) {
    return truncate(value, 0);
  }

  private static Object truncate(Object value, int depth) {
    if (value == null || depth > MAX_TRUNCATE_DEPTH) {
      return value;
    }
    if (value instanceof String text) {
      return text.length() > MAX_LOG_STRING_LEN
        ? text.substring(0, MAX_LOG_STRING_LEN) + TRUNCATED
        : text;
    }
    if (value instanceof Collection<?> collection) {
      List<Object> result = new ArrayList<>(Math.min(collection.size(), MAX_LOG_ITEMS + 1));
      int index = 0;
      for (Object item : collection) {
        if (index++ == MAX_LOG_ITEMS) {
          result.add("…(共 " + collection.size() + " 项" + TRUNCATED + ")");
          return result;
        }
        result.add(truncate(item, depth + 1));
      }
      return result;
    }
    if (value instanceof Map<?, ?> map) {
      Map<Object, Object> result = new LinkedHashMap<>(Math.min(map.size(), MAX_LOG_ITEMS + 1));
      int index = 0;
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (index++ == MAX_LOG_ITEMS) {
          result.put("(其余字段" + TRUNCATED + ")", "…(共 " + map.size() + " 项)");
          return result;
        }
        result.put(entry.getKey(), truncate(entry.getValue(), depth + 1));
      }
      return result;
    }
    if (value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive()) {
      Object[] array = (Object[]) value;
      if (array.length <= MAX_LOG_ITEMS) {
        // 小数组原样返回；元素不做深层截断（数组入参罕见，不值得为它复制）
        return value;
      }
      List<Object> result = new ArrayList<>(MAX_LOG_ITEMS + 1);
      result.addAll(Arrays.asList(array).subList(0, MAX_LOG_ITEMS));
      result.add("…(共 " + array.length + " 项" + TRUNCATED + ")");
      return result;
    }
    return value;
  }


  /**
   * 拦截
   *
   * @param inv 调用者
   */
  @Override
  public Object doIntercept(Invocation inv) throws Throwable {

    MethodHolder methodHolder = inv.method();
    final Method method = methodHolder.getMethod();
    final Log apiLog = method.getAnnotation(Log.class);

    // 无 @Log 的方法不产生请求日志（控制台与入库都不做），直接放行
    if (apiLog == null) {
      return inv.invoke();
    }

    TIME_THREADLOCAL.set(System.currentTimeMillis());

    // 控制台日志关掉时，下面的拼装（含一次 JSON 序列化）全是白干
    final boolean infoEnabled = log.isInfoEnabled();
    StringBuffer logMsg = infoEnabled ? safeBuildRequestLog(method, apiLog) : null;

    Object invokeResult = null;
    // Throwable 而非 Exception：StackOverflowError（树构建成环）、OutOfMemoryError 这些
    // 「最需要日志」的场景恰恰不是 Exception，用 Exception 会让审计行与控制台日志同时消失
    Throwable failure = null;
    try {
      invokeResult = inv.invoke();
      return invokeResult;
    } catch (Throwable throwable) {
      failure = throwable;
      throw throwable;
    } finally {
      // 日志收口全部在业务 try 之外：日志失败绝不改写业务结果
      try {
        if (infoEnabled) {
          logMsg.append("COST TIME\t: ").append(System.currentTimeMillis() - TIME_THREADLOCAL.get())
            .append("ms").append(System.lineSeparator());
        }
        handleLog(inv, apiLog, failure, invokeResult);
      } finally {
        if (infoEnabled) {
          log.info("{}", logMsg);
        }
        TIME_THREADLOCAL.remove();
      }
    }
  }

  /**
   * 控制台日志拼装失败不能影响业务：异常时退化为「方法名 + 失败标记」。
   *
   * <p>必须在这里兜住——{@link #buildRequestLog} 在业务 try 之外，抛出的异常会直接冒泡成 500，
   * 且 {@link #TIME_THREADLOCAL} 不会被 {@code finally} 清理，线程复用时下一个请求的耗时统计就错了。
   */
  private StringBuffer safeBuildRequestLog(Method method, Log apiLog) {
    try {
      return buildRequestLog(method, apiLog);
    } catch (Throwable t) {
      log.debug("请求日志拼装失败, 已降级: {}", t.toString());
      return new StringBuffer(64)
        .append("METHOD\t: ").append(method.getDeclaringClass().getSimpleName()).append(".").append(method.getName())
        .append(" [request log build failed]").append(System.lineSeparator());
    }
  }

  /**
   * 把路径里的会话凭据打码，其余部分保持原样。
   *
   * <p>只按「单段长度」判定：数字 ID、{@code changeStatus} 这类短段不受影响，
   * 而 sa-token 的 32 位 uuid、JWT 都会被打码。保留前 4 后 4 位，便于按日志核对
   * 「是哪一次会话」；token 本体仍不可还原。
   */
  static String maskPathSecrets(String path) {
    if (StrUtil.isEmpty(path)) {
      return path;
    }
    return PATH_SECRET.matcher(path).replaceAll(match -> {
      String secret = match.group();
      return secret.length() <= PATH_KEEP_PREFIX + PATH_KEEP_SUFFIX
        ? LogMasker.MASK
        : StrUtil.hide(secret, PATH_KEEP_PREFIX, secret.length() - PATH_KEEP_SUFFIX);
    });
  }

  /**
   * 审计里的错误摘要。
   *
   * <p>{@code StackOverflowError} 的 {@code getMessage()} 是 {@code null}，
   * 直接写库会让「最需要日志的场景」只留一行空白，因此退化为异常类名。
   */
  static String errorMessageOf(Throwable e) {
    String message = e.getMessage();
    return StrUtil.isBlank(message) ? e.getClass().getSimpleName() : message;
  }

  /** 控制台请求日志的 METHOD/URL/PARAMS 三行 */
  StringBuffer buildRequestLog(Method method, Log apiLog) {
    Context current = Context.current();
    Class<?> controllerClazz = method.getDeclaringClass();
    Integer methodLineNumber = LogTool.getMethodLineNumberFromCache(method);

    StringBuffer logMsg = new StringBuffer(256);
    // METHOD
    logMsg.append("METHOD\t: ").append("(").append(controllerClazz.getSimpleName()).append(".java:")
      .append(methodLineNumber).append(") ").append(controllerClazz.getSimpleName()).append(".").append(method.getName())
      .append(System.lineSeparator());
    // URL：与入库 oper_url 同口径，路径里的会话凭据同样打码（控制台日志也会被采集/转发）
    logMsg.append("URL\t: ").append(current.method()).append(" ").append(maskPathSecrets(current.path()))
      .append(System.lineSeparator());
    // PARAMS
    logMsg.append("PARAMS\t: ").append(serializeForLog(current.paramMap().toValuesMap(), apiLog))
      .append(System.lineSeparator());
    return logMsg;
  }


  private void handleLog(final Invocation invocation, Log apiLog, final Throwable e, Object jsonResult) {

    // 无 @Log 注解的方法不记录数据库日志（doIntercept 已提前放行，这里防御）
    if (apiLog == null) {
      return;
    }
    try {
      final Context context = Context.current();
      final Method method = invocation.method().getMethod();
      // 获取当前的用户
      SessionUser currentUser = ActionContext.current().getSessionUser();

      // *========数据库日志=========*//
      SysOperLog operLog = new SysOperLog();
      operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
      // 请求的地址
      String ip = IpUtils.getIpAddr();
      operLog.setOperIp(ip);
      operLog.setOperUrl(StrUtil.sub(maskPathSecrets(context.path()), 0, 255));
      if (currentUser != null) {
        operLog.setOperName(currentUser.getUsername());
        SysUserDTO userDTO = currentUser.getUser();
        if (ObjectUtil.isNotNull(userDTO) && ObjectUtil.isNotNull(userDTO.getDept())) {
          operLog.setDeptName(userDTO.getDept().getDeptName());
        }
      }

      if (e != null) {
        operLog.setStatus(BusinessStatus.FAIL.ordinal());
        operLog.setErrorMsg(StrUtil.sub(errorMessageOf(e), 0, 2000));
      }
      // 设置方法名称
      String className = method.getDeclaringClass().getName();
      String methodName = method.getName();
      operLog.setMethod(className + "." + methodName + "()");
      // 设置请求方式
      operLog.setRequestMethod(Context.current().method());
      // 处理设置注解上的参数
      getControllerMethodDescription(invocation, apiLog, operLog, jsonResult);
      operLog.setOperTime(LocalDateTimeUtil.of(new Date(TIME_THREADLOCAL.get())));
      // 设置消耗时间
      operLog.setCostTime(System.currentTimeMillis() - TIME_THREADLOCAL.get());
      // 保存数据库
      if (auditSink != null) {
        auditSink.operEvent(operLog);
      }
    } catch (Throwable exp) {
      // 记录本地异常日志（含 Error：审计写不进去时至少控制台要留下痕迹）
      log.error("异常信息:{}", exp.getMessage(), exp);
    }
  }

  /**
   * 获取注解中对方法的描述信息 用于Controller层注解
   *
   * @param operLog 操作日志
   */
  private void getControllerMethodDescription(Invocation joinPoint, Log apiLog, SysOperLog operLog, Object jsonResult) {
    // 设置action动作
    operLog.setBusinessType(apiLog.businessType().ordinal());
    // 设置标题
    operLog.setTitle(apiLog.title());
    // 设置操作人类别
    operLog.setOperatorType(apiLog.operatorType().ordinal());
    // 是否需要保存request，参数和值
    if (apiLog.isSaveRequestData()) {
      // 获取参数的信息，传入到数据库中。
      setRequestValue(joinPoint, operLog, apiLog);
    }
    // 是否需要保存response，参数和值（响应体与请求同口径：mask=true 才脱敏）
    if (apiLog.isSaveResponseData() && ObjectUtil.isNotNull(jsonResult)) {
      operLog.setJsonResult(StrUtil.sub(serializeForLog(jsonResult, apiLog), 0, 2000));
    }
  }

  /**
   * 获取请求的参数，放到log中
   * <p>
   * 两条来源同口径处理：POST/PUT 的 JSON body 走 {@code inv.args()}，
   * 其余（含 query string / form）走 {@code paramMap}。
   *
   * @param operLog 操作日志
   */
  private void setRequestValue(Invocation inv, SysOperLog operLog, Log apiLog) {
    Map<?, ?> paramsMap = Context.current().paramMap().toValuesMap();
    String requestMethod = operLog.getRequestMethod();

    String params;
    if (MapUtil.isEmpty(paramsMap)
      && (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod))) {
      params = argsArrayToString(inv.args(), apiLog);
    } else {
      params = serializeForLog(paramsMap, apiLog);
    }
    operLog.setOperParam(StrUtil.sub(params, 0, 2000));
  }

  /**
   * 参数拼装
   */
  private String argsArrayToString(Object[] paramsArray, Log apiLog) {
    if (paramsArray == null) {
      return StrUtil.EMPTY;
    }

    StringBuilder params = new StringBuilder();
    for (Object o : paramsArray) {
      if (!ObjectUtil.isNotNull(o) || isFilterObject(o)) {
        continue;
      }
      String jsonObj = serializeForLog(o, apiLog);
      if (StrUtil.isNotEmpty(jsonObj)) {
        params.append(jsonObj).append(" ");
      }
    }
    return params.toString().trim();
  }

  /**
   * 判断是否需要过滤的对象。
   *
   * @param o 对象信息。
   * @return 如果是需要过滤的对象，则返回true，则说明不需要记录。
   */
  @SuppressWarnings("rawtypes")
  public boolean isFilterObject(final Object o) {
    Class<?> clazz = o.getClass();
    if (clazz.isArray()) {
      return clazz.getComponentType().isAssignableFrom(UploadedFile.class);
    } else if (Collection.class.isAssignableFrom(clazz)) {
      Collection collection = (Collection) o;
      for (Object value : collection) {
        return value instanceof UploadedFile;
      }
    } else if (Map.class.isAssignableFrom(clazz)) {
      Map map = (Map) o;
      for (Object value : map.entrySet()) {
        Map.Entry entry = (Map.Entry) value;
        return entry.getValue() instanceof UploadedFile;
      }
    }
    return o instanceof DownloadedFile || o instanceof Context;
  }
}
