package com.github.link2fun.generator.domain.pdmaner.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PDManerEntityProperty implements Serializable {
  private String packageName;
  private String webEntityType;
  private String webEntityVar;
  private String webEntityVarUpperCamel;
  private String webFunctionType;
  private String webFunctionVar;

  /** 是否使用列表查询 */
  private String useListSearch;

  /** 是否使用分离的 表单 */
  private String useSeparateForm;

  /**
   * 是否是明细表
   * eg: sd_sale_order_head,id,document_id
   */
  private String detailTableInfo;

  /** 是否是数据库实体：默认为 true */
  private String dbEntity;

  private String methodNames;


}
