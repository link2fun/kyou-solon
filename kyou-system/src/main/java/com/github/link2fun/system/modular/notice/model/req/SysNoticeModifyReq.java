package com.github.link2fun.system.modular.notice.model.req;

import com.github.link2fun.support.xss.Xss;
import lombok.Data;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

/**
 * 修改通知公告请求
 * {@link com.github.link2fun.system.modular.notice.model.SysNotice}
 */
@Data
public class SysNoticeModifyReq {

  /** 公告ID */
  @NotNull(message = "公告ID不能为空")
  private Long noticeId;

  /** 公告标题 */
  @Xss(message = "公告标题不能包含脚本字符")
  @NotBlank(message = "公告标题不能为空")
  @Length(min = 0, max = 50, message = "公告标题长度不能超过50个字符")
  private String noticeTitle;

  /** 公告类型（1通知 2公告） */
  private String noticeType;

  /** 公告内容 */
  private String noticeContent;

  /** 公告状态（0正常 1关闭） */
  private String status;

  /** 备注 */
  private String remark;

}
