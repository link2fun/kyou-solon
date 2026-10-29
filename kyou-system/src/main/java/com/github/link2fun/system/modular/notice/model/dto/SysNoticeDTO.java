package com.github.link2fun.system.modular.notice.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告响应
 * {@link com.github.link2fun.system.modular.notice.model.SysNotice }
 */
@Data
public class SysNoticeDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 公告ID */
  private Long noticeId;
  /** 公告标题 */
  private String noticeTitle;
  /** 公告类型 */
  private String noticeType;
  /** 公告内容 */
  private String noticeContent;
  /** 状态 */
  private String status;
  /** 备注 */
  private String remark;
  /** 创建者 */
  private String createBy;
  /** 创建时间 */
  private LocalDateTime createTime;
  /** 更新者 */
  private String updateBy;
  /** 更新时间 */
  private LocalDateTime updateTime;
}
