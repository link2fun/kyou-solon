package com.github.link2fun.support.easyquery;

import cn.hutool.core.collection.CollectionUtil;
import com.github.link2fun.support.exception.ServiceException;

import java.util.List;
import java.util.Objects;

/**
 * 唯一性判定: 探测到 0 行 = 可用, 多行 = 脏数据判不可用, 单行 = 须是自身(编辑排除自身)。
 * 探测查询由各业务自行构造并以 toList 取列表, 判定统一在此, 脏数据下不抛 ORM 异常。
 */
public class UniqueChecker {

  private UniqueChecker() {
  }

  /** 判定探测结果是否可用, 可用表示该值未被其他行占用 */
  public static boolean available(final List<Long> matchedIds, final Long selfId) {
    if (CollectionUtil.isEmpty(matchedIds)) {
      return true;
    }
    if (matchedIds.size() > 1) {
      // 多行同值属于脏数据, 判不唯一
      return false;
    }
    return Objects.equals(matchedIds.get(0), selfId);
  }

  /** 判定探测结果是否可用, 不可用时抛出业务异常 */
  public static void checkOrThrow(final List<Long> matchedIds, final Long selfId, final String errorMessage) {
    if (!available(matchedIds, selfId)) {
      throw new ServiceException(errorMessage);
    }
  }
}
