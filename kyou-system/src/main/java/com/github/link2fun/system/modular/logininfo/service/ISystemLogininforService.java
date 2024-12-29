package com.github.link2fun.system.modular.logininfo.service;

import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.logininfo.model.SysLogininfor;


import java.util.List;

/**
 * 系统访问日志情况信息 服务层
 *
 * @author ruoyi
 */
public interface ISystemLogininforService {
  /**
   * 新增系统登录日志
   *
   * @param logininfor 访问日志对象
   */
  void insertLogininfor(SysLogininfor logininfor);

  /**
   * 查询系统登录日志集合
   *
   * @param page      分页适配器
   * @param searchReq 访问日志对象
   * @return 登录记录集合
   */
  Page<SysLogininfor> selectLogininforList(final Page<SysLogininfor> page, SysLogininfor searchReq);

  /**
   * 批量删除系统登录日志
   *
   * @param infoIds 需要删除的登录日志ID
   * @return 结果
   */
  boolean deleteLogininforByIds(List<Long> infoIds);

  /**
   * 清空系统登录日志
   */
  void cleanLogininfor();
}
