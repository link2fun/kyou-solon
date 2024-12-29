package com.github.link2fun.system.modular.post.service;

import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.domain.entity.SysPost;

import java.util.List;

/**
 * 岗位信息 服务层
 *
 * @author ruoyi
 */
public interface ISystemPostService {
  /**
   * 查询岗位信息集合
   *
   * @param post 岗位信息
   * @return 岗位列表
   */
  List<SysPost> selectPostList(SysPost post);

  /**
   * 根据搜索条件进行分页查询岗位信息。
   *
   * @param page      分页适配器
   * @param searchReq 搜索条件
   * @return 分页结果
   */
  Page<SysPost> pageSearch(Page<SysPost> page, SysPost searchReq);

  /**
   * 查询所有岗位
   *
   * @return 岗位列表
   */
  List<SysPost> selectPostAll();

  /**
   * 通过岗位ID查询岗位信息
   *
   * @param postId 岗位ID
   * @return 角色对象信息
   */
  SysPost selectPostById(Long postId);

  /**
   * 根据用户ID获取岗位选择框列表
   *
   * @param userId 用户ID
   * @return 选中岗位ID列表
   */
  List<Long> selectPostListByUserId(Long userId);

  /**
   * 校验岗位名称
   *
   * @param post 岗位信息
   * @return 结果
   */
  boolean checkPostNameUnique(SysPost post);

  /**
   * 校验岗位编码
   *
   * @param post 岗位信息
   * @return 结果
   */
  boolean checkPostCodeUnique(SysPost post);

  /**
   * 批量删除岗位信息
   *
   * @param postIds 需要删除的岗位ID
   * @return 结果
   */
  long deletePostByIds(List<Long> postIds);

  /**
   * 新增保存岗位信息
   *
   * @param post 岗位信息
   * @return 结果
   */
  long insertPost(SysPost post);

  /**
   * 修改保存岗位信息
   *
   * @param post 岗位信息
   * @return 结果
   */
  long updatePost(SysPost post);

  List<SysPost> listByIds(List<Long> postIdList);
}
