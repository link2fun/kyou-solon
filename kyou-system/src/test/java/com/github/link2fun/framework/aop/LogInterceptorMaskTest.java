package com.github.link2fun.framework.aop;

import com.github.link2fun.system.modular.user.model.req.SysUserReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 操作日志入库参数脱敏。
 *
 * <p>钉住真实请求 DTO 与 query 参数两条路径：敏感字段名命中即打码，非敏感字段原样保留。
 */
class LogInterceptorMaskTest {

  private final LogInterceptor interceptor = new LogInterceptor();

  @Test
  @DisplayName("新增用户的请求体中 password 必须脱敏")
  void masksPasswordInRequestBody() {
    SysUserReq.AddReq req = new SysUserReq.AddReq();
    req.setUserName("tom");
    req.setPassword("P@ssw0rd!");
    req.setRoleIds(List.of(1L, 2L));

    String json = interceptor.toMaskedJson(req, Set.of());

    assertFalse(json.contains("P@ssw0rd!"), json);
    assertTrue(json.contains("\"password\":\"P*******!\""), json);
    assertTrue(json.contains("\"userName\":\"tom\""), json);
  }

  @Test
  @DisplayName("query/form 参数同样脱敏，嵌套结构递归处理")
  void masksNestedParams() {
    Map<String, Object> params = Map.of(
      "userName", "tom",
      "password", "123456",
      "profile", Map.of("newPassword", "abc", "nickName", "T")
    );

    String json = interceptor.toMaskedJson(params, Set.of());

    assertFalse(json.contains("123456"), json);
    assertFalse(json.contains("\"abc\""), json);
    assertTrue(json.contains("\"nickName\":\"T\""), json);
  }

  @Test
  @DisplayName("excludeParamNames 指定的字段整体剔除")
  void excludesNamedParams() {
    Map<String, Object> params = Map.of("userName", "tom", "captcha", "8x2k");

    String json = interceptor.toMaskedJson(params, Set.of("captcha"));

    assertFalse(json.contains("captcha"), json);
    assertTrue(json.contains("tom"), json);
  }

  @Test
  @DisplayName("excludeParamNames 对 Bean 入参同样生效")
  void excludesNamedParamsOnBean() {
    SysUserReq.AddReq req = new SysUserReq.AddReq();
    req.setUserName("tom");
    req.setPassword("P@ssw0rd!");

    String json = interceptor.toMaskedJson(req, Set.of("password"));

    assertFalse(json.contains("password"), json);
    assertTrue(json.contains("\"userName\":\"tom\""), json);
  }

  @Test
  @DisplayName("嵌套 Bean 与集合里的敏感字段同样打码")
  void masksNestedBeanAndList() {
    SysUserReq.AddReq user = new SysUserReq.AddReq();
    user.setUserName("tom");
    user.setPassword("P@ssw0rd!");

    Wrapper wrapper = new Wrapper();
    wrapper.name = "outer";
    wrapper.user = user;
    wrapper.items = List.of(user);

    String json = interceptor.toMaskedJson(wrapper, Set.of());

    assertFalse(json.contains("P@ssw0rd!"), json);
    assertTrue(json.contains("\"password\":\"P*******!\""), json);
    assertTrue(json.contains("\"name\":\"outer\""), json);
  }

  @Test
  @DisplayName("序列化失败统一返回 [unserializable]，不抛出、不影响业务")
  void unserializableFallback() {
    Object bad = new Object() {
      public String getName() {
        throw new IllegalStateException("boom");
      }
    };

    assertEquals(LogInterceptor.UNSERIALIZABLE, interceptor.toMaskedJson(bad, Set.of()));
  }

  @Test
  @DisplayName("大集合与超长字符串在序列化前先截断，不做全量序列化")
  void truncatesOversizedPayloadBeforeSerialization() {
    List<Integer> big = new ArrayList<>();
    for (int i = 0; i < 150; i++) {
      big.add(i);
    }
    String json = interceptor.toMaskedJson(big, Set.of());

    assertTrue(json.contains("已截断"), json);
    assertFalse(json.contains(",100,"), json);

    Map<String, Object> params = Map.of("remark", "x".repeat(8192));
    String textJson = interceptor.toMaskedJson(params, Set.of());

    assertFalse(textJson.contains("x".repeat(4097)), textJson);
  }

  /** 仅用于验证嵌套结构 */
  static class Wrapper {
    public String name;
    public SysUserReq.AddReq user;
    public List<SysUserReq.AddReq> items;
  }
}
