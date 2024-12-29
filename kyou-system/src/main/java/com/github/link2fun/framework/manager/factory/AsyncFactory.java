package com.github.link2fun.framework.manager.factory;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.utils.LogUtils;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.support.utils.ip.AddressUtils;
import com.github.link2fun.support.utils.ip.IpUtils;
import com.github.link2fun.system.modular.logininfo.model.SysLogininfor;
import com.github.link2fun.system.modular.logininfo.service.ISystemLogininforService;
import com.github.link2fun.system.modular.operlog.model.SysOperLog;
import com.github.link2fun.system.modular.operlog.service.ISystemOperLogService;
import org.noear.solon.Solon;
import org.noear.solon.core.handle.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimerTask;

/**
 * 异步工厂（产生任务用）
 *
 * @author ruoyi
 */
public class AsyncFactory {
  private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

  /**
   * 记录登录信息
   *
   * @param username 用户名
   * @param status   状态
   * @param message  消息
   * @param args     列表
   * @return 任务task
   */
  public static TimerTask recordLogininfor(final String username, final String status, final String message,
                                           final Object... args) {
    final UserAgent userAgent = UserAgentUtil.parse(Context.current().userAgent());
    final String ip = IpUtils.getIpAddr();
    return new TimerTask() {
      @Override
      public void run() {
        String address = AddressUtils.getRealAddressByIP(ip);
        final String s = LogUtils.getBlock(ip) +
          address +
          LogUtils.getBlock(username) +
          LogUtils.getBlock(status) +
          LogUtils.getBlock(message);
        // 打印信息到日志
        sys_user_logger.info(s, args);
        // 获取客户端操作系统
        String os = userAgent.getOs().getName();
        // 获取客户端浏览器
        String browser = userAgent.getBrowser().getName();
        // 封装对象
        SysLogininfor logininfor = new SysLogininfor();
        logininfor.setUserName(username);
        logininfor.setIpaddr(ip);
        logininfor.setLoginLocation(address);
        logininfor.setBrowser(browser);
        logininfor.setOs(os);
        logininfor.setMsg(message);
        logininfor.setLoginTime(LocalDateTime.now());
        // 日志状态
        if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER)) {
          logininfor.setStatus(Constants.SUCCESS);
        } else if (Constants.LOGIN_FAIL.equals(status)) {
          logininfor.setStatus(Constants.FAIL);
        }
        // 插入数据
        Solon.context().getBean(ISystemLogininforService.class).insertLogininfor(logininfor);
      }
    };
  }

  /**
   * 操作日志记录
   *
   * @param operLog 操作日志信息
   * @return 任务task
   */
  public static TimerTask recordOper(final SysOperLog operLog) {
    return new TimerTask() {
      @Override
      public void run() {
        // 远程查询操作地点
        operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
        Solon.context().getBean(ISystemOperLogService.class).insertOperlog(operLog);
      }
    };
  }
}
