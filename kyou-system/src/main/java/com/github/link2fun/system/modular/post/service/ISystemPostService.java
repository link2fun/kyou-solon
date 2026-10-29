package com.github.link2fun.system.modular.post.service;

import com.github.link2fun.support.core.domain.entity.SysPost;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.post.model.req.SysPostAddReq;
import com.github.link2fun.system.modular.post.model.req.SysPostModifyReq;
import com.github.link2fun.system.modular.post.model.req.SysPostSearchReq;

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
   * @param searchReq 查询条件
   * @return 岗位列表
   */
  <T> List<T> selectPostList(SysPostSearchReq searchReq, Class<T> resultClass);

  /**
   * 根据搜索条件进行分页查询岗位信息。
   *
   * @param pageRequest 分页适配器
   * @param searchReq   搜索条件
   * @return 分页结果
   */
  <T> Page<T> pageSearch(Page<T> pageRequest, SysPostSearchReq searchReq, Class<T> resultClass);

  /**
   * 查询所有岗位
   *
   * @return 岗位列表
   */
  <T> List<T> selectPostAll(Class<T> resultClass);

  /**
   * 通过岗位ID查询岗位信息
   *
   * @param postId 岗位ID
   * @return 角色对象信息
   */
  <T> T selectPostById(Long postId, Class<T> resultClass);

  /**
   * 根据用户ID获取岗位选择框列表
   *
   * @param userId 用户ID
   * @return 选中岗位ID列表
   */
  List<Long> selectPostListByUserId(Long userId);

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
   * @param addReq 新增岗位请求
   * @return 结果
   */
  long insertPost(SysPostAddReq addReq);

  /**
   * 修改保存岗位信息
   *
   * @param modifyReq 修改岗位请求
   * @return 结果
   */
  long updatePost(SysPostModifyReq modifyReq);

  List<SysPost> listByIds(List<Long> postIdList);
}
