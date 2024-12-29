package com.github.link2fun.generator.template.pdmaner;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.link2fun.generator.domain.pdmaner.TableInfo;
import com.github.link2fun.generator.template.CodeTemplate;
import com.github.link2fun.generator.util.CodeBuilder;
import org.noear.solon.annotation.Component;

import java.io.File;

@Component
public class CodeTemplateMenuSQL implements CodeTemplate {


  /**
   * 模板的优先级
   *
   * @return 模板的优先级
   */
  @Override
  public Integer getTemplateIndex() {
    return 99;
  }


  @Override
  public String getTemplateCode() {
    return "MENU_SQL";
  }

  /**
   * 获取模板名称
   *
   * @return 模板名称
   */
  @Override
  public String getTemplateName() {
    return "菜单SQL";
  }

  /**
   * 生成菜单sql
   *
   * @param tableInfo 表信息
   * @return 生成的菜单sql
   */
  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();
    JSONObject tableOptions = JSONUtil.parseObj(tableInfo.getTable().getOptions());
    String parentMenuId = tableOptions.getStr("parentMenuId");

    String functionName = tableInfo.getTable().getFunctionName();
    String businessName = tableInfo.getTable().getBusinessName();
    String moduleName = tableInfo.getTable().getModuleName();

    //-- 菜单 SQL
    result.line("-- 菜单 SQL");
    result.line("insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)");

    //values('${functionName}', '${parentMenuId}', '1', '${businessName}', '${moduleName}/${businessName}/index', 1, 0, 'C', '0', '0', '${permissionPrefix}:list', '#', 'admin', sysdate(), '', null, '${functionName}菜单');
    String permissionPrefix = tableInfo.permissionKey();
    result.lineTemplate("values('{}', '{}', '1', '{}', '{}/{}/index', 1, 0, 'C', '0', '0', '{}:list', '#', 'admin', sysdate(), '', null, '{}菜单');",
        functionName, parentMenuId, businessName, moduleName, businessName, permissionPrefix, functionName)
      .lineBreak();


    //
    //-- 按钮父菜单ID
    result.line("-- 按钮父菜单ID");
    //SELECT @parentId := LAST_INSERT_ID();
    result.line("SELECT @parentId := LAST_INSERT_ID();").lineBreak();
    //
    //-- 按钮 SQL
    result.line("-- 按钮 SQL");
    //insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    result.line("insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)");
    //values('${functionName}查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', '${permissionPrefix}:query',        '#', 'admin', sysdate(), '', null, '');
    result.lineTemplate("values('{}查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', '{}:query',        '#', 'admin', sysdate(), '', null, '');",
        functionName, permissionPrefix)
      .lineBreak();

    //
    //insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    result.line("insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)");
    //values('${functionName}新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', '${permissionPrefix}:add',          '#', 'admin', sysdate(), '', null, '');
    result.lineTemplate("values('{}新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', '{}:add',          '#', 'admin', sysdate(), '', null, '');",
        functionName, permissionPrefix)
      .lineBreak();

    //
    //insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    result.line("insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)");

    //values('${functionName}修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', '${permissionPrefix}:edit',         '#', 'admin', sysdate(), '', null, '');
    result.lineTemplate("values('{}修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', '{}:edit',         '#', 'admin', sysdate(), '', null, '');",
        functionName, permissionPrefix)
      .lineBreak();
    //
    //insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    result.line("insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)");
    //values('${functionName}删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', '${permissionPrefix}:remove',       '#', 'admin', sysdate(), '', null, '');
    result.lineTemplate("values('{}删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', '{}:remove',       '#', 'admin', sysdate(), '', null, '');",
        functionName, permissionPrefix)
      .lineBreak();
    //
    //insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    result.line("insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)");
    //values('${functionName}导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', '${permissionPrefix}:export',       '#', 'admin', sysdate(), '', null, '');
    result.lineTemplate("values('{}导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', '{}:export',       '#', 'admin', sysdate(), '', null, '');",
        functionName, permissionPrefix)
      .lineBreak();


    

    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String businessName = tableInfo.getTable().getBusinessName();
    String moduleName = tableInfo.getTable().getModuleName();
    return String.format("sql/%s/%s_menu.sql", moduleName, businessName);
  }
}
