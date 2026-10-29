package com.github.link2fun.system.modular.dept.api;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.dto.SysDeptDTO;
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
  public Resp<List<SysDeptDTO>> list(SysDeptSearchReq searchReq) {

    final ActionContext context = ActionContext.current();
    List<SysDeptDTO> deptList = deptService.selectDeptList(context, searchReq, SysDeptDTO.class);

    return Resp.successData(deptList);
  }

  /**
   * 查询部门列表（排除节点）
   */
  @SaCheckPermission("system:dept:list")
  @Mapping(value = "/list/exclude/{deptId}", method = MethodType.GET)
  public Resp<List<SysDeptDTO>> excludeChild(@Path(value = "deptId") Long deptId) {
    return Resp.successData(deptService.selectDeptListExcludingSubtree(ActionContext.current(), deptId));
  }

  /**
   * 根据部门编号获取详细信息
   */

  @SaCheckPermission("system:dept:query")
  @Mapping(value = "/{deptId}", method = MethodType.GET)
  public Resp<SysDeptDTO> getInfo(@Path Long deptId) {
    return Resp.successData(deptService.selectDeptDetail(ActionContext.current(), deptId));
  }

  /**
   * 新增部门
   */
  @SaCheckPermission("system:dept:add")
  @Log(title = "部门管理", businessType = BusinessType.INSERT, mask = true)
  @Mapping(method = MethodType.POST)
  public Resp<Void> add(@Validated @Body SysDeptAddReq addReq) {
    return Resp.ofAffected(deptService.insertDept(addReq));
  }

  /**
   * 修改部门
   */
  @SaCheckPermission("system:dept:edit")
  @Log(title = "部门管理", businessType = BusinessType.UPDATE, mask = true)
  @Mapping(method = MethodType.PUT)
  public Resp<Void> edit(@Validated @Body SysDeptModifyReq modifyReq) {
    return Resp.ofAffected(deptService.updateDept(ActionContext.current(), modifyReq));
  }

  /**
   * 删除部门
   */
  @SaCheckPermission("system:dept:remove")
  @Log(title = "部门管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{deptId}", method = MethodType.DELETE)
  public Resp<Void> remove(@Path Long deptId) {
    return Resp.ofAffected(deptService.deleteDeptById(ActionContext.current(), deptId));
  }
}
