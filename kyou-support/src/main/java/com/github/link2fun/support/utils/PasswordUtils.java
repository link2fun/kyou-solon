package com.github.link2fun.support.utils;

import com.github.link2fun.framework.password.PasswordEncoder;
import org.noear.solon.Solon;

/**
 * 密码加密与比对工具
 */
public class PasswordUtils {

  /**
   * 生成BCryptPasswordEncoder密码
   *
   * @param password 密码
   * @return 加密字符串
   */
  public static String encryptPassword(String password) {
    PasswordEncoder passwordEncoder = Solon.context().getBean(PasswordEncoder.class);
    return passwordEncoder.encode(password);
  }

  /**
   * 判断密码是否相同
   *
   * @param rawPassword     真实密码
   * @param encodedPassword 加密后字符
   * @return 结果
   */
  public static boolean matchesPassword(String rawPassword, String encodedPassword) {
    PasswordEncoder passwordEncoder = Solon.context().getBean(PasswordEncoder.class);
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }
}
