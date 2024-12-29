package com.github.link2fun.system.modular.operlog.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.operlog.model.SysOperLog;
import com.github.link2fun.system.modular.operlog.service.ISystemOperLogService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class SystemOperLogServiceImpl implements ISystemOperLogService {

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 新增操作日志
   *
   * @param operLog 操作日志对象
   */
  @Override
  public void insertOperlog(final SysOperLog operLog) {
    entityQuery.insertable(operLog).executeRows();
  }

  /**
   * 查询系统操作日志集合
   *
   * @param page      分页对象
   * @param searchReq 操作日志对象
   * @return 操作日志集合
   */
  @Override
  public Page<SysOperLog> selectOperLogList(final Page<SysOperLog> page, final SysOperLog searchReq) {

    EasyPageResult<SysOperLog> pageResult = entityQuery.queryable(SysOperLog.class)
      .where(operLog -> {
        operLog.operIp().like(StrUtil.isNotBlank(searchReq.getOperIp()), searchReq.getOperIp());
        operLog.title().like(StrUtil.isNotBlank(searchReq.getTitle()), searchReq.getTitle());
        operLog.businessType().eq(Objects.nonNull(searchReq.getBusinessType()), searchReq.getBusinessType());
        operLog.businessType().in(CollectionUtil.isNotEmpty(searchReq.getBusinessTypes()), searchReq.getBusinessTypes());
        operLog.status().eq(Objects.nonNull(searchReq.getStatus()), searchReq.getStatus());
        operLog.operName().like(StrUtil.isNotBlank(searchReq.getOperName()), searchReq.getOperName());
        operLog.operTime().ge(Objects.nonNull(searchReq.getParams().getBeginTime()), searchReq.getParams().getBeginTime());
        operLog.operTime().le(Objects.nonNull(searchReq.getParams().getEndTime()), searchReq.getParams().getEndTime());

      })
      .toPageResult(page.getPageNum(), page.getPageSize());

    return Page.of(pageResult);
  }

  /**
   * 批量删除系统操作日志
   *
   * @param operIds 需要删除的操作日志ID
   * @return 结果
   */
  @Override
  public long deleteOperLogByIds(final List<Long> operIds) {
    return entityQuery.deletable(SysOperLog.class)
      .where(operLog -> operLog.operId().in(operIds))
      .allowDeleteStatement(true)
      .executeRows();
  }

  /**
   * 查询操作日志详细
   *
   * @param operId 操作ID
   * @return 操作日志对象
   */
  @Override
  public SysOperLog selectOperLogById(final Long operId) {
    return entityQuery.queryable(SysOperLog.class)
      .where(operLog -> operLog.operId().eq(operId))
      .singleOrNull();

  }

  /**
   * 清空操作日志
   */
  @Override
  public void cleanOperLog() {
    entityQuery.sqlExecute("truncate table sys_oper_log");
  }
}
