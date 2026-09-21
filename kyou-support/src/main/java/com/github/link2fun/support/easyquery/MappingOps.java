package com.github.link2fun.support.easyquery;

import java.util.Collection;
import java.util.List;

/**
 * 一张关联表（两列复合主键：宿主 + 目标）的操作集。
 *
 * @author link2fun
 */
public interface MappingOps {

  /**
   * 查询宿主当前关联的全部目标 ID
   *
   * @param ownerId 宿主 ID
   * @return 目标 ID 列表, 无关联时返回空集合
   */
  List<Long> findTargetIds(Long ownerId);

  /**
   * 批量建立关联
   *
   * @param ownerId   宿主 ID
   * @param targetIds 目标 ID 集合, 调用方保证非空且不含 null
   */
  void link(Long ownerId, Collection<Long> targetIds);

  /**
   * 批量解除指定关联
   *
   * @param ownerId   宿主 ID
   * @param targetIds 要解除的目标 ID 集合, 调用方保证非空且不含 null
   */
  void unlink(Long ownerId, Collection<Long> targetIds);

  /**
   * 解除宿主的全部关联
   *
   * @param ownerIds 宿主 ID 集合, 空集合时不执行任何操作
   */
  void unlinkAll(Collection<Long> ownerIds);
}
