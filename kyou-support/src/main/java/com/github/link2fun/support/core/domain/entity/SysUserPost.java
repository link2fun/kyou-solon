package com.github.link2fun.support.core.domain.entity;


import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EasyAlias;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserPostProxy;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户和岗位关联 sys_user_post
 *
 * @author ruoyi
 */
@Data
@EntityProxy
@Table("sys_user_post")
@EasyAlias("userPost")
public class SysUserPost implements Serializable, ProxyEntityAvailable<SysUserPost, SysUserPostProxy> {
  /** 用户ID */
  @Column(value = "user_id",primaryKey = true)
  private Long userId;

  /** 岗位ID */
  @Column(value = "post_id",primaryKey = true)
  private Long postId;


}
