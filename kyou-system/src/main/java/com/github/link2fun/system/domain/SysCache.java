package com.github.link2fun.system.domain;


import com.github.link2fun.support.utils.StringUtils;
import lombok.Data;

import java.io.Serializable;

/**
 * 缓存信息
 *
 * @author ruoyi
 */
@Data
public class SysCache implements Serializable {

  /** 缓存名称 */
  private String cacheName = "";

  /** 缓存键名 */
  private String cacheKey = "";

  /** 缓存内容 */
  private String cacheValue = "";

  /** 备注 */
  private String remark = "";

  public SysCache() {

  }

  public SysCache(String cacheName, String remark) {
    this.cacheName = cacheName;
    this.remark = remark;
  }

  public SysCache(String cacheName, String cacheKey, String cacheValue) {
    this.cacheName = StringUtils.replace(cacheName, "", "");
    this.cacheKey = StringUtils.replace(cacheKey, cacheName, "");
    this.cacheValue = cacheValue;
  }
}
