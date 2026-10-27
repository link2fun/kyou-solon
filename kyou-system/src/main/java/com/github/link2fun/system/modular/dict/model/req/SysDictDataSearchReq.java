package com.github.link2fun.system.modular.dict.model.req;

import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;

/**
 * {@link com.github.link2fun.support.core.domain.entity.SysDictData}
 * SysDictDataSearchReq 查询条件; 只含列表页真正参与过滤的字段
 */
@Data
public class SysDictDataSearchReq {

  /** 字典类型 */
  private String dictType;

  /** 字典标签 */
  private String dictLabel;

  /** 状态（0正常 1停用） */
  private String status;


  /** 请求参数(日期区间等); 命名固定为 params, 前端以 params[beginTime] 形式传入 */
  private BaseEntityParams params = new BaseEntityParams();
}
