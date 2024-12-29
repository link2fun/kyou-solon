package com.github.link2fun.generator.domain.pdmaner.dto.standardfield;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PDManerStandardField implements Serializable {
  private String defKey;
  private String defName;
  private List<PDManerStandardFieldInfo> fields;

}
