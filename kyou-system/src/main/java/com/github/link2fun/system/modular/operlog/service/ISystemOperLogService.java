package com.github.link2fun.system.modular.operlog.service;

import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.operlog.model.SysOperLog;

import java.util.List;

/**
 * 操作日志 服务层
 *
 * @author ruoyi
 */
public interface ISystemOperLogService {
  /**
   * 新增操作日志
   *
   * @param operLog 操作日志对象
   */
  void insertOperlog(SysOperLog operLog);

  /**
   * 查询系统操作日志集合
   *
   * @param page    分页信息
   * @param operLog 操作日志对象
   * @return 操作日志集合
   */
  Page<SysOperLog> selectOperLogList(final Page<SysOperLog> page, SysOperLog operLog);

  /**
   * 批量删除系统操作日志
   *
   * @param operIds 需要删除的操作日志ID
   * @return 结果
   */
  long deleteOperLogByIds(List<Long> operIds);

  /**
   * 查询操作日志详细
   *
   * @param operId 操作ID
   * @return 操作日志对象
   */
  SysOperLog selectOperLogById(Long operId);

  /**
   * 清空操作日志
   */
  void cleanOperLog();
}
