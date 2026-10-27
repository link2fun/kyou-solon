package com.github.link2fun.system.modular.notice.model.req;

import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;

/**
 * {@link com.github.link2fun.system.modular.notice.model.SysNotice}
 * SysNoticeSearchReq 查询条件; 只含列表页真正参与过滤的字段
 */
@Data
public class SysNoticeSearchReq {

  /** 公告标题 */
  private String noticeTitle;

  /** 公告类型 */
  private String noticeType;

  /** 创建者 */
  private String createBy;


  /** 请求参数(日期区间等); 命名固定为 params, 前端以 params[beginTime] 形式传入 */
  private BaseEntityParams params = new BaseEntityParams();
}
