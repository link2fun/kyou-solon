package com.github.link2fun.generator.domain.pdmaner.dto.datatypemapping;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class PDManerDataTypeMappingInfo extends HashMap<String, String> implements Serializable {


  public String getId() {
    return get("id");
  }

  public void setId(String id) {
    put("id", id);
  }

  public String getDefKey() {
    return get("defKey");
  }

  public String getDefName() {
    return get("defName");
  }
}
