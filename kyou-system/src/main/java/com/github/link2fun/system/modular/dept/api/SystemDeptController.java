package com.github.link2fun.system.modular.dept.api;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.collection.CollectionUtil;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.system.modular.dept.service.ISystemDeptService;

import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;


/**
 * 部门信息
 *
 * @author ruoyi
 */
@Controller
@Mapping("/system/dept")
public class SystemDeptController extends BaseController {
  @Inject
  private ISystemDeptService deptService;

  /**
   * 获取部门列表
   */
  @SaCheckPermission("system:dept:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public AjaxResult list(SysDept dept) {

    final ActionContext context = ActionContext.current();
    List<SysDept> deptList = deptService.selectDeptList(context,dept);

    return successData(deptList);
  }

  /**
   * 查询部门列表（排除节点）
   */
  @SaCheckPermission("system:dept:list")
  @Mapping(value = "/list/exclude/{deptId}", method = MethodType.GET)
  public AjaxResult excludeChild(@Path(value = "deptId") Long deptId) {

    List<SysDept> deptList = deptService.selectDeptList(ActionContext.current(), new SysDept());
    deptList.removeIf(d -> d.getDeptId().intValue() == deptId || CollectionUtil.contains(StringUtils.split(d.getAncestors(), ","), deptId + ""));
    return successData(deptList);
  }

  /**
   * 根据部门编号获取详细信息
   */

  @SaCheckPermission("system:dept:query")
  @Mapping(value = "/{deptId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path Long deptId) {
    final ActionContext context = ActionContext.current();
    deptService.checkDeptDataScope(context,deptId);
    return successData(deptService.selectDeptById(deptId));
  }

  /**
   * 新增部门
   */
  @SaCheckPermission("system:dept:add")
  @Log(title = "部门管理", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public AjaxResult add(@Validated @Body SysDept dept) {
    dept.setCreateBy(getUsername());
    return toAjax(deptService.insertDept(dept));
  }

  /**
   * 修改部门
   */
  @SaCheckPermission("system:dept:edit")
  @Log(title = "部门管理", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public AjaxResult edit(@Validated @Body SysDept dept) {
    dept.setUpdateBy(getUsername());
    return toAjax(deptService.updateDept(ActionContext.current(), dept));
  }

  /**
   * 删除部门
   */
  @SaCheckPermission("system:dept:remove")
  @Log(title = "部门管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{deptId}", method = MethodType.DELETE)
  public AjaxResult remove(@Path Long deptId) {
    return toAjax(deptService.deleteDeptById(ActionContext.current(), deptId));
  }
}
