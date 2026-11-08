package com.github.link2fun.schedule.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.schedule.constant.ScheduleConstants;
import com.github.link2fun.schedule.modular.job.model.SysJob;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.exception.job.TaskException;
import org.noear.solon.Solon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 任务执行工具
 *
 * @author ruoyi
 */
public class JobInvokeUtil {
  /** 方法级白名单（bean名#方法名），执行前校验，防止直接修改数据库绕过入口校验 */
  private static final Set<String> METHOD_WHITELIST = Set.of(
    "ryTask#ryMultipleParams",
    "ryTask#ryParams",
    "ryTask#ryNoParams"
  );

  /**
   * 执行方法
   *
   * @param sysJob 系统任务
   */
  public static void invokeMethod(SysJob sysJob) throws Exception {
    String invokeTarget = sysJob.getInvokeTarget();
    final String violation = violation(invokeTarget);
    if (violation != null) {
      throw new TaskException(violation, TaskException.Code.CONFIG_ERROR);
    }
    String beanName = getBeanName(invokeTarget);
    String methodName = getMethodName(invokeTarget);
    List<Object[]> methodParams = getMethodParams(invokeTarget);

    Object bean = Solon.context().getBean(beanName);
    if (ObjectUtil.isNull(bean)) {
      throw new TaskException("调用目标 bean 不存在：" + beanName, TaskException.Code.CONFIG_ERROR);
    }
    invokeMethod(bean, methodName, methodParams);
  }

  /** 判定调用目标是否违规: 违规返回原因文案, 通过返回 null; 保存期与运行期共用, 避免「存得下、跑不了」 */
  public static String violation(final String invokeTarget) {
    if (StrUtil.isBlank(invokeTarget)) {
      return "调用目标不能为空";
    }
    if (StrUtil.containsIgnoreCase(invokeTarget, ScheduleConstants.LOOKUP_RMI)) {
      return "目标字符串不允许'rmi'调用";
    }
    if (StrUtil.containsAnyIgnoreCase(invokeTarget, ScheduleConstants.LOOKUP_LDAP, ScheduleConstants.LOOKUP_LDAPS)) {
      return "目标字符串不允许'ldap(s)'调用";
    }
    if (StrUtil.containsAnyIgnoreCase(invokeTarget, Constants.HTTP, Constants.HTTPS)) {
      return "目标字符串不允许'http(s)'调用";
    }
    if (StrUtil.containsAnyIgnoreCase(invokeTarget, ScheduleConstants.JOB_ERROR_STR)) {
      return "目标字符串存在违规";
    }
    final String beanName = getBeanName(invokeTarget);
    if (isValidClassName(beanName)) {
      return "不支持类名形式的调用目标：" + invokeTarget;
    }
    if (!METHOD_WHITELIST.contains(beanName + "#" + getMethodName(invokeTarget))) {
      return "调用目标不在方法白名单内：" + invokeTarget;
    }
    return null;
  }

  /**
   * 调用任务方法
   *
   * @param bean         目标对象
   * @param methodName   方法名称
   * @param methodParams 方法参数
   */
  private static void invokeMethod(Object bean, String methodName, List<Object[]> methodParams)
    throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
    if (ObjectUtil.isNotNull(methodParams) && !methodParams.isEmpty()) {
      Method method = bean.getClass().getMethod(methodName, getMethodParamsType(methodParams));
      method.invoke(bean, getMethodParamsValue(methodParams));
    } else {
      Method method = bean.getClass().getMethod(methodName);
      method.invoke(bean);
    }
  }

  /**
   * 校验是否为为class包名
   *
   * @param invokeTarget 名称
   * @return true是 false否
   */
  public static boolean isValidClassName(String invokeTarget) {
    return StrUtil.count(invokeTarget, ".") > 1;
  }

  /**
   * 获取bean名称
   *
   * @param invokeTarget 目标字符串
   * @return bean名称
   */
  public static String getBeanName(String invokeTarget) {
    String beanName = StrUtil.subBefore(invokeTarget, "(", false);
    return StrUtil.subBefore(beanName, ".", true);
  }

  /**
   * 获取bean方法
   *
   * @param invokeTarget 目标字符串
   * @return method方法
   */
  public static String getMethodName(String invokeTarget) {
    String methodName = StrUtil.subBefore(invokeTarget, "(", false);
    return StrUtil.subAfter(methodName, ".", true);
  }

  /**
   * 获取method方法参数相关列表
   *
   * @param invokeTarget 目标字符串
   * @return method方法相关参数列表
   */
  public static List<Object[]> getMethodParams(String invokeTarget) {
    String methodStr = StrUtil.subBetween(invokeTarget, "(", ")");
    if (StrUtil.isBlank(methodStr)) {
      return null;
    }
    String[] methodParams = methodStr.split(",(?=([^\"']*[\"'][^\"']*[\"'])*[^\"']*$)");
    List<Object[]> classs = new LinkedList<>();
    for (int i = 0; i < methodParams.length; i++) {
      String str = StrUtil.trimToEmpty(methodParams[i]);
      // String字符串类型，以'或"开头
      if (StrUtil.startWithAny(str, "'", "\"")) {
        classs.add(new Object[]{StrUtil.sub(str, 1, str.length() - 1), String.class});
      }
      // boolean布尔类型，等于true或者false
      else if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
        classs.add(new Object[]{Boolean.valueOf(str), Boolean.class});
      }
      // long长整形，以L结尾
      else if (StrUtil.endWith(str, "L")) {
        classs.add(new Object[]{Long.valueOf(StrUtil.sub(str, 0, str.length() - 1)), Long.class});
      }
      // double浮点类型，以D结尾
      else if (StrUtil.endWith(str, "D")) {
        classs.add(new Object[]{Double.valueOf(StrUtil.sub(str, 0, str.length() - 1)), Double.class});
      }
      // 其他类型归类为整形
      else {
        classs.add(new Object[]{Integer.valueOf(str), Integer.class});
      }
    }
    return classs;
  }

  /**
   * 获取参数类型
   *
   * @param methodParams 参数相关列表
   * @return 参数类型列表
   */
  public static Class<?>[] getMethodParamsType(List<Object[]> methodParams) {
    Class<?>[] classs = new Class<?>[methodParams.size()];
    int index = 0;
    for (Object[] os : methodParams) {
      classs[index] = (Class<?>) os[1];
      index++;
    }
    return classs;
  }

  /**
   * 获取参数值
   *
   * @param methodParams 参数相关列表
   * @return 参数值列表
   */
  public static Object[] getMethodParamsValue(List<Object[]> methodParams) {
    Object[] classs = new Object[methodParams.size()];
    int index = 0;
    for (Object[] os : methodParams) {
      classs[index] = os[0];
      index++;
    }
    return classs;
  }
}
