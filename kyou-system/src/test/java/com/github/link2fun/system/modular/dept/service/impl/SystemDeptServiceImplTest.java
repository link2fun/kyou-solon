package com.github.link2fun.system.modular.dept.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 部门环路判定与父链改写：ancestors 必须按逗号切分比较，不能退化成字符串包含。
 */
class SystemDeptServiceImplTest {

  @Test
  @DisplayName("上级链中命中自己的部门ID时判定为环")
  void detectsCycle() {
    assertTrue(SystemDeptServiceImpl.containsAncestor("0,100,101", 100L));
    assertTrue(SystemDeptServiceImpl.containsAncestor("0,100,101", 101L));
  }

  @Test
  @DisplayName("前缀相同的其他ID不能被误判")
  void doesNotMatchByPrefix() {
    assertFalse(SystemDeptServiceImpl.containsAncestor("0,10,1001", 1L));
    assertFalse(SystemDeptServiceImpl.containsAncestor("0,10,1001", 100L));
  }

  @Test
  @DisplayName("空链与空ID不构成环")
  void handlesBlankInput() {
    assertFalse(SystemDeptServiceImpl.containsAncestor(null, 1L));
    assertFalse(SystemDeptServiceImpl.containsAncestor("", 1L));
    assertFalse(SystemDeptServiceImpl.containsAncestor("0,100", null));
  }

  @Test
  @DisplayName("部门移到根节点时，整棵子树的 ancestors 去掉旧父链")
  void rebaseChildrenWhenMovedToRoot() {
    // 5 号部门从 0,100 移到根下，它自己和子孙都要跟着改
    assertEquals("0,5", SystemDeptServiceImpl.rebaseAncestors("0,100,5", "0,100", "0"));
    assertEquals("0,5,101", SystemDeptServiceImpl.rebaseAncestors("0,100,5,101", "0,100", "0"));
  }

  @Test
  @DisplayName("部门挂到新父节点时，整棵子树的 ancestors 换成新父链")
  void rebaseChildrenWhenMovedUnderNewParent() {
    assertEquals("0,200,300,5", SystemDeptServiceImpl.rebaseAncestors("0,100,5", "0,100", "0,200,300"));
  }

  @Test
  @DisplayName("父链未变或缺失时子孙的 ancestors 保持原值")
  void rebaseKeepsValueWhenNothingToDo() {
    assertEquals("0,5", SystemDeptServiceImpl.rebaseAncestors("0,5", "0", "0"));
    assertEquals("0,5", SystemDeptServiceImpl.rebaseAncestors("0,5", "", "0,9"));
    assertNull(SystemDeptServiceImpl.rebaseAncestors(null, "0", "0,9"));
  }
}
