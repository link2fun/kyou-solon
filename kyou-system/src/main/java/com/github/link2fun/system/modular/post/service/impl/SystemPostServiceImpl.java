package com.github.link2fun.system.modular.post.service.impl;

import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.domain.entity.SysPost;
import com.github.link2fun.system.modular.post.service.ISystemPostService;
import com.github.link2fun.support.core.domain.entity.SysUserPost;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import java.util.List;
import java.util.Objects;


@Slf4j
@Component
public class SystemPostServiceImpl implements ISystemPostService {


  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 查询岗位信息集合
   *
   * @param searchReq 岗位信息
   * @return 岗位列表
   */
  @Override
  public List<SysPost> selectPostList(final SysPost searchReq) {
    return entityQuery.queryable(SysPost.class)
      .where(post -> {
        post.postCode().like(StrUtil.isNotBlank(searchReq.getPostCode()), searchReq.getPostCode()); // 岗位编码
        post.postName().like(StrUtil.isNotBlank(searchReq.getPostName()), searchReq.getPostName()); // 岗位名称
        post.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus()); // 状态
      })
      .toList();
  }

  /**
   * 根据搜索条件进行分页查询岗位信息。
   *
   * @param page      分页适配器
   * @param searchReq 搜索条件
   * @return 分页结果
   */
  @Override
  public Page<SysPost> pageSearch(Page<SysPost> page, SysPost searchReq) {
    if (Objects.isNull(searchReq)) {
      EasyPageResult<SysPost> pageResult = entityQuery.queryable(SysPost.class)
        .toPageResult(page.getPageNum(), page.getPageSize());
      return Page.of(pageResult);
    }
    EasyPageResult<SysPost> pageResult = entityQuery.queryable(SysPost.class)
      .where(post -> {
        post.postCode().like(StrUtil.isNotBlank(searchReq.getPostCode()), searchReq.getPostCode()); // 岗位编码
        post.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus()); // 状态
        post.postName().like(StrUtil.isNotBlank(searchReq.getPostName()), searchReq.getPostName()); // 岗位名称
      })
      .toPageResult(page.getPageNum(), page.getPageSize());
    return Page.of(pageResult);
  }

  /**
   * 查询所有岗位
   *
   * @return 岗位列表
   */
  @Override
  public List<SysPost> selectPostAll() {
    return entityQuery.queryable(SysPost.class).toList();
  }

  /**
   * 通过岗位ID查询岗位信息
   *
   * @param postId 岗位ID
   * @return 角色对象信息
   */
  @Override
  public SysPost selectPostById(final Long postId) {
    return entityQuery.queryable(SysPost.class).whereById(postId).singleOrNull();
  }

  /**
   * 根据用户ID获取岗位选择框列表
   *
   * @param userId 用户ID
   * @return 选中岗位ID列表
   */
  @Override
  public List<Long> selectPostListByUserId(final Long userId) {
    return entityQuery.queryable(SysPost.class)
      .innerJoin(SysUserPost.class, (post, userPost) -> post.postId().eq(userPost.postId()))
      .where((post, userPost) -> userPost.userId().eq(userId))
      .select((post, userPost) -> post.postId())
      .toList();

  }

  /**
   * 校验岗位名称
   *
   * @param post 岗位信息
   * @return 结果
   */
  @Override
  public boolean checkPostNameUnique(final SysPost post) {
    final SysPost temp = entityQuery.queryable(SysPost.class)
      .where(_post -> _post.postName().eq(post.getPostName()))
      .singleOrNull();
    if (Objects.nonNull(temp) && !Objects.equals(temp.getPostId(), post.getPostId())) {
      return UserConstants.NOT_UNIQUE;
    }
    return UserConstants.UNIQUE;
  }

  /**
   * 校验岗位编码
   *
   * @param post 岗位信息
   * @return 结果
   */
  @Override
  public boolean checkPostCodeUnique(final SysPost post) {
    final SysPost temp = entityQuery.queryable(SysPost.class)
      .where(_post -> _post.postCode().eq(post.getPostCode()))
      .singleOrNull();
    if (Objects.nonNull(temp) && !Objects.equals(temp.getPostId(), post.getPostId())) {
      return UserConstants.NOT_UNIQUE;
    }
    return UserConstants.UNIQUE;
  }


  /**
   * 批量删除岗位信息
   *
   * @param postIds 需要删除的岗位ID
   * @return 结果
   */
  @Override
  public long deletePostByIds(final List<Long> postIds) {
    return entityQuery.deletable(SysPost.class)
      .where(post -> post.postId().in(postIds))
      .allowDeleteStatement(true)
      .executeRows();
  }

  /**
   * 新增保存岗位信息
   *
   * @param post 岗位信息
   * @return 结果
   */
  @Override
  public long insertPost(final SysPost post) {
    return entityQuery.insertable(post)
      .executeRows();
  }

  /**
   * 修改保存岗位信息
   *
   * @param post 岗位信息
   * @return 结果
   */
  @Override
  public long updatePost(final SysPost post) {
    return entityQuery.updatable(post)
      .executeRows();
  }

  @Override
  public List<SysPost> listByIds(List<Long> postIdList) {
    return entityQuery.queryable(SysPost.class)
      .whereByIds(postIdList).toList();
  }
}
