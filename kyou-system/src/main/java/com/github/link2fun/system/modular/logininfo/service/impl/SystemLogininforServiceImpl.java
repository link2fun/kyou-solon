package com.github.link2fun.system.modular.logininfo.service.impl;

import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.logininfo.model.SysLogininfor;
import com.github.link2fun.system.modular.logininfo.service.ISystemLogininforService;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class SystemLogininforServiceImpl implements ISystemLogininforService {

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 新增系统登录日志
   *
   * @param logininfor 访问日志对象
   */
  @Override
  public void insertLogininfor(final SysLogininfor logininfor) {
    entityQuery.insertable(logininfor).executeRows();
  }

  /**
   * 查询系统登录日志集合
   *
   * @param page      分页适配器
   * @param searchReq 访问日志对象
   * @return 登录记录集合
   */
  @Override
  public Page<SysLogininfor> selectLogininforList(final Page<SysLogininfor> page, final SysLogininfor searchReq) {

    EasyPageResult<SysLogininfor> pageResult = entityQuery.queryable(SysLogininfor.class)
      .where(s -> {
        s.ipaddr().like(StrUtil.isNotBlank(searchReq.getIpaddr()), searchReq.getIpaddr());
        s.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus());
        s.userName().like(StrUtil.isNotBlank(searchReq.getUserName()), searchReq.getUserName());
        s.loginTime().ge(Objects.nonNull(searchReq.getParams().getBeginTime()), searchReq.getParams().getBeginTime());
        s.loginTime().le(Objects.nonNull(searchReq.getParams().getEndTime()), searchReq.getParams().getEndTime());
      })
      .toPageResult(page.getPageNum(), page.getPageSize());

    return Page.of(pageResult);

  }

  /**
   * 批量删除系统登录日志
   *
   * @param infoIds 需要删除的登录日志ID
   * @return 结果
   */
  @Override
  public boolean deleteLogininforByIds(final List<Long> infoIds) {
    return entityQuery.deletable(SysLogininfor.class)
      .allowDeleteStatement(true)
      .where(s -> s.infoId().in(infoIds))
      .executeRows() > 0;
  }

  /**
   * 清空系统登录日志
   */
  @Override
  public void cleanLogininfor() {

    entityQuery.sqlExecute("truncate table sys_logininfor");
  }
}
