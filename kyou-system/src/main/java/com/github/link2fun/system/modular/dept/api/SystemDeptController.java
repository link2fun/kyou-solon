package com.github.link2fun.system.modular.dept.api;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.system.modular.dept.model.req.SysDeptAddReq;
import com.github.link2fun.system.modular.dept.model.req.SysDeptModifyReq;
import com.github.link2fun.system.modular.dept.model.req.SysDeptSearchReq;
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
public class SystemDeptController {
  @Inject
  private ISystemDeptService deptService;

  /**
   * 获取部门列表
   */
  @SaCheckPermission("system:dept:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public AjaxResult list(SysDeptSearchReq searchReq) {

    final ActionContext context = ActionContext.current();
    List<SysDept> deptList = deptService.selectDeptList(context, searchReq);

    return AjaxResult.successData(deptList);
  }

  /**
   * 查询部门列表（排除节点）
   */
  @SaCheckPermission("system:dept:list")
  @Mapping(value = "/list/exclude/{deptId}", method = MethodType.GET)
  public AjaxResult excludeChild(@Path(value = "deptId") Long deptId) {
    return AjaxResult.successData(deptService.selectDeptListExcludingSubtree(ActionContext.current(), deptId));
  }

  /**
   * 根据部门编号获取详细信息
   */

  @SaCheckPermission("system:dept:query")
  @Mapping(value = "/{deptId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path Long deptId) {
    return AjaxResult.successData(deptService.selectDeptDetail(ActionContext.current(), deptId));
  }

  /**
   * 新增部门
   */
  @SaCheckPermission("system:dept:add")
  @Log(title = "部门管理", businessType = BusinessType.INSERT, mask = true)
  @Mapping(method = MethodType.POST)
  public AjaxResult add(@Validated @Body SysDeptAddReq addReq) {
    return AjaxResult.ofAffected(deptService.insertDept(addReq));
  }

  /**
   * 修改部门
   */
  @SaCheckPermission("system:dept:edit")
  @Log(title = "部门管理", businessType = BusinessType.UPDATE, mask = true)
  @Mapping(method = MethodType.PUT)
  public AjaxResult edit(@Validated @Body SysDeptModifyReq modifyReq) {
    return AjaxResult.ofAffected(deptService.updateDept(ActionContext.current(), modifyReq));
  }

  /**
   * 删除部门
   */
  @SaCheckPermission("system:dept:remove")
  @Log(title = "部门管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{deptId}", method = MethodType.DELETE)
  public AjaxResult remove(@Path Long deptId) {
    return AjaxResult.ofAffected(deptService.deleteDeptById(ActionContext.current(), deptId));
  }
}
