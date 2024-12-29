package com.github.link2fun.generator.domain.pdmaner.dto.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class PDManerDomain implements Serializable {
  private String id;
  private String defKey;
  private String defName;
  private String applyFor;
  private String len;
  private String scale;
  private Object uiHint;

}
