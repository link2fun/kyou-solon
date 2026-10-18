package com.github.link2fun.schedule.util;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.exception.ServiceException;
import org.noear.solon.Solon;

/**
 * 定时任务工具类
 *
 * @author ruoyi
 */
public class ScheduleUtils {

  /** 定时任务 bean 允许的包名前缀 */
  private static final String TASK_PACKAGE_PREFIX = "com.github.link2fun.schedule.task";


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
    String beanName = StrUtil.split(invokeTarget, ".").get(0);
    Object obj = Solon.context().getBean(beanName);
    if (ObjectUtil.isNull(obj)) {
      throw new ServiceException("定时任务目标 bean 不存在：" + beanName);
    }
    String beanPackageName = obj.getClass().getPackage().getName();
    return StrUtil.startWith(beanPackageName, TASK_PACKAGE_PREFIX);
  }
}
