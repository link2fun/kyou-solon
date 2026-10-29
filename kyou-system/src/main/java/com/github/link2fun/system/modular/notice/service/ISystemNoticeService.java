package com.github.link2fun.system.modular.notice.service;

import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.notice.model.req.SysNoticeAddReq;
import com.github.link2fun.system.modular.notice.model.req.SysNoticeModifyReq;
import com.github.link2fun.system.modular.notice.model.req.SysNoticeSearchReq;

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
  <T> T selectNoticeById(Long noticeId, Class<T> resultClass);

  /**
   * 查询公告列表
   *
   * @param searchReq 查询条件
   * @return 公告集合
   */
  <T> List<T> selectNoticeList(SysNoticeSearchReq searchReq, Class<T> resultClass);

  /**
   * 根据搜索条件进行分页查询通知列表。
   *
   * @param pageRequest 分页适配器
   * @param searchReq   搜索请求
   * @return 分页结果
   */
  <T> Page<T> pageSearch(Page<T> pageRequest, SysNoticeSearchReq searchReq, Class<T> resultClass);

  /**
   * 新增公告
   *
   * @param addReq 新增公告请求
   * @return 结果
   */
  long insertNotice(SysNoticeAddReq addReq);

  /**
   * 修改公告
   *
   * @param modifyReq 修改公告请求
   * @return 结果
   */
  long updateNotice(SysNoticeModifyReq modifyReq);

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
