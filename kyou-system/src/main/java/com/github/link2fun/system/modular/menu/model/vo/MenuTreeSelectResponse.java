package com.github.link2fun.system.modular.menu.model.vo;

import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.TreeSelect;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 角色菜单树响应：已勾选的菜单与可选菜单树
 */
@Getter
@Setter
public class MenuTreeSelectResponse extends Resp<Void> {
  /** 已勾选的菜单 id */
  private List<String> checkedKeys;

  /** 可选菜单树 */
  private List<TreeSelect> menus;
}
