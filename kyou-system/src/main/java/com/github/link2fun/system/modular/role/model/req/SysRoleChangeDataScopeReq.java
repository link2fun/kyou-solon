package com.github.link2fun.system.modular.role.model.req;


import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

import java.util.List;

/** 修改角色数据权限请求 */
@Data
public class SysRoleChangeDataScopeReq {

  @NotNull(message = "角色ID不能为空")
  private Long roleId;

  @NotBlank(message = "数据权限不能为空")
  private String dataScope;

  private List<Long> deptIds;

  private String remark;
}
