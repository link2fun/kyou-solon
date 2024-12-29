package com.github.link2fun.generator.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.link2fun.generator.domain.GenTable;
import com.github.link2fun.generator.domain.GenTableColumn;
import com.github.link2fun.generator.domain.pdmaner.PDManer;
import com.github.link2fun.generator.domain.pdmaner.dto.datatypemapping.PDManerDataTypeMapping;
import com.github.link2fun.generator.domain.pdmaner.dto.datatypemapping.PDManerDataTypeMappingInfo;
import com.github.link2fun.generator.domain.pdmaner.dto.domain.PDManerDomain;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntity;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntityField;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntityProperty;
import com.github.link2fun.generator.domain.pdmaner.dto.profile.PDManerDataTypeSupport;
import com.github.link2fun.generator.domain.pdmaner.dto.profile.PDManerProfile;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.PDManerViewGroup;
import com.github.link2fun.generator.util.PDManerTool;
import com.github.link2fun.support.core.text.Convert;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

public class PDManerFactory {
  public static PDManer createPDManer(GenTable table, List<GenTableColumn> columns) {

    PDManer pdManer = new PDManer();
    pdManer.setName("kyou-solon");
    pdManer.setDescribe("Kyou Solon (音同 Q Solon)脚手架");
    pdManer.setAvatar("");
    pdManer.setVersion("");
//    pdManer.setCreatedTime();
//    pdManer.setUpdatedTime();
    pdManer.setDbConns(Lists.newArrayList()); // 手动构建的不需要SQL


    PDManerProfile profile = new PDManerProfile();
//    profile.set_default();
//    profile.setJavaHome();
//    profile.setSql();
    PDManerDataTypeSupport javaSupport = new PDManerDataTypeSupport();
    javaSupport.setId("Java");
    javaSupport.setDefKey("JAVA");

    PDManerDataTypeSupport typeScriptSupport = new PDManerDataTypeSupport();
    typeScriptSupport.setId("TypeScript");
    typeScriptSupport.setDefKey("TypeScript");


    profile.setDataTypeSupports(Lists.newArrayList(javaSupport,typeScriptSupport));
//    profile.setCodeTemplates();
//    profile.setGeneratorDoc();
//    profile.setModelType();
//    profile.setUiHintList();


    pdManer.setProfile(profile);


    PDManerEntity currentEntity = new PDManerEntity();
    currentEntity.setId(Convert.toStr(table.getTableId()));
    currentEntity.setDefKey(table.getTableName());
    currentEntity.setDefName(table.getTableComment());
    currentEntity.setComment(table.getRemark());


    String optionStr = table.getOptions();
    JSONObject tableOption = JSONUtil.parseObj(optionStr);


    PDManerEntityProperty entityProps = new PDManerEntityProperty();

    entityProps.setPackageName(tableOption.getStr("packageName"));
    entityProps.setWebEntityType(table.getClassName());
    String webEntityVar = tableOption.getStr("webEntityVar");
    String entityVarUpperCamel = PDManerTool.lowerUnder2UpperCamel(webEntityVar);
    entityProps.setWebEntityVar(webEntityVar);
    entityProps.setWebEntityVarUpperCamel(entityVarUpperCamel);
    entityProps.setWebFunctionType(tableOption.getStr("webFunctionType"));
    entityProps.setWebFunctionVar(tableOption.getStr("webFunctionVar"));
    entityProps.setUseListSearch(tableOption.getStr("useListSearch"));
    entityProps.setUseSeparateForm(tableOption.getStr("useSeparateForm"));
//    entityProps.setDetailTableInfo(); // TODO 需要兼容明细表
    entityProps.setDbEntity("Y"); // 默认是数据库实体
//    entityProps.setMethodNames();


    currentEntity.setProperties(entityProps);
//    currentEntity.setNameTemplate();
    currentEntity.setHeaders(Lists.newArrayList());


    List<PDManerEntityField> fields = columns.stream().map(col -> {
      PDManerEntityField field = new PDManerEntityField();
      field.setDefKey(col.getColumnName());
      field.setDefName(col.getColumnComment());
      field.setComment(StrUtil.EMPTY);
      field.setDomain(col.getJavaType());
      field.setType(col.getJavaType());
//      field.setLen(col.);
//      field.setScale();
      field.setPrimaryKey(col.isPk());
      field.setNotNull(col.isRequired());
      field.setAutoIncrement(col.isIncrement());
      field.setDefaultValue(null); // TODO 默认值需要兼容一下
      field.setHideInGraph(col.isList()); //
      field.setUiHint(null);
      field.setFieldAliasName(null);
      field.setFieldMethodList(StrUtil.EMPTY);
      field.setChiner(pdManer);


      return field;
    }).collect(Collectors.toList());

    currentEntity.setFields(fields);
    currentEntity.setIndexes(Lists.newArrayList());
    currentEntity.setCorrelations(Lists.newArrayList());


    pdManer.setEntities(Lists.newArrayList(currentEntity));
    pdManer.setViews(Lists.newArrayList());

    pdManer.setDicts(Lists.newArrayList());

    PDManerViewGroup currentViewGroup = new PDManerViewGroup();
    currentViewGroup.setDefKey(tableOption.getStr("moduleCode"));
    currentViewGroup.setDefName(tableOption.getStr("moduleDesc"));
    currentViewGroup.setRefEntities(Lists.newArrayList(Convert.toStr(table.getTableId())));
//    currentViewGroup.setRefDiagrams();
//    currentViewGroup.setRefViews();
//    currentViewGroup.setRefDicts();
//    currentViewGroup.setProperties();


    pdManer.setViewGroups(Lists.newArrayList(currentViewGroup));
    PDManerDataTypeMapping dataTypeMapping = new PDManerDataTypeMapping();
//    dataTypeMapping.setReferURL();
    PDManerDataTypeMappingInfo mappingInfoString = new PDManerDataTypeMappingInfo();
    mappingInfoString.setId("String");
    mappingInfoString.put("Java", "String");
    mappingInfoString.put("TypeScript", "string");

    PDManerDataTypeMappingInfo mappingInfoLong = new PDManerDataTypeMappingInfo();
    mappingInfoLong.setId("Long");
    mappingInfoLong.put("Java", "Long");
    mappingInfoLong.put("TypeScript", "number");

    PDManerDataTypeMappingInfo mappingInfoInteger = new PDManerDataTypeMappingInfo();
    mappingInfoInteger.setId("Integer");
    mappingInfoInteger.put("Java", "Integer");
    mappingInfoInteger.put("TypeScript", "number");

    PDManerDataTypeMappingInfo mappingInfoBigDecimal = new PDManerDataTypeMappingInfo();
    mappingInfoBigDecimal.setId("BigDecimal");
    mappingInfoBigDecimal.put("Java", "BigDecimal");
    mappingInfoBigDecimal.put("TypeScript", "number");

    PDManerDataTypeMappingInfo mappingInfoDate = new PDManerDataTypeMappingInfo();
    mappingInfoDate.setId("Date");
    mappingInfoDate.put("Java", "Date");
    mappingInfoDate.put("TypeScript", "string");

    PDManerDataTypeMappingInfo mappingInfoBoolean = new PDManerDataTypeMappingInfo();
    mappingInfoBoolean.setId("Boolean");
    mappingInfoBoolean.put("Java", "Boolean");
    mappingInfoBoolean.put("TypeScript", "boolean");

    PDManerDataTypeMappingInfo mappingInfoLocalDateTime = new PDManerDataTypeMappingInfo();
    mappingInfoLocalDateTime.setId("LocalDateTime");
    mappingInfoLocalDateTime.put("Java", "LocalDateTime");
    mappingInfoLocalDateTime.put("TypeScript", "string");

    PDManerDataTypeMappingInfo mappingInfoLocalDate = new PDManerDataTypeMappingInfo();
    mappingInfoLocalDate.setId("LocalDate");
    mappingInfoLocalDate.put("Java", "LocalDate");
    mappingInfoLocalDate.put("TypeScript", "string");


    dataTypeMapping.setMappings(Lists.newArrayList(mappingInfoString, mappingInfoLong, mappingInfoInteger, mappingInfoBigDecimal, mappingInfoDate, mappingInfoBoolean, mappingInfoLocalDate, mappingInfoLocalDateTime));


    pdManer.setDataTypeMapping(dataTypeMapping);



    pdManer.setDomains(Lists.newArrayList(buildNewDomain("String"),buildNewDomain("Long"),buildNewDomain("Integer"),buildNewDomain("BigDecimal"),buildNewDomain("Date"),buildNewDomain("Boolean"),buildNewDomain("LocalDate"),buildNewDomain("LocalDateTime")));
    pdManer.setDiagrams(Lists.newArrayList());
    pdManer.setStandardFields(Lists.newArrayList());


    return pdManer;
  }


  public static PDManerDomain buildNewDomain(String keyword) {
    PDManerDomain domain = new PDManerDomain();
    domain.setId(keyword);
    domain.setDefKey(keyword);
    domain.setDefName(keyword);
    domain.setApplyFor(keyword);

    return domain;
  }
}
