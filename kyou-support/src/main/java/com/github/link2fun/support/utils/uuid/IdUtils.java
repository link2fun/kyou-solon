package com.github.link2fun.support.utils.uuid;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;

/**
 * ID生成器工具类
 *
 * @author ruoyi
 */
@SuppressWarnings("unused")
public class IdUtils {
  /**
   * 获取随机UUID
   *
   * @return 随机UUID
   */
  public static String randomUUID() {
    return UUID.randomUUID().toString();
  }

  /**
   * 简化的UUID，去掉了横线
   *
   * @return 简化的UUID，去掉了横线
   */
  public static String simpleUUID() {
    return UUID.randomUUID().toString(true);
  }

  /**
   * 获取随机UUID，使用性能更好的ThreadLocalRandom生成UUID
   *
   * @return 随机UUID
   */
  public static String fastUUID() {
    return UUID.fastUUID().toString();
  }

  /**
   * 简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
   *
   * @return 简化的UUID，去掉了横线
   */
  public static String fastSimpleUUID() {
    return UUID.fastUUID().toString(true);
  }

  /**
   * 判断 Long 类型ID是否合法<br/>
   * 当传入值为 null 或者小于等于 0 时返回 false
   *
   * @param id ID
   * @return 是否合法
   */
  public static boolean isIdValid(Long id) {
    return id != null && id > 0;
  }

  /**
   * 判断 String 类型ID是否合法<br/>
   * 当传入值为 null 或者空字符串时返回 false
   *
   * @param id ID
   *           @return 是否合法
   */
  public static boolean isIdValid(String id) {
    return StrUtil.isNotBlank(id);
  }


}
