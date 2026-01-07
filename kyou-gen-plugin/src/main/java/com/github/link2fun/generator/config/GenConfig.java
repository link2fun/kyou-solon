package com.github.link2fun.generator.config;

import lombok.Data;

/**
 * 读取代码生成相关配置
 *
 * @author ruoyi
 */
@Data
public class GenConfig {

  /** 作者 */
  private String author;

  /** 生成包路径 */
  private String packageName;

  /** 自动去除表前缀，默认是false */
  private Boolean autoRemovePre = false;

  /** 表前缀(类名不会包含表前缀) */
  private String tablePrefix;


  /** 是否使用 swagger */
  private Boolean swaggerEnable = false;

  /**
   * restfulLevel = 0 只使用 GET 查询 POST 修改(含新增 编辑 删除)
   * restfulLevel = 2 使用 GET 查询 POST 新增 PUT 修改 DELETE 删除
   */
  private Integer restfulLevel = 0;

  /** restfulLevel = 0 只使用 GET 查询 POST 修改(含新增 编辑 删除) */
  public Boolean isRestfulLevel0() {
    return restfulLevel != null && restfulLevel == 0;
  }

  /** restfulLevel = 2 使用 GET 查询 POST 新增 PUT 修改 DELETE 删除 */
  public Boolean isRestfulLevel2() {
    return restfulLevel != null && restfulLevel == 2;
  }
}
