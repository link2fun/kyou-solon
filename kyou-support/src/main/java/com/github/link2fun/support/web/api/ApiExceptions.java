package com.github.link2fun.support.web.api;

import com.github.link2fun.support.constant.HttpStatus;
import org.noear.solon.annotation.Note;

/**
 * 状态码定义
 *
 * @author xm
 */
public class ApiExceptions {

  /** 成功 */
  @Note("成功")
  public static final ApiCode CODE_200 = new ApiCode(HttpStatus.SUCCESS, "成功");

  /** 失败，未知错误 */
  @Note("失败，未知错误")
  public static final ApiCode CODE_400 = new ApiCode(HttpStatus.BAD_REQUEST, "未知错误");

  /** 登录已失效 */
  @Note("登录已失效或未登录")
  public static final ApiCode CODE_401 = new ApiCode(HttpStatus.UNAUTHORIZED, "登录已失效");

  /** 请求的接口不存在或不再支持 */
  @Note("请求的接口不存在或不再支持")
  public static final ApiCode CODE_404 = new ApiCode(HttpStatus.NOT_FOUND, "请求的接口不存在或不再支持");

  /** 未知错误 */
  @Note("未知错误")
  public static ApiCode CODE_400(Throwable cause) {
    return new ApiCode(cause);
  }

  /** 未知错误 */
  @Note("未知错误")
  public static ApiCode CODE_400(String description) {
    return new ApiCode(400, description);
  }

}
