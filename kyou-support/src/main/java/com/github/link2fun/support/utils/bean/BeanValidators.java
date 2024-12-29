package com.github.link2fun.support.utils.bean;

import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.ValidatorManager;

/**
 * bean对象属性验证
 *
 * @author ruoyi
 */
@SuppressWarnings("unused")
public class BeanValidators {
  public static void validateWithException(Object object, Class<?>... groups) {

    final Result<?> result = ValidatorManager.validateOfEntity(object, groups);
    if (result.getCode() != Result.SUCCEED_CODE) {
      throw new IllegalArgumentException(result.getDescription());
    }
  }
}
