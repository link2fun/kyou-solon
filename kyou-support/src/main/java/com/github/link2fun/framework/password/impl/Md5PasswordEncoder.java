package com.github.link2fun.framework.password.impl;

import cn.hutool.core.util.StrUtil;
import com.github.link2fun.framework.password.PasswordEncoder;
import com.github.link2fun.support.utils.sign.Md5Utils;

/**
 * MD5密码编码器实现类。
 *
 * @author link2fun
 */
public class Md5PasswordEncoder implements PasswordEncoder {

  /**
   * 对原始密码进行MD5编码。
   *
   * @param rawPassword 原始密码
   * @return 编码后的密码
   */
  @Override
  public String encode(final String rawPassword) {
    return Md5Utils.hash(rawPassword);
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
    return StrUtil.equals(Md5Utils.hash(rawPassword), encodedPassword);
  }
}
