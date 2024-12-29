package com.github.link2fun.generator.template.pdmaner;
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
public class CodeTemplateF04DetailIndexTsx implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 52;
  }

  @Override
  public String getTemplateCode() {
    return "F04_DETAIL_INDEX_TSX";
  }

  @Override
  public String getTemplateName() {
    return "[前端] 分离详情页";
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();

    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();


    result.line("import type { Dispatch } from 'umi';");
    result.append("import type { ").append(entity.getProperties().getWebEntityType()).append(" } from '@/pages/").append(viewGroup.getProperties().getDirName()).append("/").append(entity.getProperties().getWebEntityVar()).append("/data';").lineBreak();
    result.line("import React, { useEffect, useRef, useState } from 'react';");
    result.line("import type { FormInstance} from 'antd';");
    result.line("import { Space } from 'antd';");
    result.line("import { Button, Divider, Form, message, Popconfirm, Spin } from 'antd';");
    result.line("import { closeCurrentTab, getPageQuery } from '@/utils/utils';");
    result.line("import StrTool from '@/utils/StrTool';");
    result.line("// @ts-ignore");
    result.line("import { history } from 'umi';");
    result.append("import Api").append(entity.getProperties().getWebFunctionType()).append(" from '@/services/").append(viewGroup.getProperties().getDirName()).append("/").append("Api").append(entity.getProperties().getWebFunctionType()).append("';").lineBreak();
    result.line("import WebConst from '@/utils/WebConst';");
    result.line("import ProForm, { ProFormText } from '@ant-design/pro-form';");
    result.line("import ProFormItem from '@ant-design/pro-form/es/components/FormItem';");
    result.line("import { connect } from '@@/plugin-dva/exports';");
    result.append("import type { ").append(entity.getProperties().getWebFunctionType()).append("ModelState } from '@/pages/").append(viewGroup.getProperties().getDirName()).append("/").append(entity.getProperties().getWebEntityVar()).append("/data';").lineBreak();
    result.line("import { DeleteOutlined } from '@ant-design/icons';").lineBreak().lineBreak();


    result.append("type ").append(entity.getProperties().getWebFunctionType()).append("DetailIndexProps = {").lineBreak()
      .tab_2().append("dispatch: Dispatch;").lineBreak()
      .tab_2().append("currentModel: Partial<").append(entity.getProperties().getWebEntityType()).append(">;").lineBreak()
      .line("}")
      .lineBreak()
      .lineBreak();


    result.append("const ").append(entity.getProperties().getWebFunctionType()).append("DetailIndex: React.FC<").append(entity.getProperties().getWebFunctionType()).append("DetailIndexProps> = (").lineBreak()
      .tab_2().append("{ dispatch, currentModel }").lineBreak()
      .append(") => {").lineBreak();


    result.append("  const [").append(entity.getProperties().getWebEntityVar()).line("Form] = Form.useForm();")
      .line("  const formRef = useRef<FormInstance>();")
      .line("  const urlQuery = getPageQuery();");

    result.line("  const { mode, ").append(entity.getProperties().getWebEntityVar()).append("Id } = urlQuery;");


    result.line("  const [loading, setLoading] = useState<boolean>(true);");
    result.line("  const [closeTabSignal, setCloseTabSignal] = useState<boolean>(false);");


    // 保存方法
    result.line("  const handleSave = (values: any) => {").lineBreak()
      .line("    if (mode !== WebConst.ActionType.EDIT) {")
      .line("      // 直接不处理")
      .line("      return Promise.resolve();")
      .line("    }")
      .lineBreak()
      .line("    setLoading(true);")

      .append("    if (StrTool.isBlank(").append(entity.getProperties().getWebEntityVar()).append("Id as string)) {").lineBreak()
      .append("      // 新增").lineBreak()
      .append("      return Api").append(entity.getProperties().getWebFunctionType()).append(".add").append(entity.getProperties().getWebEntityVarUpperCamel()).append("(values)").lineBreak()
      .line("        .then((id: string) => {")
      .append("          return message.success('新增").append(entity.getDefName()).append("成功', 1)").lineBreak()
      .append("            .then(() => setCloseTabSignal(true))").lineBreak()
      .append("            .then(() => {").lineBreak()
      .line("                setLoading(false);").lineBreak()
      .line("                history.push(`/" + viewGroup.getProperties().getDirName() + "/" + entity.getProperties().getWebEntityVar() + "View?" + entity.getProperties().getWebEntityVar() + "Id=${id}&mode=${WebConst.ActionType.VIEW}`);")
      .line("          });")
      .line("        });")
      .line("    } else {")
      .line("      // 修改")
      .append("      return Api" + entity.getProperties().getWebFunctionType() + ".modify" + entity.getProperties().getWebEntityVarUpperCamel() + "(values)")
      .line("        .then((id: string) => {")
      .line("          return message.success('修改" + entity.getDefName() + "成功', 1)").lineBreak()
      .append("            .then(() => setCloseTabSignal(true))").lineBreak()
      .append("            .then(() => {").lineBreak()
      .line("                setLoading(false);").lineBreak()
      .line("            history.push(`/" + viewGroup.getProperties().getDirName() + "/" + entity.getProperties().getWebEntityVar() + "View?" + entity.getProperties().getWebEntityVar() + "Id=${id}&mode=${WebConst.ActionType.VIEW}`);")
      .line("          });")
      .line("        });")
      .line("    }")
      .line("  };");

    // 删除方法
    result.lineBreak()
      .line("  const handleDel = () => {")
      .line("    setLoading(true);")
      .line("    Api" + entity.getProperties().getWebFunctionType() + ".del" + entity.getProperties().getWebEntityVarUpperCamel() + "(" + entity.getProperties().getWebEntityVar() + "Id as string)")
      .line("      .then(() => message.success('删除" + entity.getDefName() + "成功', 1)).then(() => {")
      .line("      setCloseTabSignal(true);")
      .line("    });")
      .line("  };");

    result.line("  const handleCloseTab = () => {");
    result.line("    closeCurrentTab(dispatch);");
    result.line("  };");
    result.line("");
    result.line("  useEffect(() => {");
    result.line("    if (closeTabSignal) {");
    result.line("      closeCurrentTab(dispatch);");
    result.line("");
    result.line("    }");
    result.line("  }, [closeTabSignal]);");


    result.line("  useEffect(() => {");
    result.line("    if (" + entity.getProperties().getWebEntityVar() + "Id) {");
    result.line("      Api" + entity.getProperties().getWebFunctionType() + ".select" + entity.getProperties().getWebEntityVarUpperCamel() + "(" + entity.getProperties().getWebEntityVar() + "Id as string).then((bill) => {");
    result.line("        " + entity.getProperties().getWebEntityVar() + "Form.setFieldsValue({ ...bill });");
    result.line("        dispatch({");
    result.line("          type: '" + entity.getProperties().getWebFunctionVar() + "/saveCurrentModel',");
    result.line("          payload: {");
    result.line("            ...bill,");
    result.line("          },");
    result.line("        });");
    result.line("        setLoading(false);");
    result.line("      });");
    result.line("    } else {");
    result.line("      dispatch({");
    result.line("        type: '" + entity.getProperties().getWebFunctionVar() + "/saveCurrentModel',");
    result.line("        payload: {},");
    result.line("      });");
    result.line("      " + entity.getProperties().getWebEntityVar() + "Form.setFieldsValue({});");
    result.line("      setLoading(false);");
    result.line("    }");
    result.line("  }, [" + entity.getProperties().getWebEntityVar() + "Id, mode]);");
    result.line("");
    result.line("");
    result.line("  return (<div>");
    result.line("    <Spin spinning={loading || false}>");
    result.line("      <Space size={16}>");
    result.line("");
    result.line("        <Button onClick={handleCloseTab}>关闭</Button>");
    result.line("        {mode === WebConst.ActionType.EDIT && (");
    result.line("          <Button");
    result.line("            type='primary'");
    result.line("            onClick={() => {");
    result.line("              " + entity.getProperties().getWebEntityVar() + "Form");
    result.line("                .validateFields()");
    result.line("                .then((values) => {");
    result.line("                  handleSave({ ...currentModel, ...values });");
    result.line("                })");
    result.line("                .catch(() => {");
    result.line("                });");
    result.line("            }}");
    result.line("          >");
    result.line("            保存");
    result.line("          </Button>");
    result.line("        )}");
    result.line("        {mode === WebConst.ActionType.VIEW && (");
    result.line("          <>");
    result.line("            <Button");
    result.line("              onClick={() => {");
    result.line("                history.push(");
    result.line("                  `/" + viewGroup.getProperties().getDirName() + "/" + entity.getProperties().getWebEntityVar() + "Edit?" + entity.getProperties().getWebEntityVar() + "Id=${" + entity.getProperties().getWebEntityVar() + "Id}&mode=${WebConst.ActionType.EDIT}`,");
    result.line("                );");
    result.line("              }}");
    result.line("            >");
    result.line("              修改");
    result.line("            </Button>");
    result.line("            <Popconfirm");
    result.line("              title='删除后无法恢复，请确认是否继续？'");
    result.line("              okText='确认'");
    result.line("              cancelText='再看看'");
    result.line("              onConfirm={() => handleDel()}");
    result.line("            >");
    result.line("              <Button danger icon={<DeleteOutlined />}>");
    result.line("                删除");
    result.line("              </Button>");
    result.line("            </Popconfirm>");
    result.line("");
    result.line("          </>");
    result.line("        )}");
    result.line("");
    result.line("      </Space>");
    result.line("      <Divider />");
    result.line("      <ProForm form={" + entity.getProperties().getWebEntityVar() + "Form} formRef={formRef} omitNil={false}");
    result.line("               labelCol={{ span: 4 }} wrapperCol={{ span: 8 }} layout={'horizontal'}");
    result.line("               submitter={false} preserve={false}");
    result.line("               onFinish={(values: any) => handleSave(values)}");
    result.line("      >");
    result.line("");
    result.line("        {mode === WebConst.ActionType.VIEW && (");
    result.line("          <>");

    entity.getFields().stream().filter(PDManerEntityField::isInAddReq)
      .forEach(field -> {
        result.line("            <ProFormItem label='" + field.getDefName() + "'>{currentModel?." + field.fieldName() + " || ''}</ProFormItem>");
      });

    result.line("");
    result.line("          </>");
    result.line("        )}");
    result.line("");
    result.line("        {mode === WebConst.ActionType.EDIT && (");
    result.line("          <>");
    entity.getFields().stream().filter(PDManerEntityField::isInAddReq)
      .forEach(field -> {
        if (StrUtil.equals(field.getFieldTsType(), "number")) {

          result.append("          <ProFormDigit label={\"" + field.getDefName() + "\"} name={\"" + field.fieldName() + "\"} ");
          if (field.getNotNull()) {
            result.append(" rules={[{ required: true, message: '请输入" + field.getDefName() + "' }]} ");
          }
          result.append("/>").lineBreak();

        } else if (StrUtil.equalsAny(field.getFieldJavaType(), "LocalDateTime")) {
          result.append("          <ProFormDateTimePicker label={\"" + field.getDefName() + "\"} name={\"" + field.fieldName() + "\"} ");
          if (field.getNotNull()) {
            result.append(" rules={[{ required: true, message: '请输入" + field.getDefName() + "' }]} ");
          }
          result.append("/>").lineBreak();
        } else if (StrUtil.equalsAny(field.getFieldJavaType(), "LocalDate")) {
          result.append("          <ProFormDatePicker label={\"" + field.getDefName() + "\"} name={\"" + field.fieldName() + "\"} ");
          if (field.getNotNull()) {
            result.append(" rules={[{ required: true, message: '请输入" + field.getDefName() + "' }]} ");
          }
          result.append("/>").lineBreak();
        } else if (StrUtil.equals(field.getFieldTsType(), "string")) {

          result.append("          <ProFormText label={\"" + field.getDefName() + "\"} name={\"" + field.fieldName() + "\"} ");
          if (field.getNotNull()) {
            result.append(" rules={[{ required: true, message: '请输入" + field.getDefName() + "' }]} ");
          }
          result.append("/>").lineBreak();
        }
      });
    result.line("          </>");
    result.line("        )}");
    result.line("      </ProForm>");
    result.line("    </Spin>");
    result.line("");
    result.line("  </div>);");
    result.line("};");
    result.line("");
    result.line("");
    result.line("// noinspection JSUnusedGlobalSymbols");
    result.line("export default connect(");
    result.line("  ({");
    result.line("     " + entity.getProperties().getWebFunctionVar() + ",");
    result.line("   }: {");
    result.line("    " + entity.getProperties().getWebFunctionVar() + ": " + entity.getProperties().getWebFunctionType() + "ModelState;");
    result.line("  }) => ({");
    result.line("    pagination: " + entity.getProperties().getWebFunctionVar() + ".pagination,");
    result.line("    currentModel: " + entity.getProperties().getWebFunctionVar() + ".currentModel,");
    result.line("  }),");
    result.line(")(" + entity.getProperties().getWebFunctionType() + "DetailIndex);");


    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String subPackage = tableInfo.getViewGroup().getProperties().getDirName() + "/" + tableInfo.getEntity().getProperties().getWebEntityVar();
    File javaRoot = new File(GenUtils.getGenConfig().getFrontendLocation(), "pages");
    File fileDir = new File(javaRoot, subPackage.replace("\\.", "/"));

    File file = new File(fileDir, "" + tableInfo.getEntity().getProperties().getWebFunctionType() + "DetailIndex.tsx");

    return file.getAbsolutePath();
  }
}
