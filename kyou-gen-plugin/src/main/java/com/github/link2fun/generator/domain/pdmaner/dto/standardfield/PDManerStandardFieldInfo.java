package com.github.link2fun.generator.domain.pdmaner.dto.standardfield;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PDManerStandardFieldInfo implements Serializable {
  private String defKey;
  private String defName;
  private String comment;
  private String type;
  private String len;
  private String scale;
  private Boolean primaryKey;
  private Boolean notNull;
  private Boolean autoIncrement;
  private String defaultValue;
  private Boolean hideInGraph;
  private String refDict;
  private String domain;
}
