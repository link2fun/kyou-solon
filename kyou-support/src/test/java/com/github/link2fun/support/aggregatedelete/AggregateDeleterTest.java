package com.github.link2fun.support.aggregatedelete;

import com.github.link2fun.support.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AggregateDeleter 的纯逻辑测试：用记录调用序列的假步骤替代真实守卫/解绑/删除,
 * 覆盖固定顺序、守卫短路、id 归一化与空集合语义。
 * 各 modular 声明的 EasyQuery 写法由 kyou-main 的冒烟测试覆盖。
 */
public class AggregateDeleterTest {

  private final AggregateDeleter deleter = new AggregateDeleter();

  private final List<String> calls = new ArrayList<>();

  @Test
  @DisplayName("守卫先于解绑, 解绑先于删除, 槽位内保持声明顺序")
  void executesSlotsInFixedOrder() {
    final long rows = deleter.delete(AggregateDeletePlan.of(List.of(1L, 2L))
      .guard(ids -> calls.add("guard-1"))
      .guard(ids -> calls.add("guard-2"))
      .unlink(ids -> calls.add("unlink-1"))
      .unlink(ids -> calls.add("unlink-2"))
      .delete(ids -> {
        calls.add("delete");
        return 2L;
      }));

    assertEquals(Arrays.asList("guard-1", "guard-2", "unlink-1", "unlink-2", "delete"), calls);
    assertEquals(2L, rows);
  }

  @Test
  @DisplayName("任一守卫抛出即短路, 后续守卫、解绑与删除都不执行")
  void guardThrowShortCircuits() {
    assertThrows(ServiceException.class, () ->
      deleter.delete(AggregateDeletePlan.of(List.of(1L))
        .guard(ids -> calls.add("guard-1"))
        .guard(ids -> {
          throw new ServiceException("已被占用");
        })
        .guard(ids -> calls.add("guard-3"))
        .unlink(ids -> calls.add("unlink"))
        .delete(ids -> {
          calls.add("delete");
          return 1L;
        })));

    assertEquals(List.of("guard-1"), calls);
  }

  @Test
  @DisplayName("id 集合先归一化：去重、剔除 null, 守卫与删除拿到同一份")
  void normalizesIdsBeforeExecution() {
    final List<Long> seenByGuard = new ArrayList<>();
    final List<Long> seenByDelete = new ArrayList<>();

    deleter.delete(AggregateDeletePlan.of(Arrays.asList(1L, null, 2L, 1L))
      .guard(ids -> seenByGuard.addAll(ids))
      .delete(ids -> {
        seenByDelete.addAll(ids);
        return 0L;
      }));

    assertEquals(List.of(1L, 2L), seenByGuard);
    assertEquals(List.of(1L, 2L), seenByDelete);
  }

  @Test
  @DisplayName("归一化后为空的 id 集合不产生任何调用, 返回 0")
  void emptyIdsProduceNoWrites() {
    final long nullRows = deleter.delete(AggregateDeletePlan.of(null)
      .guard(ids -> calls.add("guard"))
      .unlink(ids -> calls.add("unlink"))
      .delete(ids -> {
        calls.add("delete");
        return 1L;
      }));
    final long allNullRows = deleter.delete(AggregateDeletePlan.of(Arrays.asList(null, null))
      .delete(ids -> {
        calls.add("delete");
        return 1L;
      }));

    assertEquals(0L, nullRows);
    assertEquals(0L, allNullRows);
    assertTrue(calls.isEmpty(), "空集合不应触发任何步骤");
  }

  @Test
  @DisplayName("guardPerId 对归一化后的每个 id 各执行一次")
  void guardPerIdRunsOncePerId() {
    final AtomicInteger executions = new AtomicInteger();
    final List<Long> seen = new ArrayList<>();

    deleter.delete(AggregateDeletePlan.of(Arrays.asList(1L, 2L, 2L, null))
      .guardPerId(id -> {
        executions.incrementAndGet();
        seen.add(id);
      })
      .delete(ids -> 0L));

    assertEquals(2, executions.get());
    assertEquals(List.of(1L, 2L), seen);
  }

  @Test
  @DisplayName("缺少删除步骤时抛出业务异常")
  void missingDeleteStepThrows() {
    assertThrows(ServiceException.class, () ->
      deleter.delete(AggregateDeletePlan.of(List.of(1L))
        .guard(ids -> calls.add("guard"))));
    assertTrue(calls.isEmpty(), "删除步骤缺失时守卫不应执行");
  }
}
