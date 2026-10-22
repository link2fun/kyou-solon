package com.github.link2fun.framework.auth;

import org.noear.solon.data.tran.TranListener;
import org.noear.solon.data.tran.TranUtils;

/**
 * TransactionScope 的 Solon 实现: 处于事务中时把动作挂到 afterCommit。
 */
class SolonTransactionScope implements TransactionScope {

  static final SolonTransactionScope INSTANCE = new SolonTransactionScope();

  /** 当前线程是否处于事务中 */
  @Override
  public boolean active() {
    return TranUtils.inTrans();
  }

  /** 注册提交后执行; 无事务时抛 IllegalStateException */
  @Override
  public void afterCommit(final Runnable action) {
    TranUtils.listen(new TranListener() {
      @Override
      public void afterCommit() {
        action.run();
      }
    });
  }
}
