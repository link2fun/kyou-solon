package com.github.link2fun.framework.password;

/**
 * 密码编码器接口。
 * 参考自 Spring Security
 *
 * @author link2fun
 */
public interface PasswordEncoder {

  /**
   * 对原始密码进行编码。
   *
   * @param rawPassword 原始密码
   * @return 编码后的密码
   */
  String encode(String rawPassword);

  /**
   * 验证原始密码与编码后的密码是否匹配。
   *
   * @param rawPassword     原始密码
   * @param encodedPassword 编码后的密码
   * @return 如果匹配返回true，否则返回false
   */
  boolean matches(String rawPassword, String encodedPassword);

  /**
   * 升级编码密码。
   *
   * @param encodedPassword 编码后的密码
   * @return 如果升级成功返回true，否则返回false
   */
  default boolean upgradeEncoding(String encodedPassword) {
    return false;
  }

}