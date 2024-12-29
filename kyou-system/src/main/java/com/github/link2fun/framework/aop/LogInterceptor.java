package com.github.link2fun.framework.aop;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.thread.threadlocal.NamedThreadLocal;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.core.exception.EasyQueryException;
import com.easy.query.core.exception.EasyQuerySQLCommandException;
import com.easy.query.core.exception.EasyQuerySQLStatementException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.link2fun.framework.manager.AsyncManager;
import com.github.link2fun.framework.manager.factory.AsyncFactory;
import com.github.link2fun.framework.tool.LogTool;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.R;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.enums.BusinessStatus;
import com.github.link2fun.support.enums.HttpMethod;
import com.github.link2fun.support.utils.SecurityUtils;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.support.utils.ip.IpUtils;
import com.github.link2fun.system.modular.operlog.model.SysOperLog;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.Result;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.core.wrap.MethodHolder;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class LogInterceptor implements Interceptor {


  /**
   * 排除敏感属性字段
   */
  public static final String[] EXCLUDE_PROPERTIES = {"password", "oldPassword", "newPassword", "confirmPassword"};

  /**
   * 计算操作消耗时间
   */
  private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<>("Cost Time");


  /** 定义jackson对象 */
  static final FilterProvider filters = new SimpleFilterProvider()
    .addFilter("defaultFilter", SimpleBeanPropertyFilter.serializeAllExcept(EXCLUDE_PROPERTIES));

  public static final ObjectMapper objectMapper = new ObjectMapper().setFilterProvider(filters);


  // 创建一个LoadingCache来缓存ObjectMapper实例
  private static final LoadingCache<Set<String>, ObjectMapper> OBJECT_MAPPER_CACHE = Caffeine.newBuilder()
    .build((excludeParamNames) -> objectMapper.copy()
      .setFilterProvider(new SimpleFilterProvider().addFilter("customFilter", SimpleBeanPropertyFilter.serializeAllExcept(excludeParamNames)))
    );


  /** toJsonStr */
  private static String toJsonStr(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * 拦截
   *
   * @param inv 调用者
   */
  @Override
  public Object doIntercept(Invocation inv) throws Throwable {

    TIME_THREADLOCAL.set(System.currentTimeMillis());

    Context current = Context.current();

    MethodHolder methodHolder = inv.method();
    final Method method = methodHolder.getMethod();
    final Log apiLog = method.getAnnotation(Log.class);

    String methodName = method.getName();
    Class<?> controllerClazz = method.getDeclaringClass();
    Integer methodLineNumber = LogTool.getMethodLineNumberFromCache(method);

    StringBuffer logMsg = new StringBuffer();
    // METHOD
    logMsg.append("METHOD\t: ").append("(").append(controllerClazz.getSimpleName()).append(".java:")
      .append(methodLineNumber).append(") ").append(controllerClazz.getSimpleName()).append(".").append(methodName)
      .append(System.lineSeparator());
    // URL
    logMsg.append("URL\t: ").append(current.method()).append(" ").append(current.path()).append(System.lineSeparator());
    // PARAMS
    logMsg.append("PARAMS\t: ").append(toJsonStr(current.paramMap().toValuesMap())).append(System.lineSeparator());
    // BODY
    logMsg.append("BODY\t: ").append(current.body()).append(System.lineSeparator());

    Object invokeResult;
    try {
      invokeResult = inv.invoke();
      if ((invokeResult instanceof Result<?> || invokeResult instanceof AjaxResult || invokeResult instanceof R<?>) && !StrUtil.equals(current.method(), "GET")) {
        // 如果是常见的 Result 类型，且不是 GET 请求，打印响应
        logMsg.append("RESPONSE\t: ").append(objectMapper.writeValueAsString(invokeResult)).append(System.lineSeparator());
      }
      // 到这里是正常结束的
      handleLog(inv, apiLog, null, invokeResult);

      // 记录一下耗时
      logMsg.append("COST TIME\t: ").append(System.currentTimeMillis() - TIME_THREADLOCAL.get()).append("ms").append(System.lineSeparator());

      ThreadUtil.sleep(1000);
      return invokeResult;
    }  catch (Exception throwable) {
      // 到这里是异常结束的
      handleLog(inv, apiLog, throwable, null);
      throw throwable;
    } finally {
      log.info("{}", logMsg);
      TIME_THREADLOCAL.remove();
    }
  }


  protected void handleLog(final Invocation invocation, Log controllerLog, final Exception e, Object jsonResult) {

    // 如果 @Mapping 的方法上没有加上 @Log 注解，则不记录数据库日志
    if (Objects.isNull(controllerLog)) {
      return;
    }
    try {
      final Context context = Context.current();
      final Method method = invocation.method().getMethod();
      // 获取当前的用户
      SessionUser currentUser = SecurityUtils.currentUser();

      // *========数据库日志=========*//
      SysOperLog operLog = new SysOperLog();
      operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
      // 请求的地址
      String ip = IpUtils.getIpAddr();
      operLog.setOperIp(ip);
      operLog.setOperUrl(StringUtils.substring(context.path(), 0, 255));
      if (currentUser != null) {
        operLog.setOperName(currentUser.getUsername());
        SysUserDTO userDTO = currentUser.getUser();
        if (StringUtils.isNotNull(userDTO) && StringUtils.isNotNull(userDTO.getDept())) {
          operLog.setDeptName(userDTO.getDept().getDeptName());
        }
      }

      if (e != null) {
        operLog.setStatus(BusinessStatus.FAIL.ordinal());
        operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
      }
      // 设置方法名称
      String className = method.getDeclaringClass().getName();
      String methodName = method.getName();
      operLog.setMethod(className + "." + methodName + "()");
      // 设置请求方式
      operLog.setRequestMethod(Context.current().method());
      // 处理设置注解上的参数
      getControllerMethodDescription(invocation, controllerLog, operLog, jsonResult);
      operLog.setOperTime(LocalDateTimeUtil.of(new Date(TIME_THREADLOCAL.get())));
      // 设置消耗时间
      operLog.setCostTime(System.currentTimeMillis() - TIME_THREADLOCAL.get());
      // 保存数据库
      AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
    } catch (Exception exp) {
      // 记录本地异常日志
      log.error("异常信息:{}", exp.getMessage(), e);
    }
  }

  /**
   * 获取注解中对方法的描述信息 用于Controller层注解
   *
   * @param log     日志
   * @param operLog 操作日志
   */
  public void getControllerMethodDescription(Invocation joinPoint, Log log, SysOperLog operLog, Object jsonResult) {
    // 设置action动作
    operLog.setBusinessType(log.businessType().ordinal());
    // 设置标题
    operLog.setTitle(log.title());
    // 设置操作人类别
    operLog.setOperatorType(log.operatorType().ordinal());
    // 是否需要保存request，参数和值
    if (log.isSaveRequestData()) {
      // 获取参数的信息，传入到数据库中。
      setRequestValue(joinPoint, operLog, log.excludeParamNames());
    }
    // 是否需要保存response，参数和值
    if (log.isSaveResponseData() && StringUtils.isNotNull(jsonResult)) {
      operLog.setJsonResult(StringUtils.substring(toJsonStr(jsonResult), 0, 2000));
    }
  }

  /**
   * 获取请求的参数，放到log中
   *
   * @param operLog 操作日志
   */
  private void setRequestValue(Invocation inv, SysOperLog operLog, String[] excludeParamNames) {
    Map<?, ?> paramsMap = Context.current().paramMap().toValuesMap();
    String requestMethod = operLog.getRequestMethod();
    if (StringUtils.isEmpty(paramsMap)
      && (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod))) {
      String params = argsArrayToString(inv.args(), excludeParamNames);
      operLog.setOperParam(StringUtils.substring(params, 0, 2000));
    } else {
      operLog.setOperParam(StringUtils.substring(toJsonStr(paramsMap), 0, 2000));
    }
  }

  /**
   * 参数拼装
   */
  private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
    if (paramsArray == null) {
      return StrUtil.EMPTY;
    }

    StringBuilder params = new StringBuilder();
    for (Object o : paramsArray) {

      if (!StringUtils.isNotNull(o) || isFilterObject(o)) {
        continue;
      }

      try {
        // json序列化需要临时添加 filter
        String jsonObj;
        if (ArrayUtil.isNotEmpty(excludeParamNames)) {
          // 说明有要忽略的, 使用自定义的 objectMapper
          final ObjectMapper customFilterObjectMapper = getCustomFilterObjectMapper(excludeParamNames);
          jsonObj = customFilterObjectMapper
            .writeValueAsString(o);
        } else {
          jsonObj = toJsonStr(o);
        }
        params.append(jsonObj).append(" ");
      } catch (Exception ignored) {
      }
    }
    return params.toString().trim();
  }

  /**
   * 获取自定义过滤的ObjectMapper实例
   *
   * @param excludeParamNames 要排除的参数名数组
   * @return 自定义过滤的ObjectMapper实例
   */
  private ObjectMapper getCustomFilterObjectMapper(final String[] excludeParamNames) {

    return OBJECT_MAPPER_CACHE.get(Stream.of(excludeParamNames).collect(Collectors.toSet()));
  }


  /**
   * 判断是否需要过滤的对象。
   *
   * @param o 对象信息。
   * @return 如果是需要过滤的对象，则返回true；否则返回false。
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
