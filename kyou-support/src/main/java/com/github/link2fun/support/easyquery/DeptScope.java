package com.github.link2fun.support.easyquery;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 部门轴作用域: 不过滤(全量部门), 或按可见部门白名单过滤(空白名单 = 过滤到空)。
 * <p>
 * 白名单是已解析的具体部门 ID 集合, 由 {@link DataScope#of} 生产;
 * 应用时只剩 {@code in(白名单)} 一种翻译, 不再携带子查询形态。
 */
public final class DeptScope implements Serializable {

  /** 不过滤的全局单例 */
  private static final DeptScope IGNORE_FILTER = new DeptScope(false, Set.of());

  /** 是否过滤 */
  private final boolean filter;

  /** 可见部门 ID 白名单(不可变); 不过滤时为空集 */
  private final Set<Long> deptIds;

  /** 私有构造: 白名单只经工厂方法设置 */
  private DeptScope(final boolean filter, final Set<Long> deptIds) {
    this.filter = filter;
    this.deptIds = deptIds;
  }

  /** 不执行过滤(全量部门)。 */
  public static DeptScope ignoreFilter() {
    return IGNORE_FILTER;
  }

  /** 按可见部门白名单过滤(空集合 = 过滤到空)。 */
  public static DeptScope filterByDeptIds(final Collection<Long> deptIds) {
    return new DeptScope(true, Collections.unmodifiableSet(new LinkedHashSet<>(deptIds)));
  }

  /** 是否执行过滤 */
  public boolean shouldFilter() {
    return filter;
  }

  /** 可见部门 ID 白名单(不过滤时为空集) */
  public Set<Long> visibleDeptIds() {
    return deptIds;
  }

  /** 指定部门是否可见: 不过滤恒可见; 过滤时空 ID 不可见, 仅白名单内可见。 */
  public boolean contains(final Long deptId) {
    return !filter || (deptId != null && deptIds.contains(deptId));
  }
}
