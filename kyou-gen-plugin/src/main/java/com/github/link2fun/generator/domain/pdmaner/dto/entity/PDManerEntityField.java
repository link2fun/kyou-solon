package com.github.link2fun.generator.domain.pdmaner.dto.entity;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.link2fun.generator.domain.GenTableColumn;
import com.github.link2fun.generator.domain.pdmaner.PDManer;
import com.github.link2fun.generator.util.PDManerTool;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PDManerEntityField implements Serializable {

  private String id;

  private String defKey;
  private String defName;
  private String comment;
  private String domain;
  private String type;
  private String len;
  private String scale;
  private Boolean primaryKey;
  private Boolean notNull;
  private Boolean autoIncrement;
  private String defaultValue;
  private Boolean hideInGraph;
  private String uiHint;

  private String refDict;

  private String baseType;

  //     "attr1": "country_id",
  @JsonProperty("attr1")
  private String fieldAliasName;
  //        "attr2": "~~findOneBy~~",
  @JsonProperty("attr2")
  private String fieldMethodList;
  //        "attr3": "",
  private String attr3;
  //        "attr4": "",
  private String attr4;
  //        "attr5": "",
  private String attr5;
  //        "attr6": "",
  private String attr6;
  //        "attr7": "",
  private String attr7;
  //        "attr8": "",
  private String attr8;
  //        "attr9": "",
  private String attr9;

  private Map<String, Object> extProps;


  @JsonIgnore
  private transient PDManer chiner;

  @JsonIgnore
  private transient GenTableColumn column;


  public String domainCode() {
    return PDManerTool.matchDomainCode(chiner, getDomain());
  }

  public String uiSuggest() {
    return PDManerTool.matchUiSuggestion(chiner, getUiHint());
  }

  /**
   * 是否使用范围查询
   *
   * @return true/false
   */
  public Boolean isQueryTypeBetweenOrRange() {
    return StrUtil.containsAnyIgnoreCase(uiSuggest(), "range") || StrUtil.containsAnyIgnoreCase(getColumn().getQueryType(),"between");
  }

  /** 是否使用多值查询 使用多值查询, 参数类型需要为 List<T> */
  public Boolean isQueryTypeIn() {
    return StrUtil.containsAnyIgnoreCase(getColumn().getQueryType(), "IN", "NOT_IN");
  }

  public String fieldName() {
    return PDManerTool.lowerUnder2LowerCamel(getDefKey());
  }

  public String getFieldNameUpperCamel() {
    return PDManerTool.lowerUnder2UpperCamel(getDefKey());
  }

  public String getFieldGetter() {
    return "get" + PDManerTool.lowerUnder2UpperCamel(getDefKey());
  }

  public String getFieldSetter() {
    return "set" + PDManerTool.lowerUnder2UpperCamel(getDefKey());
  }

  public String getFieldJavaType() {
    return PDManerTool.matchLangType(getChiner(), this, "Java");
  }

  public Boolean isString() {
    return StrUtil.equals(getFieldJavaType(), "String");
  }

  public String getFieldTsType() {
    return PDManerTool.matchLangType(getChiner(), this, "TypeScript");
  }

  public String getFieldMapperType() {
    return PDManerTool.matchLangType(getChiner(), this, "Mapper");
  }


  /** 字段是否是必填的 */
  public Boolean isRequired() {
    return Convert.toBool(getColumn().getIsRequired());
  }

  /**
   * 是否在新增模型中
   *
   * @return
   */
  public Boolean isInAddReq() {
    // 在新增模型中需要 column isInsert == 1
    if (!Convert.toBool(getColumn().getIsInsert(), false)) {
      // 代码生成器上没有勾选 新增
      return false;
    }

    return true;

  }

  /** 是否在修改的请求参数中 */
  public Boolean isInModifyReq() {
    return Convert.toBool(getColumn().getIsEdit(),false);
  }

  /** 是否在查询请求参数中 */
  public Boolean isInSearchReq() {
    return Convert.toBool(getColumn().getIsQuery(),false);
  }

  /**
   * 是否要提供 FindListBy 方法
   */
  public Boolean getFindListBy() {
    return needMethod("FindListBy") || needMethod("FindList");
  }

  /** 是否要提供 FindOneBy 方法 */
  public Boolean getFindOneBy() {
    return needMethod("FindOneBy") || needMethod("FindOne");
  }

  /** String 字段使用 NotNull 注解 */
  public Boolean getStrNotNull() {
    return needMethod("NotNull") && getNotNull();
  }


  public Boolean getCountBy() {

    String countBy = "CountBy";
    return needMethod(countBy);
  }

  public Boolean needMethod(String methodName) {

    // 优先使用 attr2 中的配置
    if (StrUtil.isNotBlank(getFieldMethodList())) {
      // 分隔符是 ,
      // 移除头尾 ~~
      this.fieldMethodList = StrUtil.removePrefix(getFieldMethodList(), "~~");
      this.fieldMethodList = StrUtil.removeSuffix(getFieldMethodList(), "~~");

      List<String> methodList = StrUtil.split(getFieldMethodList(), ",")
        .stream().map(String::toLowerCase).toList();
      return methodList.contains(methodName.toLowerCase());
    }


    if (StrUtil.isBlank(comment)) {
      return false;
    }
    // 分隔符是 ~~
    if (!StrUtil.contains(comment, "~~")) {
      return false;
    }
    List<String> partList = StrUtil.split(comment, "~~");
    if (partList.size() != 3) {
      return false;
    }
    List<String> methodList = StrUtil.split(partList.get(1), ",")
      .stream().map(String::toLowerCase).toList();


    return methodList.contains(methodName.toLowerCase());
  }


  private Boolean methodFilter(String methodName) {
    if (StrUtil.isBlank(getUiHint())) {
      return false;
    }
    String uiHintDefKey = chiner.getProfile().findUiHintDefKey(getUiHint());
    return StrUtil.contains(uiHintDefKey, methodName);
  }


  public Boolean getFillWhenInsert() {
    return Lists.newArrayList(
      "revision",
      "created_by",
      "created_time",
      "created_ip",
      "delete_already"
    ).stream().anyMatch(v -> v.equals(getDefKey()));
  }

  public Boolean getFillWhenInsertUpdate() {
    return Lists.newArrayList(
      "updated_by",
      "updated_time",
      "updated_ip"
    ).stream().anyMatch(v -> v.equals(getDefKey()));
  }

  public Boolean getIsLogic() {
    return StrUtil.equals(getDefKey(), "delete_already");
  }

  public Boolean getIsVersion() {
    return StrUtil.equals(getDefKey(), "revision");
  }


  // === 获取别名 |alias|

  public Boolean hasAlias() {

    // 优先使用 attr1 中的配置
    if (StrUtil.isNotBlank(getFieldAliasName())) {
      return true;
    }


    if (StrUtil.isBlank(comment)) {
      return false;
    }
    // 分隔符是 |
    if (!StrUtil.contains(comment, "|")) {
      return false;
    }
    List<String> partList = StrUtil.split(comment, "|");
    if (partList.size() != 3) {
      return false;
    }
    return true;
  }

  /** 获取字段别名 */
  public String alias() {
    if (!hasAlias()) {
      return StrUtil.EMPTY;
    }


    return Optional.ofNullable(StrUtil.trimToNull(getFieldAliasName())).orElseGet(() -> StrUtil.split(comment, "|").get(1));
  }

  /** 获取字段别名，驼峰命名 */
  public String aliasCamelCase() {
    return StrUtil.toCamelCase(alias());
  }

  /** 获取字段别名 首字母大写 驼峰格式 */
  public String aliasPascalCase() {
    return NamingCase.toPascalCase(alias());
  }

  public String aliasCamelCaseFirst() {
    if (hasAlias()) {
      return aliasCamelCase();
    }
    return fieldName();
  }

  public String aliasPascalCaseFirst() {
    if (hasAlias()) {
      return aliasPascalCase();
    }

    return getFieldNameUpperCamel();
  }

  public String aliasGetterFirst() {
    if (hasAlias()) {
      return "get" + aliasPascalCaseFirst();
    }
    return getFieldGetter();
  }

  /** 是否是特殊类型的查询条件, 特殊类型的需要单独适配 */
  public Boolean isEasyQueryTypeSpecial() {
    return StrUtil.equalsAnyIgnoreCase(getColumn().getQueryType(),  "BETWEEN");
  }

  public String easyQueryType() {
    switch (getColumn().getQueryType()) {
      case "LIKE":
        return "like";
      case "NOT LIKE":
        return "notLike";
      case "IN":
        return "in";
      case "NOT IN":
        return "notIn";
      case "GT":
        return "gt";
      case "GE":
        return "ge";
      case "LT":
        return "lt";
      case "LE":
        return "le";
      default:
        return "eq";
    }
  }
}
