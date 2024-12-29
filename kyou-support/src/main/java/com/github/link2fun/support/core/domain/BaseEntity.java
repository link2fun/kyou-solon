package com.github.link2fun.support.core.domain;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.ColumnIgnore;
import com.easy.query.core.annotation.UpdateIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Entity基类
 *
 * @author ruoyi
 */
@Getter
@Setter
public class BaseEntity extends QueryEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 搜索值 */
  @JsonIgnore
  @ColumnIgnore
  private String searchValue;

  /** 创建者 */
  @UpdateIgnore
  @Column(value = "create_by")
  private String createBy;

  /** 创建时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @UpdateIgnore
  @Column(value = "create_time")
  private LocalDateTime createTime;

  /** 更新者 */
  @Column(value = "update_by")
  private String updateBy;

  /** 更新时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(value = "update_time")
  private LocalDateTime updateTime;


}
