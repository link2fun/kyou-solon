package com.github.link2fun.support.aggregatedelete;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Transaction;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 聚合删除编排器：持有「守卫 → 解绑 → 删除」的固定顺序与事务。
 *
 * <p>约定：
 * <ul>
 *   <li>任一守卫抛出即终止，解绑与删除不会执行（短路）</li>
 *   <li>归一化后的 id 集合为空时不产生任何写操作，返回 0</li>
 *   <li>事务为 required 语义：外层已有事务时合并，没有时自己开一个</li>
 * </ul>
 *
 * @author link2fun
 */
@Slf4j
@Component
public class AggregateDeleter {

  /**
   * 按计划执行聚合删除
   *
   * @param plan 删除计划：守卫 → 解绑 → 删除
   * @return 删除步骤返回的受影响行数
   */
  @Transaction
  public long delete(final AggregateDeletePlan plan) {
    // 先校验计划完整性再执行, 缺少删除步骤时守卫不白跑
    final Function<List<Long>, Long> deleteStep = plan.deleteStep();

    final List<Long> ids = plan.ids();
    if (ids.isEmpty()) {
      log.debug("[聚合删除] id 集合为空, 跳过");
      return 0;
    }

    for (final Consumer<List<Long>> guard : plan.guards()) {
      guard.accept(ids);
    }

    for (final Consumer<List<Long>> unlink : plan.unlinks()) {
      unlink.accept(ids);
    }

    final long rows = deleteStep.apply(ids);
    log.debug("[聚合删除] 完成, ids={}, rows={}", ids, rows);
    return rows;
  }
}
