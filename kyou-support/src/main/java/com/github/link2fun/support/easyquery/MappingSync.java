package com.github.link2fun.support.easyquery;

import cn.hutool.core.collection.CollectionUtil;
import com.github.link2fun.support.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Transaction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 关联表同步器：让宿主的关联集合与期望的目标集合一致。
 *
 * <p>约定：
 * <ul>
 *   <li>{@code targetIds} 为 {@code null} 或空集合时，{@link #sync} 视同清空全部关联</li>
 *   <li>重复元素先去重，{@code null} 元素忽略</li>
 *   <li>事务为 required 语义：外层已有事务时合并，没有时自己开一个</li>
 * </ul>
 *
 * @author link2fun
 */
@Slf4j
@Component
public class MappingSync {

  /**
   * 双向同步：宿主的关联集合最终与 targetIds 完全一致, 多退少补
   *
   * @param ops       目标关联表的操作集
   * @param ownerId   宿主 ID
   * @param targetIds 期望关联的目标 ID 集合, null 或空集合表示清空
   */
  @Transaction
  public void sync(final MappingOps ops, final Long ownerId, final Collection<Long> targetIds) {
    checkOwnerId(ownerId);

    final Set<Long> desired = normalize(targetIds);
    final Set<Long> existing = currentTargets(ops, ownerId);

    if (desired.isEmpty()) {
      if (!existing.isEmpty()) {
        log.debug("[关联表同步] 目标集合为空, 清空全部关联, ownerId={}", ownerId);
        ops.unlinkAll(List.of(ownerId));
      }
      return;
    }

    final Set<Long> toLink = difference(desired, existing);
    if (!toLink.isEmpty()) {
      log.debug("[关联表同步] 建立关联, ownerId={}, targetIds={}", ownerId, toLink);
      ops.link(ownerId, toLink);
    }

    final Set<Long> toUnlink = difference(existing, desired);
    if (!toUnlink.isEmpty()) {
      log.debug("[关联表同步] 解除关联, ownerId={}, targetIds={}", ownerId, toUnlink);
      ops.unlink(ownerId, toUnlink);
    }
  }

  /**
   * 单向追加：只补上尚不存在的关联, 不解除任何已有关系
   *
   * @param ops       目标关联表的操作集
   * @param ownerId   宿主 ID
   * @param targetIds 要追加的目标 ID 集合
   */
  @Transaction
  public void add(final MappingOps ops, final Long ownerId, final Collection<Long> targetIds) {
    checkOwnerId(ownerId);

    final Set<Long> desired = normalize(targetIds);
    if (desired.isEmpty()) {
      return;
    }

    final Set<Long> toLink = difference(desired, currentTargets(ops, ownerId));
    if (!toLink.isEmpty()) {
      log.debug("[关联表同步] 追加关联, ownerId={}, targetIds={}", ownerId, toLink);
      ops.link(ownerId, toLink);
    }
  }

  /** 宿主 ID 为空时抛业务异常 */
  private void checkOwnerId(final Long ownerId) {
    if (Objects.isNull(ownerId)) {
      throw new ServiceException("关联表同步的宿主 ID 不能为空");
    }
  }

  /** 去重并剔除 null 元素; null 集合视同空集合 */
  private Set<Long> normalize(final Collection<Long> targetIds) {
    if (CollectionUtil.isEmpty(targetIds)) {
      return Set.of();
    }
    return targetIds.stream()
      .filter(Objects::nonNull)
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /** 读取宿主的现有目标 ID */
  private Set<Long> currentTargets(final MappingOps ops, final Long ownerId) {
    final List<Long> existing = ops.findTargetIds(ownerId);
    if (CollectionUtil.isEmpty(existing)) {
      return Set.of();
    }
    return existing.stream()
      .filter(Objects::nonNull)
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /** present 中不在 absent 里的元素 */
  private Set<Long> difference(final Set<Long> present, final Set<Long> absent) {
    final Set<Long> result = new LinkedHashSet<>(present);
    result.removeAll(absent);
    return result;
  }
}
