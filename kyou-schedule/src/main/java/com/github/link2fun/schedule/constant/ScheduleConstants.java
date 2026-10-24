package com.github.link2fun.schedule.constant;

import lombok.Getter;

/**
 * 任务调度通用常量
 *
 * @author ruoyi
 */
public class ScheduleConstants {
  /** 默认 */
  public static final String MISFIRE_DEFAULT = "0";

  /** 定时任务白名单配置（仅允许访问的包名，如其他需要可以自行添加） */
  public static final String[] JOB_WHITELIST_STR = {"com.github.link2fun"};

  /** 定时任务违规的字符 */
  public static final String[] JOB_ERROR_STR = {"java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
    "org.springframework", "org.apache", "com.github.link2fun.support.utils.file", "com.github.link2fun.support.config"};

  /** RMI 远程方法调用 */
  public static final String LOOKUP_RMI = "rmi:";

  /** LDAP 远程方法调用 */
  public static final String LOOKUP_LDAP = "ldap:";

  /** LDAPS 远程方法调用 */
  public static final String LOOKUP_LDAPS = "ldaps:";

  @Getter
  public enum Status {
    /**
     * 正常
     */
    NORMAL("0"),
    /**
     * 暂停
     */
    PAUSE("1");

    private final String value;

    Status(String value) {
      this.value = value;
    }

  }
}
