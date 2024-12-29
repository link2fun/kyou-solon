package com.github.link2fun.support.core.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysMenu;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TreeSelect 树结构实体类
 *
 * @author ruoyi
 */
@Setter
@Getter
@SuppressWarnings("unused")
public class TreeSelect implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 节点ID */
  @JsonSerialize(using = ToStringSerializer.class)
  private Long id;

  /** 节点名称 */
  private String label;

  /** 子节点 */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<TreeSelect> children;

  public TreeSelect() {

  }

  public TreeSelect(SysDept dept) {
    this.id = dept.getDeptId();
    this.label = dept.getDeptName();
    this.children = dept.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
  }

  public TreeSelect(SysMenu menu) {
    this.id = menu.getMenuId();
    this.label = menu.getMenuName();
    this.children = menu.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
  }

}
