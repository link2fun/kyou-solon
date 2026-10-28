package com.github.link2fun.support.enums;

import java.io.Serializable;

/**
 * 带标签的枚举
 *
 * @param <T> 数据库存储值类型
 */
public interface LabelEnum<T extends Serializable> extends IEnum<T> {

  /** 展示标签 */
  String getLabel();
}
