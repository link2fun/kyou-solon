package com.github.link2fun.support.web.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.noear.solon.core.handle.Result;
import org.noear.solon.core.util.DataThrowable;

/**
 * 接口状态码
 *
 * @author xm
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApiCode extends DataThrowable {

  private int code = 0;

  private String description = "";

  public ApiCode(int code) {
    super(code + "");
    this.code = code;
  }

  public ApiCode(int code, String description) {
    super(description);
    this.code = code;
    this.description = description;
  }

  public ApiCode(Throwable throwable) {
    super(throwable.getMessage(), throwable);
    this.code = Result.FAILURE_CODE;
    this.description = throwable.getMessage();
  }

}
