package com.github.link2fun.hello.entity;

import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Navigate;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.enums.RelationTypeEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.hello.entity.proxy.UserAccountProxy;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;

@Data
@FieldNameConstants
@EntityProxy
@Table("demo_user_account")
public class UserAccount implements ProxyEntityAvailable<UserAccount, UserAccountProxy> {
  /** 用户账户的唯一标识符 */
  private String id;

  /** 用户的唯一标识符 */
  private String userId;

  /** 平台名称 */
  private String platform;

  /** 用户昵称 */
  private String nickName;

  /** 用户的联合ID */
  private String unionId;

  /** 用户的开放ID */
  private String openId;

  /** 账户余额 */
  private BigDecimal amount;

  @Navigate(value = RelationTypeEnum.OneToMany, selfProperty = Fields.userId, targetProperty = User.Fields.id)
  /** 关联的用户信息 */
  private User user;
}
