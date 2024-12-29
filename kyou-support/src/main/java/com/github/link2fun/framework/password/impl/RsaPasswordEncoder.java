package com.github.link2fun.framework.password.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.github.link2fun.framework.password.PasswordEncoder;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * RSA密码编码器实现类。
 *
 * @author link2fun
 */
@Getter
@Slf4j
public class RsaPasswordEncoder implements PasswordEncoder {

  private final RSA rsa;

  public RsaPasswordEncoder(String publicKey, String privateKey) {
    Preconditions.checkArgument(StrUtil.isNotBlank(publicKey), "publicKey is required");
    Preconditions.checkArgument(StrUtil.isNotBlank(privateKey), "privateKey is required");

    this.rsa = SecureUtil.rsa(privateKey, publicKey);
  }

  /**
   * 对原始密码进行编码。
   *
   * @param rawPassword 原始密码
   * @return 编码后的密码
   */
  @Override
  public String encode(final String rawPassword) {
    return getRsa().encryptBase64(rawPassword, StandardCharsets.UTF_8, KeyType.PublicKey);
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
    final String hash = getRsa().decryptStr(rawPassword, KeyType.PrivateKey);
    return StrUtil.equals(hash, encodedPassword);
  }

}
