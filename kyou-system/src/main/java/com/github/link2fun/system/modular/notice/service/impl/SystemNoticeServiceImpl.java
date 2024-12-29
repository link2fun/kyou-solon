package com.github.link2fun.system.modular.notice.service.impl;

import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.notice.model.SysNotice;
import com.github.link2fun.system.modular.notice.service.ISystemNoticeService;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import java.util.List;

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
  public SysNotice selectNoticeById(final Long noticeId) {
    return entityQuery.queryable(SysNotice.class).whereById(noticeId).singleOrNull();
  }

  /**
   * 查询公告列表
   *
   * @param notice 公告信息
   * @return 公告集合
   */
  @Override
  public List<SysNotice> selectNoticeList(final SysNotice notice) {

    return entityQuery.queryable(SysNotice.class)
      .where(noticeQuery -> {
        noticeQuery.noticeTitle().like(StrUtil.isNotBlank(notice.getNoticeTitle()), notice.getNoticeTitle()); // 公告标题
        noticeQuery.noticeType().eq(StrUtil.isNotBlank(notice.getNoticeType()), notice.getNoticeType()); // 公告类型
        noticeQuery.createBy().like(StrUtil.isNotBlank(notice.getCreateBy()), notice.getCreateBy()); // 创建者
      }).toList();
  }

  /**
   * 根据搜索条件进行分页查询通知列表。
   *
   * @param page      分页适配器
   * @param searchReq 搜索请求
   * @return 分页结果
   */
  @Override
  public Page<SysNotice> pageSearch(final Page<SysNotice> page, final SysNotice searchReq) {

    EasyPageResult<SysNotice> pageResult = entityQuery.queryable(SysNotice.class)
      .where(noticeQuery -> {
        noticeQuery.noticeTitle().like(StrUtil.isNotBlank(searchReq.getNoticeTitle()), searchReq.getNoticeTitle()); // 公告标题
        noticeQuery.noticeType().eq(StrUtil.isNotBlank(searchReq.getNoticeType()), searchReq.getNoticeType()); // 公告类型
        noticeQuery.createBy().like(StrUtil.isNotBlank(searchReq.getCreateBy()), searchReq.getCreateBy()); // 创建者
      }).toPageResult(page.getPageNum(), page.getPageSize());
    return Page.of(pageResult);
  }

  /**
   * 新增公告
   *
   * @param notice 公告信息
   * @return 结果
   */
  @Override
  public long insertNotice(final SysNotice notice) {
    return entityQuery.insertable(notice).executeRows();
  }

  /**
   * 修改公告
   *
   * @param notice 公告信息
   * @return 结果
   */
  @Override
  public long updateNotice(final SysNotice notice) {
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
