package com.github.link2fun.system.modular.notice.model;


import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.support.core.domain.BaseEntity;
import com.github.link2fun.support.xss.Xss;
import com.github.link2fun.system.modular.notice.model.proxy.SysNoticeProxy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;

/**
 * 通知公告表 sys_notice
 *
 * @author ruoyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
@EntityProxy
@Table("sys_notice")
public class SysNotice extends BaseEntity implements ProxyEntityAvailable<SysNotice, SysNoticeProxy> {

  /** 公告ID */
  @Column(value = "notice_id", primaryKey = true, generatedKey = true)
  private Long noticeId;

  /** 公告标题 */
  @Xss(message = "公告标题不能包含脚本字符")
  @NotBlank(message = "公告标题不能为空")
  @Length(min = 0, max = 50, message = "公告标题长度不能超过50个字符")
  @Column("notice_title")
  private String noticeTitle;

  /** 公告类型（1通知 2公告） */
  @Column("notice_type")
  private String noticeType;

  /** 公告内容 */
  @Column("notice_content")
  private String noticeContent;

  /** 公告状态（0正常 1关闭） */
  @Column("status")
  private String status;

  /** 备注 */
  @Column(value = "remark")
  private String remark;

}
