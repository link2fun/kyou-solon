package com.github.link2fun.support.core.tree;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 平铺节点列表组装成树的通用工具: 按「父 id → 子节点列表」一次分组,
 * 每个节点查表写回自己的子节点, 不递归、O(n); id 类型不限(仅依赖 equals/hashCode)。
 *
 * @author link2fun
 */
public final class TreeBuilder {

  private TreeBuilder() {
  }

  /**
   * 组装树: 父节点不在节点集合中的(含父 id 为 null)视为顶级。
   *
   * @param nodes          平铺节点列表
   * @param idGetter       节点自身 id
   * @param parentIdGetter 节点父 id
   * @param childrenSetter 子节点列表写回节点
   * @return 顶级节点列表(各级子节点已挂好); 组不出树(如互为父子成环)时原样返回兜底
   */
  public static <T, K> List<T> build(final List<T> nodes,
                                     final Function<T, K> idGetter,
                                     final Function<T, K> parentIdGetter,
                                     final BiConsumer<T, List<T>> childrenSetter) {
    final Map<K, List<T>> childrenByParentId = groupByParentId(nodes, parentIdGetter);
    final Set<K> nodeIds = nodes.stream().map(idGetter).collect(Collectors.toSet());

    final List<T> roots = new ArrayList<>();
    for (final T node : nodes) {
      final K parentId = parentIdGetter.apply(node);
      if (Objects.isNull(parentId) || !nodeIds.contains(parentId)) {
        roots.add(node);
      }
    }
    // 一次遍历挂接: 每个节点查表拿到自己的子节点列表写回, 与遍历顺序无关
    attachAll(nodes, idGetter, childrenByParentId, childrenSetter);

    if (roots.isEmpty()) {
      return nodes;
    }
    return roots;
  }

  /**
   * 组装树: 以「父 id 等于指定根父 id」的节点为顶级。
   *
   * <p>与 {@link #build} 的差别: 父节点不在集合中的孤儿节点不会升级为顶级(路由树语义)。
   *
   * @param nodes          平铺节点列表
   * @param rootParentId   顶级节点的父 id(通常为 0)
   * @param idGetter       节点自身 id
   * @param parentIdGetter 节点父 id
   * @param childrenSetter 子节点列表写回节点
   * @return 顶级节点列表(各级子节点已挂好)
   */
  public static <T, K> List<T> buildByRootParent(final List<T> nodes, final K rootParentId,
                                                 final Function<T, K> idGetter,
                                                 final Function<T, K> parentIdGetter,
                                                 final BiConsumer<T, List<T>> childrenSetter) {
    final Map<K, List<T>> childrenByParentId = groupByParentId(nodes, parentIdGetter);
    final List<T> roots = nodes.stream()
      .filter(n -> Objects.equals(parentIdGetter.apply(n), rootParentId))
      .collect(Collectors.toList());
    attachAll(nodes, idGetter, childrenByParentId, childrenSetter);
    return roots;
  }

  /** 每个节点把「以自己 id 为父 id」的分组写回, 无剩余时写回空列表 */
  private static <T, K> void attachAll(final List<T> nodes,
                                       final Function<T, K> idGetter,
                                       final Map<K, List<T>> childrenByParentId,
                                       final BiConsumer<T, List<T>> childrenSetter) {
    for (final T node : nodes) {
      childrenSetter.accept(node, childrenByParentId.getOrDefault(idGetter.apply(node), List.of()));
    }
  }

  /** 按父 id 分组(容忍 null 父 id), 保持原有相对顺序 */
  private static <T, K> Map<K, List<T>> groupByParentId(final List<T> nodes,
                                                        final Function<T, K> parentIdGetter) {
    final Map<K, List<T>> childrenByParentId = new HashMap<>();
    for (final T node : nodes) {
      childrenByParentId.computeIfAbsent(parentIdGetter.apply(node), key -> new ArrayList<>()).add(node);
    }
    return childrenByParentId;
  }
}
