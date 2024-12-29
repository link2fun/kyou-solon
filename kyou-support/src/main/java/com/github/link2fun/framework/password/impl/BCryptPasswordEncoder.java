package com.github.link2fun.framework.password.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.github.link2fun.framework.password.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * BCryptPasswordEncoder类是一个密码编码器，用于对密码进行加密和验证。
 * 它使用BCrypt算法对密码进行哈希处理，并提供了编码和匹配方法。
 *
 * @author link2fun
 */
@Slf4j
public class BCryptPasswordEncoder implements PasswordEncoder {

  /**
   * 对原始密码进行编码。
   *
   * @param rawPassword 原始密码
   * @return 编码后的密码
   */
  @Override
  public String encode(final String rawPassword) {
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
  }

  /**
   * 验证原始密码与编码后的密码是否匹配。
   *
   * @param rawPassword     原始密码
   * @param encodedPassword 编码后的密码
   * @return 如果匹配返回true，否则返回false
   */
  @Override
  public boolean matches(final String rawPassword, final String encodedPassword) {
    return BCrypt.checkpw(rawPassword, encodedPassword);
  }

}