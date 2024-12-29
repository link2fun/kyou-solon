package com.github.link2fun.generator.domain.pdmaner;

import cn.hutool.json.JSONArray;
import com.github.link2fun.generator.domain.pdmaner.dto.datatypemapping.PDManerDataTypeMapping;
import com.github.link2fun.generator.domain.pdmaner.dto.diagram.Diagram;
import com.github.link2fun.generator.domain.pdmaner.dto.domain.PDManerDomain;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntity;
import com.github.link2fun.generator.domain.pdmaner.dto.profile.PDManerProfile;
import com.github.link2fun.generator.domain.pdmaner.dto.standardfield.PDManerStandardField;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.PDManerViewGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PDManer implements Serializable {

  /** 项目名 */
  private String name;
  private String describe;
  private String avatar;
  private String version;

  private String createdTime;
  private String updatedTime;

  private List<Object> dbConns;

  private JSONArray dbConn;
  private JSONArray logicEntities;
  private JSONArray namingRules;

  private PDManerProfile profile;

  private List<PDManerEntity> entities;

  private List<Object> views;

  private List<Object> dicts;

  private List<PDManerViewGroup> viewGroups;

  private PDManerDataTypeMapping dataTypeMapping;

  private List<PDManerDomain> domains;

  private List<Diagram> diagrams;

  private List<PDManerStandardField> standardFields;

}
