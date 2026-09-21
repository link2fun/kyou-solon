package com.github.link2fun.support.core.domain;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;

@Getter
@Setter
public class BaseEntityParams extends HashMap<String, Object> {


  public LocalDateTime getBeginTime() {
    return LocalDateTimeUtil.parse(MapUtil.getStr(this, "beginTime"));
  }

  @SuppressWarnings("unused")
  public void setBeginTime(LocalDateTime beginTime) {
    put("beginTime", beginTime);
  }

  public LocalDateTime getEndTime() {
    return LocalDateTimeUtil.parse(MapUtil.getStr(this, "endTime"));
  }

  @SuppressWarnings("unused")
  public void setEndTime(LocalDateTime endTime) {
    put("endTime", endTime);
  }
}
