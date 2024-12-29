package com.github.link2fun.generator.template.pdmaner;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.generator.domain.pdmaner.TableInfo;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntity;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntityField;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.PDManerViewGroup;
import com.github.link2fun.generator.template.CodeTemplate;
import com.github.link2fun.generator.util.CodeBuilder;
import com.github.link2fun.generator.util.GenUtils;
import org.noear.solon.annotation.Component;

import java.io.File;

@Component
public class CodeTemplateF08RefSelect implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 58;
  }

  @Override
  public String getTemplateCode() {
    return "F08_REF_SELECT";
  }

  @Override
  public String getTemplateName() {
    return "F08_REF_SELECT";
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();

    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();

    result.line("import React, { useEffect, useState } from 'react';");
    result.line("import { Select } from 'antd';");
    result.line("import type { " + entity.getProperties().getWebEntityType() + " } from '@/pages/" + viewGroup.getProperties().getDirName() + "/" + entity.getProperties().getWebEntityVar() + "/data';");
    result.line("import Api" + entity.getProperties().getWebFunctionType() + " from '@/services/" + viewGroup.getProperties().getDirName() + "/Api" + entity.getProperties().getWebFunctionType() + "';");
    result.line("");
    result.line("interface " + entity.getProperties().getWebFunctionType() + "SelectProps {");
    result.line("  value?: string;");
    result.line("  onChange?: (value: string) => void;");
    result.line("}");
    result.line("");
    result.line("const " + entity.getProperties().getWebFunctionType() + "Select: React.FC<" + entity.getProperties().getWebFunctionType() + "SelectProps> = ({ value, onChange }) => {");
    result.line("  const [selectDs, setSelectDs] = useState<" + entity.getProperties().getWebEntityType() + "[]>([]);");
    result.line("");
    result.line("  useEffect(() => {");
    result.line("    Api" + entity.getProperties().getWebFunctionType() + ".pageSearch" + entity.getProperties().getWebEntityVarUpperCamel() + "({ pageNum: 1, pageSize: 1000 }).then((data: any) => {");
    result.line("      setSelectDs(data.records);");
    result.line("    }).catch(() => {");
    result.line("    });");
    result.line("  }, []);");
    result.line("");
    result.line("  return (");
    result.line("    <Select");
    result.line("      value={value}");
    result.line("      onChange={onChange}");
    result.line("      allowClear");
    result.line("      style={{ minWidth: 120 }}");
    result.line("      placeholder='请选择" + entity.getDefName() + "'");
    result.line("    >");
    result.line("      {selectDs.map((item: " + entity.getProperties().getWebEntityType() + ") => (");
    result.line("        <Select.Option value={item.id} key={item.id}>{item." + entity.getProperties().getWebEntityVar() + "Name}</Select.Option>");
    result.line("      ))}");
    result.line("    </Select>");
    result.line("  );");
    result.line("};");
    result.line("");
    result.line("export default " + entity.getProperties().getWebFunctionType() + "Select;");

    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String subPackage = tableInfo.getViewGroup().getProperties().getDirName() + "/" + tableInfo.getEntity().getProperties().getWebEntityVar() + "/references";
    File javaRoot = new File(GenUtils.getGenConfig().getFrontendLocation(), "pages");
    File fileDir = new File(javaRoot, subPackage.replace("\\.", "/"));

    File file = new File(fileDir, "" + tableInfo.getEntity().getProperties().getWebFunctionType() + "Select.tsx");

    return file.getAbsolutePath();
  }
}
