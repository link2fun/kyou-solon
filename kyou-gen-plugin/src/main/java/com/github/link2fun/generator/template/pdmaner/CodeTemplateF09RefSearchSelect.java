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
public class CodeTemplateF09RefSearchSelect implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 55;
  }

  @Override
  public String getTemplateCode() {
    return "F09_REF_SEARCH_SELECT";
  }

  @Override
  public String getTemplateName() {
    return "F09_REF_SEARCH_SELECT";
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();

    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();


    result.line("import React, { useEffect, useState } from 'react';");
    result.line("import { Select, Spin } from 'antd';");
    result.line("import type { " + entity.getProperties().getWebEntityType() + " } from '@/pages/" + viewGroup.getProperties().getDirName() + "/" + entity.getProperties().getWebEntityVar() + "/data';");
    result.line("import Api" + entity.getProperties().getWebFunctionType() + " from '@/services/" + viewGroup.getProperties().getDirName() + "/Api" + entity.getProperties().getWebFunctionType() + "';");
    result.line("");


    result.line("type " + entity.getProperties().getWebFunctionType() + "SelectProps = {");
    result.line("  value?: string;");
    result.line("  onChange?: (value: string) => void;");
    result.line("  allowClear?: boolean;");
    result.line("  mode?: string | string[];");
    result.line("  datasource?: " + entity.getProperties().getWebEntityType() + "[]");
    result.line("  valueField?: string;");
    result.line("}");
    result.line("");


    result.line("");
    result.line("const " + entity.getProperties().getWebFunctionType() + "SearchSelect: React.FC<" + entity.getProperties().getWebFunctionType() + "SelectProps> = ({ ")
      .tab_4().line("value, ")
      .tab_4().line("onChange,")
      .tab_4().line("allowClear,")
      .tab_4().line("mode = 'edit',")
      .tab_4().line("datasource,")
      .tab_4().line("valueField = 'id',")
      .line(" }) => {");
    result.line("  const [apiLoading, setApiLoading] = useState(false);");
    result.line("  const [selectDs, setSelectDs] = useState<" + entity.getProperties().getWebEntityType() + "[]>([]);");
    result.line("");

    result.line("  useEffect(() => {");
    result.line("    if (datasource&&datasource?.length > 0) {");
    result.line("      setSelectDs(datasource);");
    result.line("    }");
    result.line("  }, [datasource]);");
    result.lineBreak().lineBreak();


    result.line("  const load" + entity.getProperties().getWebEntityVarUpperCamel() + " = (keyword: string) => {");
    result.line("    setApiLoading(true);");
    result.line("    Api" + entity.getProperties().getWebFunctionType() + ".pageSearch" + entity.getProperties().getWebEntityVarUpperCamel() + "({ keyword, pageNum: 1, pageSize: 1000 }).then((data) => {");
    result.line("      setSelectDs(data.records);");
    result.line("    }).catch(() => {");
    result.line("    })");
    result.line("      .finally(() => setApiLoading(false));");
    result.line("  };");
    result.line("");
    result.line("  useEffect(() => {");
    result.line("");
    result.line("    if (value && (!selectDs || selectDs.length < 1)) {");
    result.line("      if(valueField === 'id') {");
    result.line("        Api" + entity.getProperties().getWebFunctionType() + ".select" + entity.getProperties().getWebEntityVarUpperCamel() + "(value)");
    result.line("          .then((" + entity.getProperties().getWebEntityVar() + ": " + entity.getProperties().getWebEntityType() + ") => {");
    result.line("            setSelectDs([" + entity.getProperties().getWebEntityVar() + "]);");
    result.line("          }).catch(() => {");
    result.line("          });");
    result.line("      } else {");
    result.line("        load" + entity.getProperties().getWebEntityVarUpperCamel() + "('');");
    result.line("      }");
    result.line("    } else if (!value) {");
    result.line("      load" + entity.getProperties().getWebEntityVarUpperCamel() + "('');");
    result.line("    }");
    result.line("    // eslint-disable-next-line react-hooks/exhaustive-deps");
    result.line("  }, [value]);");
    result.line("");


    result.line("  if (mode === 'read') {");
    result.line("    if (value && selectDs.length === 1 && selectDs[0][valueField] === value) {");
    result.line("      return (<>{selectDs[0].typeCode} - {selectDs[0].typeName}</>);");
    result.line("    }").lineBreak();
    result.line("    return (<>-</>)").lineBreak();
    result.line("  }").lineBreak();


    result.line("  return (");
    result.line("    <Select");
    result.line("      allowClear={allowClear}");
    result.line("      showSearch");
    result.line("      value={value}");
    result.line("      placeholder='请选择" + entity.getDefName() + "'");
    result.line("      notFoundContent={apiLoading ? <Spin size={'small'} /> : <>请输入关键字查询" + entity.getDefName() + "</>}");
    result.line("      filterOption={false}");
    result.line("      onSearch={load" + entity.getProperties().getWebEntityVarUpperCamel() + "}");
    result.line("      onChange={(val: string) => {");
    result.line("        if (onChange) {");
    result.line("          onChange(val);");
    result.line("        }");
    result.line("      }");
    result.line("      }");
    result.line("      style={{ width: '100%', minWidth: '200px' }}");
    result.line("    >");
    result.line("      {selectDs.map((item: " + entity.getProperties().getWebEntityType() + ") => (");
    result.line("        <Select.Option key={`" + entity.getProperties().getWebEntityVarUpperCamel() + "Select-${item[valueField]}`} value={item[valueField]}>{item.typeCode} - {item.typeName}</Select.Option>");
    result.line("      ))}");
    result.line("    </Select>");
    result.line("  );");
    result.line("};");
    result.line("");
    result.line("export default " + entity.getProperties().getWebFunctionType() + "SearchSelect;");

    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String subPackage = tableInfo.getViewGroup().getProperties().getDirName() + "/" + tableInfo.getEntity().getProperties().getWebEntityVar() + "/references";
    File javaRoot = new File(GenUtils.getGenConfig().getFrontendLocation(), "pages");
    File fileDir = new File(javaRoot, subPackage.replace("\\.", "/"));

    File file = new File(fileDir, "" + tableInfo.getEntity().getProperties().getWebFunctionType() + "SearchSelect.tsx");

    return file.getAbsolutePath();
  }
}
