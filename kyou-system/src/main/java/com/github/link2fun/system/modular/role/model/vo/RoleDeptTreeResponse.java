package com.github.link2fun.system.modular.role.model.vo;

import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.TreeSelect;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 角色部门树响应：已勾选的部门与可选部门树
 */
@Getter
@Setter
public class RoleDeptTreeResponse extends Resp<Void> {
  /** 已勾选的部门 id */
  private List<Long> checkedKeys;

  /** 可选部门树 */
  private List<TreeSelect> depts;
}
