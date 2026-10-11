package com.github.link2fun.support.easyquery;

import cn.hutool.core.collection.CollectionUtil;
import com.github.link2fun.support.exception.ServiceException;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 唯一性探测与判定
 */
public class UniqueChecker {

  private UniqueChecker() {
  }

  /**
   * 占用探测判定: 探测到占用即抛出业务异常。
   *
   * @param occupied 占用探测结果, true 表示已被其他行占用
   * @param message  不可用时抛出的业务异常文案(惰性)
   */
  public static void checkOccupied(final boolean occupied, final Supplier<String> message) {
    if (occupied) {
      throw new ServiceException(message.get());
    }
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
