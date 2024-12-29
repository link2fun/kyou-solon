package com.github.link2fun.support.enums;

/**
 * 限流类型
 *
 * @author ruoyi
 */
@SuppressWarnings("unused")
public enum LimitType {
  /**
   * 默认策略全局限流
   */
  GLOBAL,

  /**
   * 根据请求者IP进行限流
   */
  IP,

  /**
   * 根据请求者ID进行限流
   */
  USER;
}
