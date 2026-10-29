package com.github.link2fun.system.modular.notice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.api.proxy.entity.select.EntityQueryable;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.notice.model.SysNotice;
import com.github.link2fun.system.modular.notice.model.proxy.SysNoticeProxy;
import com.github.link2fun.system.modular.notice.model.req.SysNoticeAddReq;
import com.github.link2fun.system.modular.notice.model.req.SysNoticeModifyReq;
import com.github.link2fun.system.modular.notice.model.req.SysNoticeSearchReq;
import com.github.link2fun.system.modular.notice.service.ISystemNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class SystemNoticeServiceImpl implements ISystemNoticeService {

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 查询公告信息
   *
   * @param noticeId 公告ID
   * @return 公告信息
   */
  @Override
  public <T> T selectNoticeById(final Long noticeId, final Class<T> resultClass) {
    return entityQuery.queryable(SysNotice.class).whereById(noticeId).selectAutoInclude(resultClass).singleOrNull();
  }

  /**
   * 查询公告列表
   *
   * @param searchReq 查询条件
   * @return 公告集合
   */
  @Override
  public <T> List<T> selectNoticeList(final SysNoticeSearchReq searchReq, final Class<T> resultClass) {
    return buildQuery(searchReq).selectAutoInclude(resultClass).toList();
  }

  /** 构造公告列表查询 */
  private EntityQueryable<SysNoticeProxy, SysNotice> buildQuery(final SysNoticeSearchReq searchReq) {
    EntityQueryable<SysNoticeProxy, SysNotice> queryable = entityQuery.queryable(SysNotice.class);
    if (Objects.isNull(searchReq)) {
      return queryable;
    }
    return queryable.where(noticeQuery -> {
      noticeQuery.noticeTitle().like(StrUtil.isNotBlank(searchReq.getNoticeTitle()), searchReq.getNoticeTitle()); // 公告标题
      noticeQuery.noticeType().eq(StrUtil.isNotBlank(searchReq.getNoticeType()), searchReq.getNoticeType()); // 公告类型
      noticeQuery.createBy().like(StrUtil.isNotBlank(searchReq.getCreateBy()), searchReq.getCreateBy()); // 创建者
    });
  }

  /**
   * 根据搜索条件进行分页查询通知列表。
   *
   * @param pageRequest 分页适配器
   * @param searchReq   搜索请求
   * @return 分页结果
   */
  @Override
  public <T> Page<T> pageSearch(final Page<T> pageRequest, final SysNoticeSearchReq searchReq, final Class<T> resultClass) {
    return Page.of(pageRequest, buildQuery(searchReq).selectAutoInclude(resultClass).toPageResult(pageRequest.getPageNum(), pageRequest.getPageSize(), pageRequest.getTotal()));
  }

  /**
   * 新增公告
   *
   * @param addReq 新增公告请求
   * @return 结果
   */
  @Override
  public long insertNotice(final SysNoticeAddReq addReq) {
    final SysNotice notice = BeanUtil.toBean(addReq, SysNotice.class);
    return entityQuery.insertable(notice).executeRows();
  }

  /**
   * 修改公告
   *
   * @param modifyReq 修改公告请求
   * @return 结果
   */
  @Override
  public long updateNotice(final SysNoticeModifyReq modifyReq) {
    final SysNotice notice = BeanUtil.toBean(modifyReq, SysNotice.class);
    return entityQuery.updatable(notice).executeRows();
  }

  /**
   * 删除公告信息
   *
   * @param noticeId 公告ID
   * @return 结果
   */
  @Override
  public long deleteNoticeById(final Long noticeId) {
    return entityQuery.deletable(SysNotice.class).whereById(noticeId)
      .allowDeleteStatement(true)
      .executeRows();
  }

  /**
   * 批量删除公告信息
   *
   * @param noticeIds 需要删除的公告ID
   * @return 结果
   */
  @Override
  public long deleteNoticeByIds(final List<Long> noticeIds) {
    return entityQuery.deletable(SysNotice.class).where(notice -> notice.noticeId().in(noticeIds))
      .allowDeleteStatement(true)
      .executeRows();
  }
}
