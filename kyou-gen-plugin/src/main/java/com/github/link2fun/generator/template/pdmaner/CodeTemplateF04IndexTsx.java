package com.github.link2fun.generator.template.pdmaner;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.generator.domain.pdmaner.TableInfo;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntity;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.PDManerViewGroup;
import com.github.link2fun.generator.template.CodeTemplate;
import com.github.link2fun.generator.util.CodeBuilder;
import com.github.link2fun.generator.util.GenUtils;
import org.noear.solon.annotation.Component;

import java.io.File;

@Component
public class CodeTemplateF04IndexTsx implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 52;
  }

  @Override
  public String getTemplateCode() {
    return "F04_INDEX_TSX";
  }

  @Override
  public String getTemplateName() {
    return "[前端] 列表页";
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();

    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();


    result.line("import ActionControlAddButton from '@/components/BizButtons/ActionControlAddButton';");
    result.line("import PermissionButton from '@/components/BizButtons/PermissionButton';");
    result.line("import PopconfirmButton from '@/components/BizButtons/PopconfirmButton';");
    result.line("import TableRowDelButton from '@/components/BizButtons/TableRowDelButton';");
    result.line("import TableRowEditButton from '@/components/BizButtons/TableRowEditButton';");
    result.line("import EllipsisText from '@/components/EllipsisText';");
    result.line("import useActionControl from '@/hooks/useActionControl';");
    result.line("import React, { useEffect } from 'react';");


    result.line("import { DeleteOutlined, ExportOutlined } from '@ant-design/icons';");
    result.line("import { ProTable } from '@ant-design/pro-components';");

    result.lineTemplate("const {}Index = () => {", entity.functionType());
    result.line("  const actionControl = useActionControl({");
    result.line("    editAction: {");
    result.lineTemplate("      onModalOpen: (values) => Api{}.detail(values.{}),", entity.functionType(), entity.primaryKeyVar());
    result.lineTemplate("      onActionCall: (values) => Api{}.edit(values),", entity.functionType());
    result.line("    },");
    result.line("    addAction: {");
    result.lineTemplate("      onActionCall: (values) => Api{}.add(values),", entity.functionType());
    result.line("    },");
    result.line("    removeAction: {");
    result.lineTemplate("      onActionCall: (values) => Api{}.remove(values.{}),", entity.functionType(), entity.primaryKeyVar());
    result.line("    },");
    result.line("  });");
    result.lineBreak();

    result.line("  return (");
    result.line("    <div>");
    result.line("      <ProTable");
    result.line("        {...actionControl.table}");
    result.line("        request={async (_params: any) => {");
    result.line("          const params = actionControl.queryParams.wrapPageTotal(_params);");
    result.lineBreak();

    result.lineTemplate("          const data = await Api{}.list(params);", entity.functionType());
    result.line("          return {");
    result.line("            data: data.rows,");
    result.line("            success: true,");
    result.line("            total: data.total,");
    result.line("          };");
    result.line("        }}");
    result.line("        toolBarRender={() => [");
    result.line("          <ActionControlAddButton");
    result.line("            key={'add'}");
    result.line("            actionControl={actionControl}");
    result.lineTemplate("            permissionsRequired={['{}:add']}", tableInfo.permissionKey());
    result.line("          />,");
    result.line("          <PopconfirmButton");
    result.line("            key={'export'}");
    result.lineTemplate("            permissionsRequired={['{}:export']}", tableInfo.permissionKey());
    result.line("            icon={<ExportOutlined />}");
    result.line("            loading={actionControl.loading.value}");
    result.line("            onClick={() =>");
    result.line("              actionControl.loading.wrap({");
    result.lineTemplate("                action: () => Api{}.export({}),", entity.functionType());
    result.line("                loadingMessage: '数据导出中，请稍后...',");
    result.line("            })");
    result.line("            }");
    result.line("          >");
    result.line("            导出");
    result.line("          </PopconfirmButton>,");
    result.line("        ]}");
    result.line("        scroll={{ x: 'max-content' }}");
    result.lineTemplate("        rowKey={'{}'}", entity.primaryKeyVar());
    result.line("        columns={[");

    entity.getFields().forEach(field -> {
      if (!Convert.toBool(field.getColumn().getIsList(),false) && !field.isInSearchReq()) {
        // 不是列表字段且不在查询里面不显示
        return;
      }
      //          {
      result.line("          {");
      result.lineTemplate("            title: '{}',", field.getDefName());
      result.lineTemplate("            dataIndex: '{}',", field.fieldName());
      if (!Convert.toBool(field.getColumn().getIsList())) {
        result.line("            hideInTable: true,");
      }
      if (!field.isInSearchReq() || field.isEasyQueryTypeSpecial()) {
        // 不在查询参数里面, 或者是特殊类型的查询参数
        result.line("            hideInSearch: true,");
      }
      // 如果类型不是 input 的话 要设置一下 valueType
      if (!StrUtil.equalsAnyIgnoreCase(field.getColumn().getHtmlType(), "input", "textarea")) {
        result.lineTemplate("            valueType: '{}',", field.getColumn().getHtmlType());
      }
      // 如果是字典类型
      if (StrUtil.isNotBlank(field.getColumn().getDictType())) {
        // request: useProFormSelectDictRequest({
        result.line("            request: useProFormSelectDictRequest({");
        //              typeCode: 'sys_notice_type',
        result.lineTemplate("              typeCode: '{}',", field.getColumn().getDictType());
        //            }),
        result.line("            }),");
      }


      result.line("          },");

    });

    result.line("        {");
    result.line("          title: '操作',");
    result.line("          fixed: 'right',");
    result.line("          width: actionControl.rowAction.width,");
    result.line("          render: (_, record) => {");
    result.line("            return (");
    result.line("              <div ref={actionControl.rowAction.ref}>");
    result.line("                <TableRowEditButton");
    result.line("                  actionControl={actionControl}");
    result.line("                  record={record}");
    result.lineTemplate("                  permissionsRequired={['{}:edit']}", tableInfo.permissionKey());
    result.line("                />");
    result.line("                <TableRowDelButton");
    result.line("                  actionControl={actionControl}");
    result.line("                  record={record}");
    result.lineTemplate("                  permissionsRequired={['{}:remove']}", tableInfo.permissionKey());
    result.line("                />");
    result.line("              </div>");
    result.line("            );");
    result.line("          },");
    result.line("        },");
    result.line("        ]}");
    result.line("      />");
    result.lineTemplate("      <{}EditModal {...actionControl.editModal} />", entity.functionType());
    result.line("    </div>");
    result.line("  );");
    result.line("};");
    result.lineBreak();
    result.lineTemplate("export default {}Index;", entity.functionType());


    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String subPackage = tableInfo.getViewGroup().getProperties().getDirName() + "/" + tableInfo.getEntity().getProperties().getWebEntityVar();
    File javaRoot = new File(GenUtils.getGenConfig().getFrontendLocation(), "pages");
    File fileDir = new File(javaRoot, subPackage.replace("\\.", "/"));

    File file = new File(fileDir, "" + tableInfo.getEntity().getProperties().getWebFunctionType() + "Index.tsx");

    return file.getAbsolutePath();
  }
}
