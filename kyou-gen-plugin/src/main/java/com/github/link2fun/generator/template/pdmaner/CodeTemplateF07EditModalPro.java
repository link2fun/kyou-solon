package com.github.link2fun.generator.template.pdmaner;

import cn.hutool.core.util.StrUtil;
import com.github.link2fun.generator.domain.GenTableColumn;
import com.github.link2fun.generator.domain.pdmaner.TableInfo;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntity;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntityField;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.PDManerViewGroup;
import com.github.link2fun.generator.template.CodeTemplate;
import com.github.link2fun.generator.util.CodeBuilder;
import com.github.link2fun.generator.util.GenUtils;
import org.noear.solon.annotation.Component;

import java.io.File;
import java.util.stream.Collectors;

@Component
public class CodeTemplateF07EditModalPro implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 55;
  }

  @Override
  public String getTemplateCode() {
    return "F07_EDIT_MODAL";
  }

  @Override
  public String getTemplateName() {
    return "[前端] 编辑弹窗";
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();
    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();

    // 导入声明
    result.line("import EditModalForm from '@/components/EditModalForm';")
      .line("import { EditModalProps } from '@/typing';")
      .append("import { ");
    String componentsImport = entity.getFields().stream().map(PDManerEntityField::getColumn).map(GenTableColumn::getHtmlType)
      .filter(StrUtil::isNotBlank)
      .map(GenUtils::getAntDesignProComponentImport)
      .distinct()
      .filter(StrUtil::isNotBlank)
      .sorted()
      .collect(Collectors.joining(", "));
    result.append(componentsImport);


    result.append(" } from '@ant-design/pro-components';").lineBreak();

    // 如果有数据字典的话, 要引入 hooks
    if (entity.getFields().stream().map(PDManerEntityField::getColumn).map(GenTableColumn::getDictType).anyMatch(StrUtil::isNotBlank)) {
      result.line("import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';");
    }


    result.line("import React from 'react';");

    // 组件声明
    result.lineBreak()
      .line(
        "const ", entity.getProperties().getWebFunctionType(), "EditModal: React.FC<EditModalProps> = (props) => {")
      .tab_2().line("return (")
      .tab_2().tab_2().line("<EditModalForm {...props}>");

    // 字段声明
    for (PDManerEntityField field : entity.getFields()) {
      // 如果没有编辑框则不显示
      String htmlType = field.getColumn().getHtmlType();
      if (StrUtil.isBlank(htmlType)) {
        continue;
      }
      // 如果不是编辑或新增字段, 则不显示
      if (!field.isInAddReq() && !field.isInModifyReq()) {
        continue;
      }

      // 现在是需要进行显示的
      if (StrUtil.equalsAnyIgnoreCase(htmlType, "textarea")) {
        result.tab_2().tab_2().tab_2().line("<ProFormTextArea")
          .tab_2().tab_2().tab_4().line("label={'", field.getDefName(), "'}")
          .tab_2().tab_2().tab_4().line("name={'" + field.fieldName() + "'}");
        // 文本域默认占一行
        result.tab_2().tab_2().tab_4().line("labelCol={{ span: 3 }}");
      } else if (StrUtil.equalsAnyIgnoreCase(htmlType, "select")) {
        // <ProFormSelect
        result.tab_2().tab_2().tab_2().line("<ProFormSelect")
          .tab_2().tab_2().tab_4().line("label={'" + field.getDefName() + "'}")
          .tab_2().tab_2().tab_4().line("name={'" + field.fieldName() + "'}");
        if (StrUtil.isNotBlank(field.getColumn().getDictType())) {
          // 如果是字典类型的话, 直接用字典的Request
          // request={useProFormSelectDictRequest({ typeCode: 'sys_normal_disable' })}
          result.tab_2().tab_2().tab_4().line("request={useProFormSelectDictRequest({ typeCode: '" + field.getColumn().getDictType() + "' })}");
        } else {
          // todo select 要初始化 options, 如果不是字典的话, 应该是需要手动初始化一下的
        }

      } else if (StrUtil.equalsAnyIgnoreCase(htmlType, "radio")) {

        result.tab_2().tab_2().tab_2().line("<ProFormRadio.Group")
          .tab_2().tab_2().tab_4().line("label={'" + field.getDefName() + "'}")
          .tab_2().tab_2().tab_4().line("name={'" + field.fieldName() + "'}")
          .tab_2().tab_2().tab_4().line("valueEnum={{  }}"); // todo 枚举取值待确认
      } else if (StrUtil.equalsAnyIgnoreCase(htmlType, "checkbox")) {

        result.tab_2().line("<ProFormCheckbox.Group")
          .tab_2().tab_2().tab_4().line("label={'" + field.getDefName() + "'}")
          .tab_2().tab_2().tab_4().line("name={'" + field.fieldName() + "'}")
          .tab_2().tab_2().tab_4().line("options={[]}"); // todo options 取值待确认
      } else if (StrUtil.equalsAnyIgnoreCase(htmlType, "date")) {
        // <ProFormDatePicker
        result.tab_2().tab_2().tab_2().line("<ProFormDatePicker")
          .tab_2().tab_2().tab_4().line("label={'" + field.getDefName() + "'}")
          .tab_2().tab_2().tab_4().line("name={'" + field.fieldName() + "'}");
      } else if (StrUtil.equalsAnyIgnoreCase(htmlType, "datetime")) {
        // ProFormDateTimePicker
        result.tab_2().tab_2().tab_2().line("<ProFormDateTimePicker")
          .tab_2().tab_2().tab_4().line("label={'" + field.getDefName() + "'}")
          .tab_2().tab_2().tab_4().line("name={'" + field.fieldName() + "'}");
      }
      /// 分割下
      else {
        result.tab_2().tab_2().tab_2().line("<ProFormText")
          .tab_2().tab_2().tab_4().line("label={'" + field.getDefName() + "'}")
          .tab_2().tab_2().tab_4().line("name={'" + field.fieldName() + "'}");
      }


      if (field.getNotNull()) {
        result.tab_2().tab_2().tab_4().line("rules={[{ required: true, message: '请输入" + field.getDefName() + "！' }]}");
      }

      result.tab_2().tab_2().tab_2().line("/>");
    }


    // 结尾
    result.tab_2().tab_2().line("</EditModalForm>")
      .tab_2().line(");")
      .line("};")
      .line()
      .line("export default " + entity.getProperties().getWebFunctionType() + "EditModal;");


    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String subPackage = tableInfo.getViewGroup().getProperties().getDirName() + "/"
      + tableInfo.getEntity().getProperties().getWebEntityVar() + "/components";
    File javaRoot = new File(GenUtils.getGenConfig().getFrontendLocation(), "pages");
    File fileDir = new File(javaRoot, subPackage.replace("\\.", "/"));

    File file = new File(fileDir, "" + tableInfo.getEntity().getProperties().getWebFunctionType() + "EditModal.tsx");

    return file.getAbsolutePath();
  }
}
