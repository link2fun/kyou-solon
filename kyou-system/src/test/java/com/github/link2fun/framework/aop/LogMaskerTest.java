package com.github.link2fun.framework.aop;

import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.system.modular.config.api.SystemConfigController;
import com.github.link2fun.system.modular.config.model.SysConfig;
import com.github.link2fun.system.modular.dept.api.SystemDeptController;
import com.github.link2fun.system.modular.user.api.SystemUserController;
import com.github.link2fun.system.modular.user.model.req.SysUserReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.noear.solon.core.handle.Context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 日志脱敏的行为契约：脱敏作用于实际序列化内容，按字段名判定，不依赖声明类型。
 */
class LogMaskerTest {

  private final LogInterceptor interceptor = new LogInterceptor();

  @Test
  @DisplayName("脱敏开关由 @Log(mask=true) 声明")
  void maskIsDeclaredOnAnnotation() throws Exception {
    Method add = SystemUserController.class.getMethod("add", SysUserReq.AddReq.class);
    Log addLog = add.getAnnotation(Log.class);
    assertNotNull(addLog);
    assertTrue(addLog.mask());
    assertTrue(addLog.excludeParamNames().length == 0);

    Method remove = SystemUserController.class.getMethod("remove", List.class);
    Log removeLog = remove.getAnnotation(Log.class);
    assertNotNull(removeLog);
    assertFalse(removeLog.mask());
  }

  @Test
  @DisplayName("入参含口令/联系方式/配置值的端点必须显式开启脱敏")
  void sensitiveEndpointsDeclareMask() throws Exception {
    // changeStatus 的 password 由客户端任意注入、部门带 phone/email、
    // 参数配置的 configValue 可能直接是 OSS/邮件密钥。删掉任一 mask=true 这里就会红。
    assertMaskDeclared(SystemUserController.class.getMethod("changeStatus", SysUser.class));
    assertMaskDeclared(SystemDeptController.class.getMethod("add", SysDept.class));
    assertMaskDeclared(SystemDeptController.class.getMethod("edit", SysDept.class));
    assertMaskDeclared(SystemConfigController.class.getMethod("add", SysConfig.class));
    assertMaskDeclared(SystemConfigController.class.getMethod("edit", SysConfig.class));
    // 导出接口的搜索条件同样可带手机号/邮箱/configValue
    assertMaskDeclared(SystemUserController.class.getMethod("export", Context.class, SysUser.class));
    assertMaskDeclared(SystemConfigController.class.getMethod("export", Context.class, SysConfig.class));
  }

  private static void assertMaskDeclared(Method method) {
    Log apiLog = method.getAnnotation(Log.class);
    assertNotNull(apiLog, method.getName() + " 缺少 @Log");
    assertTrue(apiLog.mask(),
      method.getDeclaringClass().getSimpleName() + "." + method.getName() + " 入参含敏感字段，必须声明 mask=true");
  }

  @Test
  @DisplayName("配置值与部门联系方式按真实字段名打码，保留前后少量字符便于核对")
  void masksConfigValueAndDeptContacts() {
    SysConfig config = new SysConfig();
    config.setConfigKey("sys.oss.secretKey");
    config.setConfigValue("AKIAIOSFODNN7EXAMPLE");

    String configJson = interceptor.toMaskedJson(config, Set.of());
    assertFalse(configJson.contains("AKIAIOSFODNN7EXAMPLE"), configJson);
    // 配置值属于 PARTIAL：保留前 3 后 4（20 字符 -> 中间 13 位打码）
    assertTrue(configJson.contains("\"configValue\":\"AKI*************MPLE\""), configJson);
    assertTrue(configJson.contains("\"configKey\":\"sys.oss.secretKey\""), configJson);

    SysDept dept = new SysDept();
    dept.setDeptName("研发部");
    dept.setPhone("13800001111");
    dept.setEmail("dev@example.com");

    String deptJson = interceptor.toMaskedJson(dept, Set.of());
    assertFalse(deptJson.contains("13800001111"), deptJson);
    assertFalse(deptJson.contains("dev@example.com"), deptJson);
    assertTrue(deptJson.contains("\"phone\":\"138****1111\""), deptJson);
    assertTrue(deptJson.contains("\"email\":\"dev********.com\""), deptJson);
    assertTrue(deptJson.contains("\"deptName\":\"研发部\""), deptJson);
  }

  static class BaseReq {
    public String userName = "tom";
  }

  static class SubReq extends BaseReq {
    public String password = "P@ssw0rd!";
  }

  @Test
  @DisplayName("运行时是子类时，按实际序列化内容打码，不受声明类型影响")
  void masksSubclassFields() {
    String json = interceptor.toMaskedJson(new SubReq(), Set.of());

    assertFalse(json.contains("P@ssw0rd!"), json);
    assertTrue(json.contains("\"password\":\"P*******!\""), json);
    assertTrue(json.contains("\"userName\":\"tom\""), json);
  }

  static class Inner {
    public String nickName = "汤姆";
    public String password = "P@ssw0rd!";
  }

  static class UnwrappedReq {
    public String userName = "tom";
    @com.fasterxml.jackson.annotation.JsonUnwrapped
    public Inner inner = new Inner();
  }

  @Test
  @DisplayName("@JsonUnwrapped 摊平后字段名对不上，仍按实际 JSON 名打码")
  void unwrappedPropertyStillMasked() {
    String json = interceptor.toMaskedJson(new UnwrappedReq(), Set.of());

    assertFalse(json.contains("P@ssw0rd!"), json);
    assertTrue(json.contains("\"password\":\"P*******!\""), json);
    assertTrue(json.contains("\"nickName\":\"汤姆\""), json);
  }

  static class HoldingList {
    public String name = "outer";
    public List<BaseReq> items = new ArrayList<>();
  }

  @Test
  @DisplayName("集合元素声明为干净类型、运行时混入子类，同样打码")
  void masksSubclassElementInList() {
    HoldingList holding = new HoldingList();
    holding.items.add(new SubReq());

    String json = interceptor.toMaskedJson(holding, Set.of());

    assertFalse(json.contains("P@ssw0rd!"), json);
    assertTrue(json.contains("\"password\":\"P*******!\""), json);
    assertTrue(json.contains("\"name\":\"outer\""), json);
  }

  /** 仅用于验证日期序列化跟随注入的映射器 */
  static class DatedReq {
    public String name = "tom";
    public java.time.LocalDateTime createTime = java.time.LocalDateTime.of(2026, 10, 19, 12, 34, 56);
  }

  @Test
  @DisplayName("日期格式跟随注入的全局映射器，与 API 响应一致")
  void dateFormatFollowsInjectedMapper() {
    LogInterceptor interceptor = new LogInterceptor();
    // 模拟 Solon 全局 mapper：通过 SimpleModule 注册 JSR-310 序列化器（非 setDateFormat）
    java.time.format.DateTimeFormatter formatter =
      java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    com.fasterxml.jackson.databind.JsonSerializer<java.time.LocalDateTime> serializer =
      new com.fasterxml.jackson.databind.JsonSerializer<>() {
        @Override
        public void serialize(java.time.LocalDateTime value, com.fasterxml.jackson.core.JsonGenerator gen,
                              com.fasterxml.jackson.databind.SerializerProvider provider) throws java.io.IOException {
          gen.writeString(formatter.format(value));
        }
      };
    com.fasterxml.jackson.databind.module.SimpleModule module =
      new com.fasterxml.jackson.databind.module.SimpleModule("test-jsr310");
    module.addSerializer(java.time.LocalDateTime.class, serializer);
    com.fasterxml.jackson.databind.ObjectMapper global =
      new com.fasterxml.jackson.databind.ObjectMapper().registerModule(module);
    interceptor.setObjectMapper(global);

    String json = interceptor.toMaskedJson(new DatedReq(), Set.of());

    assertTrue(json.contains("2026-10-19 12:34:56"), json);
    assertFalse(json.contains("[2026,10,19"), json);
  }

  @Test
  @DisplayName("干净请求的输出与直接序列化逐字节一致")
  void cleanPayloadOutputEqualsPlainSerialization() throws Exception {
    BaseReq req = new BaseReq();

    String plain = interceptor.getObjectMapper().writeValueAsString(req);
    assertEquals(plain, interceptor.toMaskedJson(req, Set.of()));
  }

  @Test
  @DisplayName("集合入参按实际内容逐个打码")
  void masksListElements() {
    SubReq a = new SubReq();
    a.userName = "a";
    SubReq b = new SubReq();
    b.userName = "b";

    String json = interceptor.toMaskedJson(List.of(a, b), Set.of());

    assertFalse(json.contains("P@ssw0rd!"), json);
    assertTrue(json.contains("\"userName\":\"a\""), json);
    assertTrue(json.contains("\"userName\":\"b\""), json);
  }

  @Test
  @DisplayName("打码保留首尾字符：口令只留各 1 位，联系方式/证件留前 3 后 4")
  void maskKeepsEdgesByLevel() {
    Map<String, Object> params = Map.of(
      "password", "P@ssw0rd!",
      "phonenumber", "13800001111",
      "idCardNo", "110101199001011234"
    );

    String json = interceptor.toMaskedJson(params, Set.of());

    assertTrue(json.contains("\"password\":\"P*******!\""), json);
    assertTrue(json.contains("\"phonenumber\":\"138****1111\""), json);
    assertTrue(json.contains("\"idCardNo\":\"110***********1234\""), json);
  }

  @Test
  @DisplayName("原文过短时整体打码，避免「保留前后几位」等于没打码")
  void shortValuesAreFullyMasked() {
    Map<String, Object> params = Map.of(
      "password", "123456",
      "phone", "12345678",
      "email", "a@b.cn"
    );

    String json = interceptor.toMaskedJson(params, Set.of());

    assertFalse(json.contains("123456"), json);
    // 6 位口令保留首尾后只能打掉 4 位，太弱，整体打码
    assertTrue(json.contains("\"password\":\"***\""), json);
    // 8 位手机号保留前 3 后 4 后只剩 1 位打码，太弱，整体打码
    assertTrue(json.contains("\"phone\":\"***\""), json);
    assertTrue(json.contains("\"email\":\"***\""), json);
  }

  @Test
  @DisplayName("非字符串敏感值（数字/对象）没有前后位语义，整体打码")
  void nonTextualSensitiveValuesAreFullyMasked() {
    Map<String, Object> params = new java.util.HashMap<>();
    params.put("token", 123456789L);
    params.put("credentials", Map.of("user", "tom"));

    String json = interceptor.toMaskedJson(params, Set.of());

    assertFalse(json.contains("123456789"), json);
    assertTrue(json.contains("\"token\":\"***\""), json);
    assertTrue(json.contains("\"credentials\":\"***\""), json);
  }

  @Test
  @DisplayName("query/form 参数的数组值逐元素打码，保留前后字符")
  void masksArrayValuedParams() {
    // MultiMap.toValuesMap() 的形状：值统一是数组
    Map<String, Object> params = Map.of(
      "oldPassword", List.of("WRONG_OLD_PWD_123"),
      "phonenumber", List.of("13800001111")
    );

    String json = interceptor.toMaskedJson(params, Set.of());

    assertFalse(json.contains("WRONG_OLD_PWD_123"), json);
    assertTrue(json.contains("\"oldPassword\":[\"W***************3\"]"), json);
    assertTrue(json.contains("\"phonenumber\":[\"138****1111\"]"), json);
  }
}
