package com.github.link2fun.generator.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.link2fun.generator.domain.GenTableColumn;
import com.github.link2fun.generator.domain.pdmaner.PDManer;
import com.github.link2fun.generator.domain.pdmaner.dto.domain.PDManerDomain;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntity;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntityField;
import com.github.link2fun.generator.domain.pdmaner.dto.profile.PDManerDataTypeSupport;
import com.github.link2fun.generator.domain.pdmaner.dto.profile.PDManerProfileUiHint;
import com.github.link2fun.support.constant.GenConstants;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.link2fun.generator.util.GenUtils.arraysContains;

@SuppressWarnings("unused")
@Slf4j
public class PDManerTool {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static PDManer loadPDManer(String path) {
    String content = FileUtil.readUtf8String(path);
    try {
      return objectMapper.readValue(content, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

  }


  public static String lowerUnder2LowerCamel(String fieldField) {
    return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, StrUtil.toUnderlineCase(fieldField));
  }

  public static String lowerUnder2UpperCamel(String fieldField) {

    return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, StrUtil.toUnderlineCase(fieldField));
  }

  public static String upperCamelToLowerCamel(String content) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, content);
  }

  public static String matchJavaTypeByDomain(PDManer pdManer, PDManerEntityField field) {


    return matchLangType(pdManer, field, "Java");

  }


  public static String matchLangType(PDManer pdManer, PDManerEntityField field, String expectLang) {
    String typeId = pdManer.getProfile().getDataTypeSupports().stream()
      .filter(item -> item.getDefKey().equalsIgnoreCase(expectLang)).map(PDManerDataTypeSupport::getId)
      .findFirst().orElseThrow();

    if (StrUtil.isNotBlank(field.getDomain())) {

      return pdManer.getDomains().stream()
        .filter(domain -> domain.getId().equals(field.getDomain()))
        .map(domain -> pdManer.getDataTypeMapping().getMappings()
          .stream().filter(m -> m.getId().equals(domain.getApplyFor()))
          .findFirst().orElseThrow().get(typeId)).findFirst().orElseThrow(() -> new RuntimeException("matchLangType fail field: " + field.getDefKey() + " domain: " + field.getDomain() + " expectLang: " + expectLang));
    }

    return pdManer.getDataTypeMapping().getMappings().stream().filter(mapping -> mapping.getId().equals(field.getBaseType())).findFirst().map(mapping -> mapping.get(typeId))
      .orElseThrow(() -> new RuntimeException("matchLangType fail field: " + field.getDefKey() + " baseType: " + field.getBaseType() + " expectLang: " + expectLang));
  }


  public static String matchDomainCode(PDManer chiner, String type) {

    return chiner.getDomains().stream()
      .filter(domain -> domain.getId().equals(type))
      .findFirst().map(PDManerDomain::getDefKey).orElse("");
  }


  /**
   * 获取匹配的 UI建议
   *
   * @param chiner chiner
   * @param uiHint uiHintId
   * @return uiHintKey
   */
  public static String matchUiSuggestion(PDManer chiner, String uiHint) {
    return chiner.getProfile().getUiHintList().stream()
      .filter(item -> item.getId().equals(uiHint))
      .findFirst().map(PDManerProfileUiHint::getDefKey)
      .orElse("");
  }


  public static List<GenTableColumn> extractTableColumns(PDManer pdManer, String tableName) {

    PDManerEntity entity = pdManer.getEntities().stream()
      .filter(e -> StrUtil.equals(tableName, e.getDefKey()))
      .findFirst().orElseThrow(() -> new RuntimeException("没有找到对应表"));
    PDManer _info = ObjectUtil.cloneByStream(pdManer);
    return entity.getFields().stream().map(field -> {
      field.setChiner(_info);

      String columnName = field.getDefKey();

      GenTableColumn column = new GenTableColumn();
      //      column.setColumnId();
      //      column.setTableId();
      column.setColumnName(columnName);
      column.setColumnComment(field.getDefName());
      // 从 field 中提取数据库类型

//      column.setColumnType();
      column.setJavaType(field.getFieldJavaType());
      column.setJavaField(field.fieldName());
      column.setIsPk(field.getPrimaryKey() ? "1" : "0");
      column.setIsIncrement(field.getAutoIncrement() ? "1" : "0");
      column.setIsRequired(field.getNotNull() ? "1" : "0");
      column.setIsInsert(GenConstants.REQUIRE); // 默认字段需要insert
      // 编辑字段
      if (!arraysContains(GenConstants.COLUMNNAME_NOT_EDIT, columnName) && !column.isPk()) {
        column.setIsEdit(GenConstants.REQUIRE);
      }
      // 列表字段
      if (!arraysContains(GenConstants.COLUMNNAME_NOT_LIST, columnName) && !column.isPk()) {
        column.setIsList(GenConstants.REQUIRE);
      }
      // 查询字段
      if (!arraysContains(GenConstants.COLUMNNAME_NOT_QUERY, columnName) && !column.isPk()) {
        column.setIsQuery(GenConstants.REQUIRE);
      }
      column.setQueryType(GenConstants.QUERY_EQ); // 默认都是等于查询
      // 查询字段类型
      if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "name")) {
        column.setQueryType(GenConstants.QUERY_LIKE);
      }
      // 状态字段设置单选框
      if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "status")) {
        column.setHtmlType(GenConstants.HTML_RADIO);
      }
      // 类型&性别字段设置下拉框
      else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "type")
        || org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "sex")) {
        column.setHtmlType(GenConstants.HTML_SELECT);
      }
      // 图片字段设置图片上传控件
      else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "image")) {
        column.setHtmlType(GenConstants.HTML_IMAGE_UPLOAD);
      }
      // 文件字段设置文件上传控件
      else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "file")) {
        column.setHtmlType(GenConstants.HTML_FILE_UPLOAD);
      }
      // 内容字段设置富文本控件
      else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "content")) {
        column.setHtmlType(GenConstants.HTML_EDITOR);
      }
//      column.setDictType();
//      column.setSort();
//      column.setSearchValue();
//      column.setCreateBy();
//      column.setCreateTime();
//      column.setUpdateBy();
//      column.setUpdateTime();
//      column.setParams();


      return column;
    }).collect(Collectors.toList());
  }
}
