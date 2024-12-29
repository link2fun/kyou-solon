package com.github.link2fun.system.modular.notice.service;

import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.notice.model.SysNotice;

import java.util.List;

/**
 * 公告 服务层
 *
 * @author ruoyi
 */
public interface ISystemNoticeService {
  /**
   * 查询公告信息
   *
   * @param noticeId 公告ID
   * @return 公告信息
   */
  SysNotice selectNoticeById(Long noticeId);

  /**
   * 查询公告列表
   *
   * @param notice 公告信息
   * @return 公告集合
   */
  List<SysNotice> selectNoticeList(SysNotice notice);

  /**
   * 根据搜索条件进行分页查询通知列表。
   *
   * @param page      分页适配器
   * @param searchReq 搜索请求
   * @return 分页结果
   */
  Page<SysNotice> pageSearch(Page<SysNotice> page, SysNotice searchReq);

  /**
   * 新增公告
   *
   * @param notice 公告信息
   * @return 结果
   */
  long insertNotice(SysNotice notice);

  /**
   * 修改公告
   *
   * @param notice 公告信息
   * @return 结果
   */
  long updateNotice(SysNotice notice);

  /**
   * 删除公告信息
   *
   * @param noticeId 公告ID
   * @return 结果
   */
  long deleteNoticeById(Long noticeId);

  /**
   * 批量删除公告信息
   *
   * @param noticeIds 需要删除的公告ID
   * @return 结果
   */
  long deleteNoticeByIds(List<Long> noticeIds);

}
