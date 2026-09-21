package com.github.link2fun.support.easyquery;

import com.github.link2fun.support.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 唯一性判定纯逻辑: 空列表 / 单行排除自身 / 脏数据多行 / 抛异常文案
 */
public class UniqueCheckerTest {

  @Test
  @DisplayName("空列表表示值未被占用, 判可用")
  public void emptyListIsAvailable() {
    assertTrue(UniqueChecker.available(List.of(), 100L));
    assertTrue(UniqueChecker.available(null, 100L));
  }

  @Test
  @DisplayName("单行且是自身(编辑排除自身), 判可用")
  public void singleSelfRowIsAvailable() {
    assertTrue(UniqueChecker.available(List.of(100L), 100L));
  }

  @Test
  @DisplayName("单行不是自身, 判不可用")
  public void singleOtherRowIsNotAvailable() {
    assertTrue(!UniqueChecker.available(List.of(101L), 100L));
  }

  @Test
  @DisplayName("单行而自身为空(新增场景), 判不可用")
  public void singleRowWithoutSelfIsNotAvailable() {
    assertTrue(!UniqueChecker.available(List.of(101L), null));
  }

  @Test
  @DisplayName("多行同值属于脏数据, 一律判不可用")
  public void dirtyDuplicateRowsAreNotAvailable() {
    assertTrue(!UniqueChecker.available(List.of(100L, 100L), 100L));
  }

  @Test
  @DisplayName("checkOrThrow 不可用时抛业务异常且文案原样")
  public void checkOrThrowThrowsWithMessage() {
    final ServiceException e = assertThrows(ServiceException.class,
      () -> UniqueChecker.checkOrThrow(List.of(101L), 100L, "新增用户'a'失败，登录账号已存在"));
    assertEquals("新增用户'a'失败，登录账号已存在", e.getMessage());
  }

  @Test
  @DisplayName("checkOrThrow 可用时不抛")
  public void checkOrThrowPassesWhenAvailable() {
    UniqueChecker.checkOrThrow(List.of(), 100L, "any");
    UniqueChecker.checkOrThrow(List.of(100L), 100L, "any");
  }
}
