package com.github.link2fun.generator.template.pdmaner;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.generator.domain.pdmaner.TableInfo;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntity;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntityField;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.PDManerViewGroup;
import com.github.link2fun.generator.template.CodeTemplate;
import com.github.link2fun.generator.util.CodeBuilder;
import org.noear.solon.annotation.Component;

@Component
public class CodeTemplateF20MenuConfig implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 60;
  }

  @Override
  public String getTemplateCode() {
    return "F20_MENU_CONFIG";
  }

  @Override
  public String getTemplateName() {
    return "F20_MENU_CONFIG";
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();

    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();


    result.line("            {");
    result.line("              path: '/" + viewGroup.getProperties().getDirName() + "',");
    result.line("              name: '" + viewGroup.getDefName() + "',");
    result.line("              icon: 'smile',");
    result.line("              // permissionRequired: [");
    result.line("              //   'Menu" + entity.getProperties().getWebFunctionType() + "',");
    result.line("              // ],");
    result.line("              routes: [");
    result.line("                {");
    result.line("                  path: '/" + viewGroup.getProperties().getDirName() + "/" + entity.getProperties().getWebEntityVar() + "',");
    result.line("                  name: '" + entity.getDefName() + "',");
    result.line("                  icon: 'smile',");
    result.line("                  component: './" + viewGroup.getProperties().getDirName() + "/" + entity.getProperties().getWebEntityVar() + "/" + entity.getProperties().getWebFunctionType() + "Index',");
    result.line("                  // permissionRequired: ['Menu" + entity.getProperties().getWebFunctionType() + "'],");
    result.line("                },");
    result.line("                {");
    result.line("                  path: '/" + viewGroup.getProperties().getDirName() + "/" + entity.getProperties().getWebEntityVar() + "Edit',");
    result.line("                  name: '" + entity.getDefName() + "编辑',");
    result.line("                  icon: 'smile',");
    result.line("                  component: './" + viewGroup.getProperties().getDirName() + "/" + entity.getProperties().getWebEntityVar() + "/" + entity.getProperties().getWebFunctionType() + "DetailIndex',");
    result.line("                  hideInMenu: true,");
    result.line("                },");
    result.line("                {");
    result.line("                  path: '/" + viewGroup.getProperties().getDirName() + "/" + entity.getProperties().getWebEntityVar() + "View',");
    result.line("                  name: '" + entity.getDefName() + "详情',");
    result.line("                  icon: 'smile',");
    result.line("                  component: './" + viewGroup.getProperties().getDirName() + "/" + entity.getProperties().getWebEntityVar() + "/" + entity.getProperties().getWebFunctionType() + "DetailIndex',");
    result.line("                  hideInMenu: true,");
    result.line("                },");
    result.line("                {");
    result.line("                  component: './exception/404',");
    result.line("                },");
    result.line("              ],");
    result.line("            },");


    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    return StrUtil.EMPTY;
  }
}
