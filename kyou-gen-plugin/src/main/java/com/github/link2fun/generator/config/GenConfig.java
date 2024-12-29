package com.github.link2fun.generator.config;

import cn.hutool.core.util.StrUtil;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.ModuleInfo;
import lombok.Data;

import java.util.List;

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
  private Boolean autoRemovePre;

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
    return restfulLevel == 0;
  }

  /** restfulLevel = 2 使用 GET 查询 POST 新增 PUT 修改 DELETE 删除 */
  public Boolean isRestfulLevel2() {
    return restfulLevel == 2;
  }


  /** 基础包名 */
  private String basePackage;

  /** 工具类所处包名 */
  private String supportPackage;

  /**
   * 同步来源, 使用导入表和同步表的时候的数据来源.
   * pdmaner: 设计器(建议);
   * db: 数据库
   */
  private String syncSource;


  /** 是否从数据库同步 */
  public Boolean syncFromDb() {
    return StrUtil.equalsAnyIgnoreCase(getSyncSource(), "db");
  }

  /** 是否从PDManer同步 */
  public Boolean syncFromPDManer() {
    return StrUtil.equalsAnyIgnoreCase(getSyncSource(), "pdmaner");
  }

  private String pdmanerJsonPath;

  /** 项目后端资源管理器路径(如果相对路径不生效, 请使用绝对路径) */
  private String backendLocation;

  /** 项目前端资源管理器路径(如果相对路径不生效, 请使用绝对路径) */
  private String frontendLocation;

  private List<ModuleInfo> moduleInfoList;


}
