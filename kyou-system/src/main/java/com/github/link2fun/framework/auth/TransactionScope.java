package com.github.link2fun.framework.auth;

/**
 * 事务边界 seam(包私有, 不进对外 API): 生产走 Solon 事务监听器, 测试用 fake 覆盖时序。
 */
interface TransactionScope {

  /** 当前线程是否处于事务中 */
  boolean active();

  /** 注册提交后执行; 无事务时抛 IllegalStateException */
  void afterCommit(Runnable action);
}
