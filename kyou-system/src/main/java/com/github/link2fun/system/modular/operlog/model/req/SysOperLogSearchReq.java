package com.github.link2fun.system.modular.operlog.model.req;

import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;

import java.util.List;

/**
 * {@link com.github.link2fun.system.modular.operlog.model.SysOperLog}
 * SysOperLogSearchReq 查询条件; 只含列表页真正参与过滤的字段
 */
@Data
public class SysOperLogSearchReq {

  /** 操作模块 */
  private String title;

  /** 业务类型 */
  private Integer businessType;

  /** 业务类型集合(多选) */
  private List<Integer> businessTypes;

  /** 操作人员 */
  private String operName;

  /** 主机地址 */
  private String operIp;

  /** 操作状态（0正常 1异常） */
  private Integer status;


  /** 请求参数(日期区间等); 命名固定为 params, 前端以 params[beginTime] 形式传入 */
  private BaseEntityParams params = new BaseEntityParams();
}
