package com.github.link2fun.support.core.domain;

import com.github.link2fun.support.constant.HttpStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应信封：code/msg/data 三件套。
 *
 * <p>响应形状由类型声明，不由调用点现场拼装；形状特殊时继承本类派生，字段写死在子类里。
 */
@Setter
@Getter
public class Resp<T> implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 状态码 */
  private int code;

  /** 返回内容 */
  private String msg;

  /** 数据对象 */
  private T data;

  public Resp() {
    this.code = HttpStatus.SUCCESS;
    this.msg = "操作成功";
  }

  /**
   * 成功，不带数据
   */
  public static <T> Resp<T> success() {
    return new Resp<>();
  }

  /**
   * 成功，带数据
   */
  public static <T> Resp<T> successData(T data) {
    final Resp<T> result = new Resp<>();
    result.setData(data);
    return result;
  }

  /**
   * 成功，自定义消息
   */
  public static <T> Resp<T> successMessage(String msg) {
    final Resp<T> result = new Resp<>();
    result.setMsg(msg);
    return result;
  }

  /**
   * 失败，状态码 500
   */
  public static <T> Resp<T> error(String msg) {
    final Resp<T> result = new Resp<>();
    result.setCode(HttpStatus.ERROR);
    result.setMsg(msg);
    return result;
  }

  /**
   * 失败，指定状态码
   */
  public static <T> Resp<T> error(int code, String msg) {
    final Resp<T> result = new Resp<>();
    result.setCode(code);
    result.setMsg(msg);
    return result;
  }

  /**
   * 按写入影响的行数返回结果，0 视为未生效
   */
  public static <T> Resp<T> ofAffected(final long rows) {
    return rows != 0 ? Resp.success() : Resp.error("操作失败");
  }

  /**
   * 按写入是否生效返回结果
   */
  public static <T> Resp<T> ofAffected(final boolean affected) {
    return affected ? Resp.success() : Resp.error("操作失败");
  }
}
