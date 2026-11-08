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

  /** 任务组缺省值, 与 sys_job.job_group 的库默认值一致; 组仅作展示与筛选, 不参与引擎键 */
  public static final String DEFAULT_GROUP = "DEFAULT";

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
