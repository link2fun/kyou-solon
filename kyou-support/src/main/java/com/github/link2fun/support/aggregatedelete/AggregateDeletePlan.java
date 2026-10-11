package com.github.link2fun.support.aggregatedelete;

import com.github.link2fun.support.exception.ServiceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 一份聚合删除计划的声明：守卫 → 解绑 → 删除，三个槽位的闭包。
 *
 * <p>顺序由 {@link AggregateDeleter} 持有，声明处只描述内容：
 * <ul>
 *   <li>守卫（guard / guardPerId）：任一抛出业务异常即终止，不产生任何写操作</li>
 *   <li>解绑（unlink）：清除主体在关联表中的痕迹，签名与 removeByXxxIds 一致</li>
 *   <li>删除（delete）：删主体本身，软删或硬删由声明处决定，返回受影响行数</li>
 * </ul>
 *
 * <p>id 集合在执行前归一化：去重、剔除 null；归一化后为空时不执行任何操作（幂等空删）。
 *
 * @author link2fun
 */
public final class AggregateDeletePlan {

  /** 归一化后的待删除 id 集合 */
  private final List<Long> ids;

  /** 守卫列表：全部先于解绑与删除执行 */
  private final List<Consumer<List<Long>>> guards = new ArrayList<>();

  /** 解绑列表：全部先于删除执行 */
  private final List<Consumer<List<Long>>> unlinks = new ArrayList<>();

  /** 删除步骤：唯一且必需 */
  private Function<List<Long>, Long> deleteStep;

  private AggregateDeletePlan(final Collection<Long> ids) {
    this.ids = normalize(ids);
  }

  /**
   * 以待删除的 id 集合创建计划
   *
   * @param ids 待删除的主体 ID 集合, 允许 null 或含 null 元素
   */
  public static AggregateDeletePlan of(final Collection<Long> ids) {
    return new AggregateDeletePlan(ids);
  }

  /**
   * 批量守卫：一次拿到归一化后的全部 id
   *
   * @param guard 守卫逻辑, 抛出业务异常即阻止删除
   */
  public AggregateDeletePlan guard(final Consumer<List<Long>> guard) {
    guards.add(guard);
    return this;
  }

  /**
   * 逐 id 守卫：对归一化后的每个 id 各执行一次
   *
   * @param guard 单个 id 的守卫逻辑, 抛出业务异常即阻止删除
   */
  public AggregateDeletePlan guardPerId(final Consumer<Long> guard) {
    return guard(ids -> ids.forEach(guard));
  }

  /**
   * 解绑步骤：清除主体在关联表中的痕迹
   *
   * @param unlink 解绑逻辑, 入参为归一化后的 id 集合
   */
  public AggregateDeletePlan unlink(final Consumer<List<Long>> unlink) {
    unlinks.add(unlink);
    return this;
  }

  /**
   * 删除主体本身, 每份计划必须且只能声明一次
   *
   * @param deleteStep 删除逻辑（软删或硬删）, 返回受影响行数
   */
  public AggregateDeletePlan delete(final Function<List<Long>, Long> deleteStep) {
    this.deleteStep = deleteStep;
    return this;
  }

  List<Long> ids() {
    return List.copyOf(ids);
  }

  List<Consumer<List<Long>>> guards() {
    return List.copyOf(guards);
  }

  List<Consumer<List<Long>>> unlinks() {
    return List.copyOf(unlinks);
  }

  Function<List<Long>, Long> deleteStep() {
    if (Objects.isNull(deleteStep)) {
      throw new ServiceException("聚合删除计划缺少删除步骤");
    }
    return deleteStep;
  }

  /** 去重并剔除 null 元素; null 集合视同空集合 */
  private static List<Long> normalize(final Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return ids.stream()
      .filter(Objects::nonNull)
      .distinct()
      .toList();
  }
}
