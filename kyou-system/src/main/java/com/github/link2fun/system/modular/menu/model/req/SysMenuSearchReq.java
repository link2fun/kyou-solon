package com.github.link2fun.system.modular.menu.model.req;

import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;

/**
 * SysMenuSearchReq 查询条件; 只含列表页真正参与过滤的字段
 * {@link com.github.link2fun.support.core.domain.entity.SysMenu}
 */
@Data
public class SysMenuSearchReq {

  /** 菜单名称 */
  private String menuName;

  /** 菜单状态（0显示 1隐藏） */
  private String visible;

  /** 菜单状态（0正常 1停用） */
  private String status;


  /** 请求参数(日期区间等); 命名固定为 params, 前端以 params[beginTime] 形式传入 */
  private BaseEntityParams params = new BaseEntityParams();
}
