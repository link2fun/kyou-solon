package com.github.link2fun.generator.domain.pdmaner.dto.entity;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;


@Data
public class PDManerEntity implements Serializable {

  private String id;
  private String defKey;
  private String defName;

  public String getDefName() {
    return StrUtil.removeSuffix(defName, "表");
  }

  private String comment;

  private String type;

  private PDManerEntityProperty properties;

  private String nameTemplate;

  private List<PDManerEntityHeader> headers;
  private List<PDManerEntityField> fields;


  private List<Object> indexes;
  private List<Object> correlations;

  private JSONObject sysProps;

  private JSONObject notes;


  /** 是否使用分离表单 */
  public boolean useSeparateForm() {
    return Convert.toBool(getProperties().getUseSeparateForm(), false);
  }

  /** 是否是数据库实体，如果不是的话，不生成 mybatis 相关注解 */
  public boolean dbEntity() {
    return Convert.toBool(getProperties().getDbEntity(), true);
  }


  /** 主键字段 */
  public PDManerEntityField primaryKeyField() {
    return getFields().stream().filter(PDManerEntityField::getPrimaryKey).findFirst().orElseGet(PDManerEntityField::new);
  }

  /**
   * 主键变量名
   * eg: BdCustomer.id -> customerId
   */
  public String primaryKeyVar() {

    if (StrUtil.startWith(primaryKeyField().fieldName(), entityVar())) {
      return primaryKeyField().fieldName();
    }

    return entityVar() + primaryKeyField().getFieldNameUpperCamel();
  }

  /**
   * 主键变量名s
   * eg: BdCustomer.id -> customerIds
   */
  public String primaryKeyVars() {
    return primaryKeyVar() + "s";
  }


  public boolean isDetailTable() {
    String detailTableInfo = getProperties().getDetailTableInfo();
    if (StrUtil.isBlank(detailTableInfo)) {
      return false;
    }


    List<String> tableInfo = StrUtil.split(StrUtil.split(detailTableInfo, ";").get(0), ",");
    if (tableInfo.size() < 3) {
      return false;
    }

    return true;
  }


  /** Java Service Interface 名称 */
  public String getJavaServiceItfClassName() {
    return "I" + getProperties().getWebFunctionType() + "Service";
  }

  /** Java Service Var 名称 */
  public String getJavaServiceItfVar() {
    return "" + getProperties().getWebEntityVar() + "Service";
  }

  public String getJavaServiceImpl() {

    return getProperties().getWebFunctionType() + "ServiceImpl";
  }

  public String getJavaApi() {

    return getProperties().getWebFunctionType() + "Controller";
  }


  public String getJavaMapper() {

    return getProperties().getWebEntityType() + "Mapper";
  }

  /** 详情页的 DTO 类名, 实际的 DTO 建议使用 easyQuery 插件生成 */
  public String detailDTOClassName() {
    return getProperties().getWebEntityType() + "DetailDTO";
  }

  /** 分页查询 列表查询 result Class */
  public String pageDTOClassName() {
    return getProperties().getWebEntityType() + "PageDTO";
  }

  /** eg: bd_customer -> customer */
  public String vari() {
    return entityVar();
  }

  /** eg: bd_customer -> Customer */
  public String varUpper() {
    return entityVarUpperCamel();
  }


  /** eg: bd_customer -> BaseDocCustomer */
  public String functionType() {
    return getProperties().getWebFunctionType();
  }

  /** eg: bd_customer -> customer */
  public String entityVar() {
    return getProperties().getWebEntityVar();
  }

  /** eg: bd_customer -> customerService */
  public String serviceVar() {
    return entityVar() + "Service";
  }

  /** eg: bd_customer -> Customer */
  public String entityVarUpperCamel() {
    return getProperties().getWebEntityVarUpperCamel();
  }

  /** eg: bd_customer -> BdCustomer */
  public String entityClassName() {
    return getProperties().getWebEntityType();
  }

  /** eg: bd_customer -> BdCustomerProxy */
  public String entityProxyClassName() {
    return entityClassName() + "Proxy";
  }

  /** eg: bd_customer -> customerProxy */
  public String entityProxyVar() {
    return entityVar() + "Proxy";
  }

  /** eg: bd_customer -> BdCustomerReq */
  public String entityTypeReqClassName() {
    return entityClassName() + "Req";
  }

  /** eg: bd_customer -> BdCustomerReq.SearchReq */
  public String entityReqSearchReqClassName() {
    return entityTypeReqClassName() + ".SearchReq";
  }

  /** eg: bd_customer -> BdCustomerReq.AddReq */
  public String entityTypeReqAddReqClassName() {
    return entityTypeReqClassName() + ".AddReq";
  }

  /** eg: bd_customer -> BdCustomerReq.ModifyReq */
  public String entityTypeReqModifyReqClassName() {
    return entityTypeReqClassName() + ".ModifyReq";
  }


  public boolean hasUniqueCode() {
    return fields.stream().anyMatch(field -> StrUtil.equals(field.domainCode(), "UniqueCode"));
  }

  public boolean hasUniqueName() {
    return fields.stream().anyMatch(field -> StrUtil.equals(field.domainCode(), "UniqueName"));
  }

  public PDManerEntityField uniqueCodeField() {
    return fields.stream().filter(field -> StrUtil.equals(field.domainCode(), "UniqueCode"))
      .findFirst().orElseGet(PDManerEntityField::new);
  }

  public List<PDManerEntityField> uniqueFieldList() {
    return fields.stream().filter(field -> StrUtil.startWith(field.domainCode(), "Unique"))
      .collect(Collectors.toList());
  }

  public PDManerEntityField uniqueNameField() {
    return fields.stream().filter(field -> StrUtil.equals(field.domainCode(), "UniqueName"))
      .findFirst().orElseGet(PDManerEntityField::new);
  }


  public List<PDManerEntityField> findListBy() {
    return fields.stream().filter(PDManerEntityField::getFindListBy)
      .collect(Collectors.toList());
  }

  public List<PDManerEntityField> findOneBy() {
    return fields.stream().filter(PDManerEntityField::getFindOneBy)
      .collect(Collectors.toList());
  }


  public Boolean hasEntityMethod(String entityMethod) {
    if (StrUtil.isBlank(entityMethod) || StrUtil.isBlank(getProperties().getMethodNames())) {
      return false;
    }
    List<String> methodList = StrUtil.split(getProperties().getMethodNames(), ",")
      .stream().map(String::toLowerCase).collect(Collectors.toList());

    return CollectionUtil.contains(methodList, entityMethod.toLowerCase());


  }

  public List<PDManerEntityField> fieldMethod(String methodName) {
    return fields.stream().filter(field -> field.needMethod(methodName))
      .collect(Collectors.toList());
  }
}
