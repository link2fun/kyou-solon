package com.github.link2fun.generator.domain.pdmaner.dto.viewgroup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PDManerViewGroup implements Serializable {

  private String id;
  private String defKey;
  private String defName;
  private List<String> refEntities;
  private List<String> refDiagrams;
  private List<Object> refViews;
  private List<Object> refDicts;


  private ModuleInfo properties;
}
