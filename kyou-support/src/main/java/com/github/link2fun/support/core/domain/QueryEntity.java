package com.github.link2fun.support.core.domain;

import com.easy.query.core.annotation.ColumnIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class QueryEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;


  /** 请求参数 */
  @ColumnIgnore
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private BaseEntityParams params;


  public BaseEntityParams getParams() {
    if (params == null) {
      params = new BaseEntityParams();
    }
    return params;
  }
}
