package com.github.link2fun.support.core.domain.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class MenuDTO implements Serializable {

  @JsonSerialize(using = ToStringSerializer.class)
  private Long menuId;

  private String menuName;

  private Long parentId;


}
