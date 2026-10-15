package com.github.link2fun.schedule.util;


import cn.hutool.core.util.StrUtil;
import com.github.link2fun.support.constant.Constants;
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
    String packageName = StrUtil.subBefore(invokeTarget, "(", false);
    int count = StrUtil.count(packageName, ".");
    if (count > 1) {
      return StrUtil.containsAnyIgnoreCase(invokeTarget, Constants.JOB_WHITELIST_STR);
    }
    Object obj = Solon.context().getBean(StrUtil.split(invokeTarget, ".").get(0));
    String beanPackageName = obj.getClass().getPackage().getName();
    return StrUtil.containsAnyIgnoreCase(beanPackageName, Constants.JOB_WHITELIST_STR)
      && !StrUtil.containsAnyIgnoreCase(beanPackageName, Constants.JOB_ERROR_STR);
  }
}
