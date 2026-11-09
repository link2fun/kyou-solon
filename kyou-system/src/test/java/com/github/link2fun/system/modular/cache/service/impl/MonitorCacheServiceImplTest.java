package com.github.link2fun.system.modular.cache.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 缓存写操作的白名单判定: 只允许业务命名空间, 会话前缀与 glob 一律拒绝
 */
public class MonitorCacheServiceImplTest {

  @Test
  @DisplayName("业务命名空间恰好是六个固定前缀")
  public void businessNamespacesAreTheFixedSix() {
    assertEquals(
      List.of("sys_config:", "sys_dict:", "captcha_codes:", "repeat_submit:", "rate_limit:", "pwd_err_cnt:"),
      MonitorCacheServiceImpl.businessNamespaces()
    );
  }

  @Test
  @DisplayName("六个业务命名空间精确匹配可写")
  public void businessNamespacesAreWritable() {
    MonitorCacheServiceImpl.businessNamespaces()
      .forEach(namespace -> assertTrue(MonitorCacheServiceImpl.isBusinessNamespace(namespace), namespace));
  }

  @Test
  @DisplayName("会话命名空间、通配符与近似值不可写")
  public void sessionNamespacesAndGlobsAreRejected() {
    assertFalse(MonitorCacheServiceImpl.isBusinessNamespace("passkey:login:session:"));
    assertFalse(MonitorCacheServiceImpl.isBusinessNamespace("passkey:login:token:"));
    assertFalse(MonitorCacheServiceImpl.isBusinessNamespace("passkey:custom:session:"));
    assertFalse(MonitorCacheServiceImpl.isBusinessNamespace("*"));
    assertFalse(MonitorCacheServiceImpl.isBusinessNamespace("sys_dict"));
    assertFalse(MonitorCacheServiceImpl.isBusinessNamespace("sys_dict:*"));
    assertFalse(MonitorCacheServiceImpl.isBusinessNamespace(null));
  }

  @Test
  @DisplayName("键以业务命名空间开头才可删")
  public void businessKeyPrefixesAreWritable() {
    assertTrue(MonitorCacheServiceImpl.isBusinessKey("sys_config:sys.account.captchaEnabled"));
    assertTrue(MonitorCacheServiceImpl.isBusinessKey("sys_dict:sys_user_sex"));
    assertFalse(MonitorCacheServiceImpl.isBusinessKey("pending:sys_dict:sys_user_sex"));
    assertFalse(MonitorCacheServiceImpl.isBusinessKey("passkey:login:token:1df76c2a"));
    assertFalse(MonitorCacheServiceImpl.isBusinessKey("*"));
    assertFalse(MonitorCacheServiceImpl.isBusinessKey(""));
    assertFalse(MonitorCacheServiceImpl.isBusinessKey(null));
  }
}
