package com.github.link2fun.support.enums;

import java.io.Serializable;

/** 枚举数据库存储值 */
public interface IEnum<T extends Serializable> {

  /** 数据库存储值 */
  T getValue();
}
