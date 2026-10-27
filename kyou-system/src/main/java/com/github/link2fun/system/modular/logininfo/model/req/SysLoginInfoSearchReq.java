package com.github.link2fun.system.modular.logininfo.model.req;

import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;

/**
 * {@link com.github.link2fun.system.modular.logininfo.model.SysLoginInfo}
 * SysLoginInfoSearchReq 查询条件; 只含列表页真正参与过滤的字段
 */
@Data
public class SysLoginInfoSearchReq {

  /** 用户账号 */
  private String userName;

  /** 登录地址 */
  private String ipaddr;

  /** 登录状态（0成功 1失败） */
  private String status;


  /** 请求参数(日期区间等); 命名固定为 params, 前端以 params[beginTime] 形式传入 */
  private BaseEntityParams params = new BaseEntityParams();
}
