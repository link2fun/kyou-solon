package com.github.link2fun.generator.domain.pdmaner.dto.datatypemapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PDManerDataTypeMapping implements Serializable {

  private String referURL;
  private List<PDManerDataTypeMappingInfo> mappings;

}
