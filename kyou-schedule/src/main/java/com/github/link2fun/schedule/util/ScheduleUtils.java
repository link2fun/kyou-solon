package com.github.link2fun.schedule.util;


import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.utils.StringUtils;
import org.noear.solon.Solon;

/**
 * 定时任务工具类
 *
 * @author ruoyi
 */
public class ScheduleUtils {


  /**
   * 检查包名是否为白名单配置
   *
   * @param invokeTarget 目标字符串
   * @return 结果
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public static boolean whiteList(String invokeTarget) {
    String packageName = StringUtils.subBefore(invokeTarget, "(", false);
    int count = StringUtils.count(packageName, ".");
    if (count > 1) {
      return StringUtils.containsAnyIgnoreCase(invokeTarget, Constants.JOB_WHITELIST_STR);
    }
    Object obj = Solon.context().getBean(StringUtils.split(invokeTarget, ".").get(0));
    String beanPackageName = obj.getClass().getPackage().getName();
    return StringUtils.containsAnyIgnoreCase(beanPackageName, Constants.JOB_WHITELIST_STR)
      && !StringUtils.containsAnyIgnoreCase(beanPackageName, Constants.JOB_ERROR_STR);
  }
}
