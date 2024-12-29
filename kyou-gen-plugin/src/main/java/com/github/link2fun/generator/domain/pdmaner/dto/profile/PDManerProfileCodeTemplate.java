package com.github.link2fun.generator.domain.pdmaner.dto.profile;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
@Data
public class PDManerProfileCodeTemplate extends HashMap<String,String> implements Serializable {

  /**
   * dbDDL: 建表
   * appCode: 程序代码
   */
//  private String type;
  public String getType() {
    return get("type");
  }
//  private String applyFor;
  public String getApplyFor() {
    return get("applyFor");
  }
//  private String referURL;
  public String getReferURL() {
    return get("referURL");
  }
//  private String createTable;
  public String getCreateTable() {
    return get("createTable");
  }
//  private String createIndex;
  public String getCreateIndex() {
    return get("createIndex");
  }
//  private String content;
  public String getContent() {
    return get("content");
  }
}
