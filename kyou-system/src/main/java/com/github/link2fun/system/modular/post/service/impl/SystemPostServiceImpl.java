package com.github.link2fun.system.modular.post.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.api.proxy.entity.select.EntityQueryable;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.aggregatedelete.AggregateDeletePlan;
import com.github.link2fun.support.aggregatedelete.AggregateDeleter;
import com.github.link2fun.support.core.domain.entity.SysPost;
import com.github.link2fun.support.core.domain.entity.proxy.SysPostProxy;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.easyquery.UniqueChecker;
import com.github.link2fun.support.utils.uuid.IdUtils;
import com.github.link2fun.system.modular.post.model.req.SysPostAddReq;
import com.github.link2fun.system.modular.post.model.req.SysPostModifyReq;
import com.github.link2fun.system.modular.post.model.req.SysPostSearchReq;
import com.github.link2fun.system.modular.post.service.ISystemPostService;
import com.github.link2fun.system.modular.userpost.UserPostRelation;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.List;
import java.util.Objects;


@Slf4j
@Component
public class SystemPostServiceImpl implements ISystemPostService {

  @Inject
  private UserPostRelation userPostRelation;

  @Inject
  private AggregateDeleter aggregateDeleter;

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 查询岗位信息集合
   *
   * @param searchReq 岗位查询条件
   * @return 岗位列表
   */
  @Override
  public List<SysPost> selectPostList(final SysPostSearchReq searchReq) {
    return buildQuery(searchReq).toList();
  }

  /** 构造岗位列表查询 */
  private EntityQueryable<SysPostProxy, SysPost> buildQuery(final SysPostSearchReq searchReq) {
    EntityQueryable<SysPostProxy, SysPost> queryable = entityQuery.queryable(SysPost.class);
    if (Objects.isNull(searchReq)) {
      return queryable;
    }
    return queryable.where(post -> {
      post.postCode().like(StrUtil.isNotBlank(searchReq.getPostCode()), searchReq.getPostCode()); // 岗位编码
      post.postName().like(StrUtil.isNotBlank(searchReq.getPostName()), searchReq.getPostName()); // 岗位名称
      post.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus()); // 状态
    });
  }

  /**
   * 根据搜索条件进行分页查询岗位信息。
   *
   * @param pageRequest 分页适配器
   * @param searchReq   搜索条件
   * @return 分页结果
   */
  @Override
  public Page<SysPost> pageSearch(final Page<SysPost> pageRequest, final SysPostSearchReq searchReq) {
    return Page.of(pageRequest, buildQuery(searchReq).toPageResult(pageRequest.getPageNum(), pageRequest.getPageSize(), pageRequest.getTotal()));
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
      .where(post -> post.userPostList().any(userPost -> userPost.userId().eq(userId)))
      .selectColumn(SysPostProxy::postId)
      .toList();
  }

  /**
   * 校验岗位名称/编码在系统中是否唯一, 不唯一时抛出业务异常
   *
   * @param action 操作描述(如 "新增"/"修改"), 用于拼接错误文案
   * @param post   岗位信息
   */
  private void checkPostFieldUnique(final String action, final SysPost post) {
    UniqueChecker.checkOccupied(entityQuery.queryable(SysPost.class)
        .where(_post -> {
          _post.postName().eq(post.getPostName());
          _post.postId().ne(IdUtils.isIdValid(post.getPostId()), post.getPostId());
        }).any(),
      () -> action + "岗位'" + post.getPostName() + "'失败，岗位名称已存在");
    UniqueChecker.checkOccupied(entityQuery.queryable(SysPost.class)
        .where(_post -> {
          _post.postCode().eq(post.getPostCode());
          _post.postId().ne(IdUtils.isIdValid(post.getPostId()), post.getPostId());
        }).any(),
      () -> action + "岗位'" + post.getPostName() + "'失败，岗位编码已存在");
  }


  /**
   * 批量删除岗位信息（静默解绑: 顺带清除岗位的用户关联）
   *
   * @param postIds 需要删除的岗位ID
   * @return 结果
   */
  @Override
  @Transaction
  public long deletePostByIds(final List<Long> postIds) {
    return aggregateDeleter.delete(AggregateDeletePlan.of(postIds)
      // 删除岗位与用户关联
      .unlink(userPostRelation::removeByPostIds)
      .delete(ids -> entityQuery.deletable(SysPost.class)
        .where(post -> post.postId().in(ids))
        .allowDeleteStatement(true)
        .executeRows())
    );
  }

  /**
   * 新增保存岗位信息
   *
   * @param addReq 新增岗位请求
   * @return 结果
   */
  @Transaction
  @Override
  public long insertPost(final SysPostAddReq addReq) {
    final SysPost post = BeanUtil.toBean(addReq, SysPost.class);
    checkPostFieldUnique("新增", post);
    return entityQuery.insertable(post)
      .executeRows();
  }

  /**
   * 修改保存岗位信息
   *
   * @param modifyReq 修改岗位请求
   * @return 结果
   */
  @Transaction
  @Override
  public long updatePost(final SysPostModifyReq modifyReq) {
    final SysPost post = BeanUtil.toBean(modifyReq, SysPost.class);
    checkPostFieldUnique("修改", post);
    return entityQuery.updatable(post)
      .executeRows();
  }

  @Override
  public List<SysPost> listByIds(List<Long> postIdList) {
    return entityQuery.queryable(SysPost.class)
      .whereByIds(postIdList).toList();
  }
}
