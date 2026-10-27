package com.github.link2fun.system.modular.dept.model.req;

import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;

/**
 * {@link com.github.link2fun.support.core.domain.entity.SysDept}
 * SysDeptSearchReq 查询条件; 只含列表页真正参与过滤的字段
 */
@Data
public class SysDeptSearchReq {

  /** 部门ID */
  private Long deptId;

  /** 父部门ID */
  private Long parentId;

  /** 部门名称 */
  private String deptName;

  /** 部门状态（0正常 1停用） */
  private String status;


  /** 请求参数(日期区间等); 命名固定为 params, 前端以 params[beginTime] 形式传入 */
  private BaseEntityParams params = new BaseEntityParams();
}
