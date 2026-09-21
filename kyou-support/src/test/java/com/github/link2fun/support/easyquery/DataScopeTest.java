package com.github.link2fun.support.easyquery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据范围纯数据的语义测试: 工厂 / contains 点查 / 「不放行 = 过滤到空」。
 *
 * <p>角色到范围的判定矩阵与谓词翻译需要查询器与真实数据, 由 kyou-main 的
 * DataScopeSqlTest(判定 + SQL 形状)与 DataScopeSmokeTest(查询结果)覆盖。
 */
public class DataScopeTest {

  /** 过滤到空(不放行)的作用域 */
  private static DataScope denyScope() {
    return new DataScope(DeptScope.filterByDeptIds(Set.of()), null);
  }

  @Test
  @DisplayName("部门轴不过滤: 恒可见, 包括空 ID")
  public void deptScopeIgnoreFilterSeesEverything() {
    final DeptScope deptScope = DeptScope.ignoreFilter();
    assertFalse(deptScope.shouldFilter());
    assertTrue(deptScope.contains(999L));
    assertTrue(deptScope.contains(null), "不过滤时空 ID 也可见");
    assertTrue(deptScope.visibleDeptIds().isEmpty());
  }

  @Test
  @DisplayName("部门轴白名单: 仅白名单内可见, 空 ID 不可见")
  public void deptScopeWhitelistContains() {
    final DeptScope deptScope = DeptScope.filterByDeptIds(Set.of(100L, 105L));
    assertTrue(deptScope.shouldFilter());
    assertTrue(deptScope.contains(100L));
    assertFalse(deptScope.contains(103L));
    assertFalse(deptScope.contains(null), "过滤时空 ID 不可见");
  }

  @Test
  @DisplayName("部门轴白名单防御性拷贝: 改源集合不影响作用域")
  public void deptScopeDefensiveCopy() {
    final TreeSet<Long> source = new TreeSet<>(Set.of(100L));
    final DeptScope deptScope = DeptScope.filterByDeptIds(source);
    source.add(105L);
    assertEquals(Set.of(100L), deptScope.visibleDeptIds(), "白名单应在构造时拷贝");
    assertThrows(UnsupportedOperationException.class, () -> deptScope.visibleDeptIds().add(108L),
      "白名单不可变");
  }

  @Test
  @DisplayName("空白名单 = 过滤到空, 不是不过滤")
  public void emptyWhitelistFiltersToNothing() {
    final DeptScope deptScope = DeptScope.filterByDeptIds(Set.of());
    assertTrue(deptScope.shouldFilter(), "空白名单仍要过滤(恒假), 不同于不过滤");
    assertFalse(deptScope.contains(100L));
  }

  @Test
  @DisplayName("聚合不过滤: 不加条件, 点查恒可见")
  public void ignoreFilterSeesEverything() {
    final DataScope dataScope = DataScope.ignoreFilter();
    assertFalse(dataScope.filters());
    assertTrue(dataScope.containsDept(103L));
    assertTrue(dataScope.containsUser(2L, 103L));
    assertTrue(dataScope.containsUser(null, null));
  }

  @Test
  @DisplayName("不放行 = 过滤到空: 仍算加条件, 点查全否")
  public void denyIsFilterToEmpty() {
    final DataScope dataScope = denyScope();
    assertTrue(dataScope.filters(), "不放行也是一个条件(恒假)");
    assertFalse(dataScope.containsDept(103L));
    assertFalse(dataScope.containsUser(2L, 103L));
  }

  @Test
  @DisplayName("本人轴点查: 是本人或部门在白名单内可见")
  public void containsUserCoversSelfAndDept() {
    final DataScope dataScope = new DataScope(DeptScope.filterByDeptIds(Set.of(105L)), 100L);
    assertTrue(dataScope.containsUser(100L, 103L), "本人不在白名单部门也可见");
    assertTrue(dataScope.containsUser(2L, 105L), "白名单部门内的其他用户可见");
    assertFalse(dataScope.containsUser(2L, 103L), "范围外用户不可见");
    assertFalse(dataScope.containsUser(2L, null), "查无部门(用户不存在)按不可见处理");
    assertTrue(dataScope.containsUser(null, 105L), "部门轴可见与用户 ID 无关");
    assertFalse(dataScope.containsUser(null, 103L), "空用户 ID 且部门不可见");
  }

  @Test
  @DisplayName("部门轴不可为 null, 构造时暴露")
  public void nullDeptScopeRejected() {
    assertThrows(NullPointerException.class, () -> new DataScope(null, null));
  }
}
