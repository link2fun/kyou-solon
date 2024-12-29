package com.github.link2fun.generator.domain.pdmaner.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PDManerEntityHeader implements Serializable {
  private String refKey;
  private Boolean hideInGraph;
  private Boolean freeze;
}
