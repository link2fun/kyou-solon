package com.github.link2fun.hello.entity;

import com.easy.query.core.annotation.*;
import com.easy.query.core.enums.RelationTypeEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.link2fun.hello.entity.proxy.UserProxy;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.noear.solon.validation.annotation.NotNull;

import java.util.List;

@Data
@EntityProxy
@FieldNameConstants
@Table(value = "demo_user")
public class User implements ProxyEntityAvailable<User, UserProxy> {
  /** 用户的唯一标识符 */
  @Column(primaryKey = true,generatedKey = true)
  private String id;

  /** 用户的姓名 */
  private String name;

  /** 密码 */
  @Getter
  @JsonFormat
  @JsonProperty(value = "hello")
  private String password;

  /** 用户的账户列表 */
  @NotNull
  @Navigate(value = RelationTypeEnum.OneToMany, selfProperty = Fields.id, targetProperty = UserAccount.Fields.userId, orderByProps = {
      @OrderByProperty(property = UserAccount.Fields.platform) })
  private List<UserAccount> accountList;

}
