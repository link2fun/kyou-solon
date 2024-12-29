package com.github.link2fun.framework.web.domain.server;


import com.github.link2fun.support.utils.Arith;

/**
 * 內存相关信息
 *
 * @author ruoyi
 */
public class Mem {
  /**
   * 内存总量
   */
  private Double total;

  /**
   * 已用内存
   */
  private Double used;

  /**
   * 剩余内存
   */
  private Double free;

  public Double getTotal() {
    return Arith.div(total, (1024 * 1024 * 1024), 2);
  }

  public void setTotal(long total) {
    this.total = (double) total;
  }

  public Double getUsed() {
    return Arith.div(used, (1024 * 1024 * 1024), 2);
  }

  public void setUsed(long used) {
    this.used = (double) used;
  }

  public Double getFree() {
    return Arith.div(free, (1024 * 1024 * 1024), 2);
  }

  public void setFree(long free) {
    this.free = (double) free;
  }

  public Double getUsage() {
    return Arith.mul(Arith.div(used, total, 4), 100);
  }
}
