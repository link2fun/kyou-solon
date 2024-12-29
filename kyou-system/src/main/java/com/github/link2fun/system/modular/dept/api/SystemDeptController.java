package com.github.link2fun.system.modular.dept.api;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.collection.CollectionUtil;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.constant.UserConstants;
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

    return success(deptList);
  }

  /**
   * 查询部门列表（排除节点）
   */
  @SaCheckPermission("system:dept:list")
  @Mapping(value = "/list/exclude/{deptId}", method = MethodType.GET)
  public AjaxResult excludeChild(@Path(value = "deptId") Long deptId) {

    List<SysDept> deptList = deptService.selectDeptList(ActionContext.current(), new SysDept());
    deptList.removeIf(d -> d.getDeptId().intValue() == deptId || CollectionUtil.contains(StringUtils.split(d.getAncestors(), ","), deptId + ""));
    return success(deptList);
  }

  /**
   * 根据部门编号获取详细信息
   */

  @SaCheckPermission("system:dept:query")
  @Mapping(value = "/{deptId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path Long deptId) {
    final ActionContext context = ActionContext.current();
    deptService.checkDeptDataScope(context,deptId);
    return success(deptService.selectDeptById(deptId));
  }

  /**
   * 新增部门
   */
  @SaCheckPermission("system:dept:add")
  @Log(title = "部门管理", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public AjaxResult add(@Validated @Body SysDept dept) {
    if (!deptService.checkDeptNameUnique(dept)) {
      return error("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
    }
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
    Long deptId = dept.getDeptId();
    deptService.checkDeptDataScope(ActionContext.current(), deptId);
    if (!deptService.checkDeptNameUnique(dept)) {
      return error("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
    } else if (dept.getParentId().equals(deptId)) {
      return error("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
    } else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus()) && deptService.selectNormalChildrenDeptById(deptId) > 0) {
      return error("该部门包含未停用的子部门！");
    }
    dept.setUpdateBy(getUsername());
    return toAjax(deptService.updateDept(dept));
  }

  /**
   * 删除部门
   */
  @SaCheckPermission("system:dept:remove")
  @Log(title = "部门管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{deptId}", method = MethodType.DELETE)
  public AjaxResult remove(@Path Long deptId) {
    if (deptService.hasChildByDeptId(deptId)) {
      return warn("存在下级部门,不允许删除");
    }
    if (deptService.checkDeptExistUser(deptId)) {
      return warn("部门存在用户,不允许删除");
    }

    deptService.checkDeptDataScope(ActionContext.current(), deptId);
    return toAjax(deptService.deleteDeptById(deptId));
  }
}
