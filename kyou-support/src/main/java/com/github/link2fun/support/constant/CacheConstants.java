package com.github.link2fun.support.constant;

/**
 * 缓存的key 常量
 *
 * @author ruoyi
 */
@SuppressWarnings("unused")
public class CacheConstants {

  /** 验证码 redis key */
  public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

  /** 参数管理 cache key */
  public static final String SYS_CONFIG_KEY = "sys_config:";

  /** 字典管理 cache key */
  public static final String SYS_DICT_KEY = "sys_dict:";

  /** 防重提交 redis key */
  public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

  /** 限流 redis key */
  public static final String RATE_LIMIT_KEY = "rate_limit:";

  /** 登录账户密码错误次数 redis key */
  public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

  /** 验证码有效期（分钟） */
  public static final Integer CAPTCHA_CODE_EXPIRATION = 5;
}
