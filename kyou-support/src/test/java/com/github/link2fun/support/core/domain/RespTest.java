package com.github.link2fun.support.core.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Resp 的响应形状: code/msg/data 三件套, 子类字段平铺在同一层, data 为空时不落进 JSON。
 */
public class RespTest {

  /** 与线上序列化配置一致: null 字段不进 JSON */
  private static final ObjectMapper MAPPER = new ObjectMapper()
    .setDefaultPropertyInclusion(JsonInclude.Value.construct(
      JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL));

  /** 形状特殊的响应: 额外字段与 code/msg 平铺在同一层 */
  @Getter
  @Setter
  private static final class DetailResponse extends Resp<Void> {
    private List<String> ids;
    private String note;
  }

  @Test
  @DisplayName("成功响应只有 code 与 msg")
  void successCarriesOnlyCodeAndMsg() throws Exception {
    assertEquals(
      MAPPER.readTree("{\"code\":200,\"msg\":\"操作成功\"}"),
      MAPPER.readTree(MAPPER.writeValueAsString(Resp.success())));
  }

  @Test
  @DisplayName("成功响应带 data")
  void successCarriesData() throws Exception {
    assertEquals(
      MAPPER.readTree("{\"code\":200,\"msg\":\"操作成功\",\"data\":\"payload\"}"),
      MAPPER.readTree(MAPPER.writeValueAsString(Resp.successData("payload"))));
  }

  @Test
  @DisplayName("成功响应可自定义消息")
  void successMessageIsCustomizable() throws Exception {
    assertEquals(
      MAPPER.readTree("{\"code\":200,\"msg\":\"已受理\"}"),
      MAPPER.readTree(MAPPER.writeValueAsString(Resp.successMessage("已受理"))));
  }

  @Test
  @DisplayName("失败响应默认 500")
  void failureDefaultsTo500() throws Exception {
    assertEquals(
      MAPPER.readTree("{\"code\":500,\"msg\":\"操作失败\"}"),
      MAPPER.readTree(MAPPER.writeValueAsString(Resp.error("操作失败"))));
  }

  @Test
  @DisplayName("失败响应可指定状态码")
  void failureCarriesGivenCode() throws Exception {
    assertEquals(
      MAPPER.readTree("{\"code\":403,\"msg\":\"权限不足\"}"),
      MAPPER.readTree(MAPPER.writeValueAsString(Resp.error(403, "权限不足"))));
  }

  @Test
  @DisplayName("按影响行数返回成功或失败")
  void affectedRowsDecideSuccessOrFailure() throws Exception {
    assertEquals(
      MAPPER.readTree("{\"code\":200,\"msg\":\"操作成功\"}"),
      MAPPER.readTree(MAPPER.writeValueAsString(Resp.ofAffected(1L))));
    assertEquals(
      MAPPER.readTree("{\"code\":500,\"msg\":\"操作失败\"}"),
      MAPPER.readTree(MAPPER.writeValueAsString(Resp.ofAffected(0L))));
    assertEquals(
      MAPPER.readTree("{\"code\":500,\"msg\":\"操作失败\"}"),
      MAPPER.readTree(MAPPER.writeValueAsString(Resp.ofAffected(false))));
  }

  @Test
  @DisplayName("子类的额外字段与 code/msg 平铺在同一层")
  void subclassFieldsSitOnTheSameLevel() throws Exception {
    final DetailResponse response = new DetailResponse();
    response.setIds(List.of("1", "2"));
    response.setNote("备注");

    assertEquals(
      MAPPER.readTree("{\"code\":200,\"msg\":\"操作成功\",\"ids\":[\"1\",\"2\"],\"note\":\"备注\"}"),
      MAPPER.readTree(MAPPER.writeValueAsString(response)));
  }

  @Test
  @DisplayName("子类的 data 为空时不落进 JSON")
  void nullDataIsOmitted() throws Exception {
    final DetailResponse response = new DetailResponse();
    response.setNote("备注");

    assertEquals(
      MAPPER.readTree("{\"code\":200,\"msg\":\"操作成功\",\"note\":\"备注\"}"),
      MAPPER.readTree(MAPPER.writeValueAsString(response)));
  }
}
