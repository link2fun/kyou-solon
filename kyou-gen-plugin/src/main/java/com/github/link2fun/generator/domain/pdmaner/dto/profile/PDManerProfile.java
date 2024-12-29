package com.github.link2fun.generator.domain.pdmaner.dto.profile;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class PDManerProfile implements Serializable {

  @JsonProperty("default")
  private Object _default;

  private String javaHome;

  private PDManerProfileSql sql;

  private List<PDManerDataTypeSupport> dataTypeSupports;

  private List<PDManerProfileCodeTemplate> codeTemplates;

  private PDManerProfileGeneratorDoc generatorDoc;

  private String modelType;

  private String relationFieldSize;

  private List<Map<String,Object>> headers;

  private List<String> recentColors;

  @JsonProperty(value = "DDLToggleCase")
  private String ddlToggleCase;

  @JsonProperty("uiHint")
  private List<PDManerProfileUiHint> uiHintList;

  public PDManerProfileUiHint findUiHintById(String id) {
    return uiHintList.stream()
      .filter(uiHint -> StrUtil.equals(uiHint.getId(), id))
      .findFirst()
      .orElseThrow();
  }

  public String findUiHintDefKey(String uiHintId) {
    return findUiHintById(uiHintId).getDefKey();
  }

}
