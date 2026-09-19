package com.github.link2fun.support.easyquery;

import com.github.link2fun.support.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MappingSync 的纯逻辑测试：用假 adapter 替代真实关联表, 覆盖差集、去重与空集合语义。
 * adapter 各自的 EasyQuery 写法由 kyou-main 的 MappingSyncSmokeTest 覆盖。
 */
public class MappingSyncTest {

  private final MappingSync mappingSync = new MappingSync();

  private final FakeMappingOps ops = new FakeMappingOps();

  @Test
  @DisplayName("目标集合为 null 时清空全部关联")
  void syncNullTargetsClearsEverything() {
    ops.existing = List.of(1L, 2L, 3L);

    mappingSync.sync(ops, 100L, null);

    assertEquals(List.of(100L), ops.unlinkedAllOwners);
    assertTrue(ops.linked.isEmpty(), "清空时不应再有建立关联的调用");
    assertTrue(ops.unlinked.isEmpty(), "清空走 unlinkAll, 不应逐条 unlink");
  }

  @Test
  @DisplayName("目标集合为空集合时清空全部关联")
  void syncEmptyTargetsClearsEverything() {
    ops.existing = List.of(1L, 2L);

    mappingSync.sync(ops, 100L, List.of());

    assertEquals(List.of(100L), ops.unlinkedAllOwners);
  }

  @Test
  @DisplayName("目标与现有一致时不产生任何写操作")
  void syncUnchangedProducesNoWrites() {
    ops.existing = List.of(1L, 2L, 3L);

    mappingSync.sync(ops, 100L, List.of(3L, 2L, 1L));

    assertTrue(ops.linked.isEmpty(), "无变更不应插入");
    assertTrue(ops.unlinked.isEmpty(), "无变更不应删除");
    assertTrue(ops.unlinkedAllOwners.isEmpty(), "无变更不应全删");
  }

  @Test
  @DisplayName("只新增缺失的关联")
  void syncOnlyAddsMissingTargets() {
    ops.existing = List.of(1L, 2L);

    mappingSync.sync(ops, 100L, List.of(1L, 2L, 3L));

    assertEquals(List.of(List.of(3L)), ops.linked);
    assertTrue(ops.unlinked.isEmpty());
  }

  @Test
  @DisplayName("只删除多余的关联")
  void syncOnlyRemovesExtraTargets() {
    ops.existing = List.of(1L, 2L, 3L);

    mappingSync.sync(ops, 100L, List.of(1L));

    assertEquals(List.of(List.of(2L, 3L)), ops.unlinked);
    assertTrue(ops.linked.isEmpty());
  }

  @Test
  @DisplayName("增删混合时两条路径分别只处理差集")
  void syncHandlesBothDirections() {
    ops.existing = List.of(1L, 2L, 3L);

    mappingSync.sync(ops, 100L, List.of(3L, 4L, 5L));

    assertEquals(List.of(List.of(4L, 5L)), ops.linked, "只插入新增的目标");
    assertEquals(List.of(List.of(1L, 2L)), ops.unlinked, "只删除多余的目标");
  }

  @Test
  @DisplayName("重复的目标元素先去重, 不会触发重复插入")
  void syncDeduplicatesTargets() {
    ops.existing = List.of();

    mappingSync.sync(ops, 100L, List.of(1L, 1L, 2L, 1L));

    assertEquals(List.of(List.of(1L, 2L)), ops.linked);
  }

  @Test
  @DisplayName("目标集合内的 null 元素被忽略")
  void syncIgnoresNullElements() {
    ops.existing = List.of();

    mappingSync.sync(ops, 100L, Arrays.asList(1L, null, 2L));

    assertEquals(List.of(List.of(1L, 2L)), ops.linked);
  }

  @Test
  @DisplayName("宿主 ID 为 null 时抛出业务异常")
  void syncRejectsNullOwnerId() {
    assertThrows(ServiceException.class, () -> mappingSync.sync(ops, null, List.of(1L)));
  }

  @Test
  @DisplayName("删除多余关联时只产生一次批量调用, 不逐条删除")
  void syncDeletesInOneBatch() {
    ops.existing = List.of(1L, 2L, 3L, 4L, 5L);

    mappingSync.sync(ops, 100L, List.of());

    assertEquals(1, ops.unlinkedAllOwners.size(), "全删只调用一次");
    assertTrue(ops.unlinked.isEmpty());
  }

  @Test
  @DisplayName("add 只补缺失的关联, 不解除任何已有关系")
  void addOnlyLinksMissing() {
    ops.existing = List.of(1L, 2L);

    mappingSync.add(ops, 100L, List.of(2L, 3L, 4L));

    assertEquals(List.of(List.of(3L, 4L)), ops.linked);
    assertTrue(ops.unlinked.isEmpty(), "add 不应删除");
    assertTrue(ops.unlinkedAllOwners.isEmpty());
  }

  @Test
  @DisplayName("add 遇到已在关联中的目标时不产生写操作")
  void addAlreadyLinkedProducesNoWrites() {
    ops.existing = List.of(1L, 2L, 3L);

    mappingSync.add(ops, 100L, List.of(1L, 2L));

    assertTrue(ops.linked.isEmpty());
  }

  @Test
  @DisplayName("add 的目标集合为 null 或空时不产生写操作")
  void addEmptyTargetsProducesNoWrites() {
    ops.existing = List.of(1L);

    mappingSync.add(ops, 100L, null);
    mappingSync.add(ops, 100L, List.of());

    assertTrue(ops.linked.isEmpty());
    assertTrue(ops.unlinkedAllOwners.isEmpty(), "add 不是清空语义");
  }

  /** 记录调用序列的假 adapter */
  private static final class FakeMappingOps implements MappingOps {

    private List<Long> existing = List.of();
    private final List<List<Long>> linked = new ArrayList<>();
    private final List<List<Long>> unlinked = new ArrayList<>();
    private final List<Long> unlinkedAllOwners = new ArrayList<>();

    /** 返回预置的现有目标 ID */
    @Override
    public List<Long> findTargetIds(final Long ownerId) {
      return existing;
    }

    /** 记录建立关联的调用 */
    @Override
    public void link(final Long ownerId, final Collection<Long> targetIds) {
      linked.add(List.copyOf(targetIds));
    }

    /** 记录解除关联的调用 */
    @Override
    public void unlink(final Long ownerId, final Collection<Long> targetIds) {
      unlinked.add(List.copyOf(targetIds));
    }

    /** 记录清空关联的调用 */
    @Override
    public void unlinkAll(final Long ownerId) {
      unlinkedAllOwners.add(ownerId);
    }
  }
}
