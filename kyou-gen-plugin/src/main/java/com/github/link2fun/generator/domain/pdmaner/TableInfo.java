package com.github.link2fun.generator.domain.pdmaner;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.link2fun.generator.domain.GenTable;
import com.github.link2fun.generator.domain.GenTableColumn;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntity;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntityField;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntityProperty;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.ModuleInfo;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.PDManerViewGroup;
import com.github.link2fun.generator.util.GenUtils;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class TableInfo {

  private String author;

  public String getBasePackage() {
    return GenUtils.getGenConfig().getBasePackage();
  }

  public String getSupportModulePackage() {
    return GenUtils.getGenConfig().getSupportPackage();
  }

  private Boolean searchReqOnlyShowUi = true;

  @JsonIgnore
  private PDManer pdManer;
  private GenTable table;
  private String tableName;

  private PDManerEntity entity;

  private PDManerEntity parentEntity;

  private PDManerViewGroup viewGroup;

  private String parentTableKey;

  private String currentTableKey;

  public PDManerEntityField parentTableKeyField() {
    return parentEntity.getFields().stream()
        .filter(field -> StrUtil.equals(field.getDefKey(), parentTableKey))
        .findFirst().orElseThrow();
  }

  public PDManerEntityField currentTableKeyField() {
    return entity.getFields().stream()
        .filter(field -> StrUtil.equals(field.getDefKey(), currentTableKey))
        .findFirst().orElseThrow();
  }

  public String javaModularPackage() {
    return new StringBuilder().append(getBasePackage())
        .append(".").append(viewGroup.getProperties().getPackageName())
        .append(".").append("modular")
        .append(".").append(entity.getProperties().getPackageName()).toString();
  }

  public String javaModelFilePath() {
    return new StringBuilder()
        .append(GenUtils.getGenConfig().getBackendLocation())
        .append("\\main\\java\\").append(getBasePackage().replace(".", "\\"))
        .append("\\").append(getViewGroup().getProperties().getPackageName())
        .append("\\").append("modular\\")
        .append(getEntity().getProperties().getPackageName()).append("\\")
        .append("model\\")
        .append(getEntity().getProperties().getWebEntityType()).append(".java")
        .toString()
        .replace("\\", "/");
  }

  public String getJavaModelReqFilePath() {
    return new StringBuilder()
        .append(GenUtils.getGenConfig().getBackendLocation())
        .append("\\main\\java\\").append(getBasePackage().replace(".", "\\"))
        .append("\\").append(getViewGroup().getProperties().getPackageName())
        .append("\\").append("modular\\")
        .append(getEntity().getProperties().getPackageName()).append("\\")
        .append("model\\req\\")
        .append(getEntity().getProperties().getWebEntityType()).append("Req.java")
        .toString()
        .replace("\\", "/");
  }

  public String getMapperJavaFilePath() {
    return new StringBuilder()
        .append(GenUtils.getGenConfig().getBackendLocation())
        .append("\\main\\java\\").append(getBasePackage().replace(".", "\\"))
        .append("\\").append(getViewGroup().getProperties().getPackageName())
        .append("\\").append("modular\\")
        .append(getEntity().getProperties().getPackageName()).append("\\")
        .append("mapper\\")
        .append(getEntity().getProperties().getWebEntityType()).append("Mapper.java")
        .toString()
        .replace("\\", "/");
  }

  public String getMapperXmlFilePath() {
    return new StringBuilder()
        .append(GenUtils.getGenConfig().getBackendLocation())
        .append("\\main\\java\\").append(getBasePackage().replace(".", "\\"))
        .append("\\").append(getViewGroup().getProperties().getPackageName())
        .append("\\").append("modular\\")
        .append(getEntity().getProperties().getPackageName()).append("\\")
        .append("mapper\\")
        .append(getEntity().getProperties().getWebEntityType()).append("Mapper.xml")
        .toString()
        .replace("\\", "/");
  }

  public String getServiceItfFilePath() {
    return new StringBuilder()
        .append(GenUtils.getGenConfig().getBackendLocation())
        .append("\\main\\java\\").append(getBasePackage().replace(".", "\\"))
        .append("\\").append(getViewGroup().getProperties().getPackageName())
        .append("\\").append("modular\\")
        .append(getEntity().getProperties().getPackageName()).append("\\")
        .append("service\\")
        .append("I" + getEntity().getProperties().getWebFunctionType()).append("Service.java")
        .toString()
        .replace("\\", "/");
  }

  public String getServiceImplFilePath() {
    return new StringBuilder()
        .append(GenUtils.getGenConfig().getBackendLocation())
        .append("\\main\\java\\").append(getBasePackage().replace(".", "\\"))
        .append("\\").append(getViewGroup().getProperties().getPackageName())
        .append("\\").append("modular\\")
        .append(getEntity().getProperties().getPackageName()).append("\\")
        .append("service\\impl\\")
        .append(getEntity().getProperties().getWebFunctionType()).append("ServiceImpl.java")
        .toString()
        .replace("\\", "/");
  }

  public String getWebApiFilePath() {
    return new StringBuilder()
        .append(GenUtils.getGenConfig().getBackendLocation())
        .append("\\main\\java\\").append(getBasePackage().replace(".", "\\"))
        .append("\\").append(getViewGroup().getProperties().getPackageName())
        .append("\\").append("modular\\")
        .append(getEntity().getProperties().getPackageName()).append("\\")
        .append("api\\")
        .append(getEntity().getProperties().getWebFunctionType()).append("Controller.java")
        .toString()
        .replace("\\", "/");
  }

  public String permissionKey() {

    String modulePermission = StrUtil.toUnderlineCase(getViewGroup().getDefKey()).toLowerCase();
    String entityPermission = StrUtil.toUnderlineCase(Optional.ofNullable(getEntity().getProperties())
        .map(PDManerEntityProperty::getWebEntityVarUpperCamel).orElse("")).toLowerCase();

    return modulePermission + ":" + entityPermission + "";
  }

  public TableInfo(PDManer pdManer, GenTable table) {
    this.pdManer = pdManer;
    this.table = table;
    this.tableName = table.getTableName();
    List<GenTableColumn> tableColumnList = table.getColumns();

    // 找到要生成的表
    this.entity = pdManer.getEntities().stream().filter(e -> StrUtil.equals(tableName, e.getDefKey()))
        .findFirst().orElseThrow(() -> new RuntimeException("没有找到对应表"));

    PDManer cloneBean = ObjectUtil.cloneByStream(pdManer);
    this.entity.getFields().forEach(field -> {
      // 字段生成的时候需要 PDManer中的信息
      field.setChiner(cloneBean);
      // 匹配一下 genCol
      tableColumnList.stream().filter(c -> StrUtil.equals(c.getColumnName(), field.getDefKey()))
          .findFirst().ifPresent(field::setColumn);
    });

    // 找到表对应的模块
    this.viewGroup = pdManer.getViewGroups().stream()
        .filter(viewGroup -> viewGroup.getRefEntities().contains(entity.getId()))
        .findFirst().orElseThrow(() -> new RuntimeException("没有找到对应ViewGroup"));

    final String viewGroupCode = this.getViewGroup().getDefKey();

    List<ModuleInfo> moduleInfoList = GenUtils.getGenConfig().getModuleInfoList();
    ModuleInfo moduleInfo = moduleInfoList.stream()
        .filter(m -> StrUtil.equals(m.getModuleCode(), viewGroup.getDefKey())).findFirst()
        .orElseThrow(() -> new RuntimeException("请在 generator.yml 中配置模块[" + viewGroup.getDefKey() + "]的信息"));

    this.viewGroup.setProperties(moduleInfo);

    // 填充下必要信息

    // 检查下是否是明细表单,如果是明细表单,增加额外信息
    if (entity.isDetailTable()) {
      // 填充上级表单信息

      String detailTableInfo = entity.getProperties().getDetailTableInfo();
      List<String> tableInfoList = StrUtil.split(detailTableInfo, ",");
      String parentTableName = tableInfoList.get(0);
      String parentTableKey = tableInfoList.get(1);
      String currentTableKey = tableInfoList.get(2);

      this.parentTableKey = parentTableKey;
      this.currentTableKey = currentTableKey;

      parentEntity = pdManer.getEntities().stream().filter(e -> StrUtil.equals(parentTableName, e.getDefKey()))
          .findFirst().orElseThrow(() -> new RuntimeException("没有找到对应表"));
      this.parentEntity.getFields().forEach(f -> f.setChiner(cloneBean));

    }

  }

}
