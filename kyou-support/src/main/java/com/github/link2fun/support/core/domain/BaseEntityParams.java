package com.github.link2fun.support.core.domain;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

@Getter
@Setter
public class BaseEntityParams extends HashMap<String, Object> {


  /** 获取数据权限过滤语句 */
  @JsonIgnore
  public String getDataScope() {
    return MapUtil.getStr(this, "dataScope");
  }

  /** 设置数据权限过滤语句 */
  public void setDataScope(String dataScope) {
    put("dataScope", dataScope);
  }

  /** 判断是否存在数据权限过滤 */
  @JsonIgnore
  public boolean hasDataScopeFilter() {
    return containsKey("dataScope");
  }


  public LocalDateTime getBeginTime() {
    return LocalDateTimeUtil.parse(MapUtil.getStr(this, "beginTime"));
  }

  public void setBeginTime(LocalDateTime beginTime) {
    put("beginTime", beginTime);
  }

  public LocalDateTime getEndTime() {
    return LocalDateTimeUtil.parse(MapUtil.getStr(this, "endTime"));
  }

  public void setEndTime(LocalDateTime endTime) {
    put("endTime", endTime);
  }
}
