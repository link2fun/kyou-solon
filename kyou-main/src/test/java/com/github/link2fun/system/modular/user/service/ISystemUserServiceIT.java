package com.github.link2fun.system.modular.user.service;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.exception.EasyQuerySingleMoreElementException;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.KyouApp;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserProxy;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.system.modular.user.model.req.SysUserReq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;
import org.noear.solon.test.annotation.Rollback;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(profiles = "classpath:app-test.yml", scanPackages = "com.github.link2fun")
@SolonTest(value = KyouApp.class)
@ExtendWith(SolonJUnit5Extension.class)
public class ISystemUserServiceIT {

  @Inject
  private ISystemUserService userService;

  @Db
  private EasyEntityQuery entityQuery;

  /** 按用户名查询 admin, 校验部门与角色信息一并带出 */
  @Test
  public void selectUserByUserName() {
    final SysUserDTO admin = userService.selectUserByUserName("admin");
    assertNotNull(admin.getDeptId(), "部门主键不为空");
    assertNotNull(admin.getDept(), "部门信息不能为空");
    assertNotNull(admin.getDept().getDeptId());
    assertNotNull(admin.getDept().getDeptName());
    assertNotNull(admin.getRoles());
    assertFalse(admin.getRoles().isEmpty());
  }

  /**
   * 用户名查无此人时返回 null 而不是抛异常。
   *
   * <p>登录流程据 null 走「用户不存在」分支并与密码错误同文案, 抛出异常会泄漏用户是否存在。
   */
  @Test
  public void selectUserByUserNameReturnsNullWhenAbsent() {
    assertNull(userService.selectUserByUserName("no-such-user-990001"));
  }


  /** 新增用户后按 ID 回查, 校验字段一致 */
  @Rollback
  @Test
  public void testSelectUserById() {

    SysUserReq.AddReq user = new SysUserReq.AddReq();
    user.setDeptId(0L);
    user.setUserName("username");
    user.setNickName("nickname");
    user.setEmail("email@example.com");
    user.setPhonenumber("1234567890");
    user.setSex("M");
    user.setAvatar("avatar_url");
    user.setPassword("password");
    user.setRoleIds(new ArrayList<>());
    user.setPostIds(new ArrayList<>());
    user.setCreateBy("creator");
    user.setRemark("remark");

    Long userId = userService.insertUser(user);

    SysUserDTO userDTO = userService.selectUserById(userId);
    assertNotNull(userDTO);
    assertEquals(user.getUserName(), userDTO.getUserName());
  }

  /** 新增与已有用户同名的用户被唯一性检查拒绝 */
  @Rollback
  @Test
  public void insertUserDuplicateUserNameRejected() {
    SysUserReq.AddReq user = new SysUserReq.AddReq();
    user.setDeptId(0L);
    user.setUserName("admin");
    user.setNickName("dup");
    user.setPassword("password");
    user.setRoleIds(new ArrayList<>());
    user.setPostIds(new ArrayList<>());

    final ServiceException e = assertThrows(ServiceException.class, () -> userService.insertUser(user));
    assertEquals("新增用户'admin'失败，登录账号已存在", e.getMessage());
  }

  /**
   * 导入不存在的账号走新增分支并成功。
   *
   * <p>importUser 用 null 表示「新用户」, 查无此人若抛异常会被它的 catch 计为导入失败。
   */
  @Rollback
  @Test
  public void importUserInsertsBrandNewAccount() {
    final String userName = "import-new-990002";
    final SysUserDTO row = new SysUserDTO();
    row.setUserName(userName);
    row.setNickName("导入新账号");

    final String message = userService.importUser(new ActionContext(), List.of(row), false, "admin");

    assertTrue(message.contains("导入成功"), message);
    assertNotNull(userService.selectUserByUserName(userName), "新账号应已落库");
  }

  /**
   * 同名多行是数据错误(用户名应唯一), 查询侧抛异常暴露, 而不是静默取首行掩盖。
   */
  @Rollback
  @Test
  public void duplicateUserNameExposesDirtyData() {
    final String userName = "dup-user-990003";
    insertSynthUser(userName, "同名一号");
    insertSynthUser(userName, "同名二号");
    assertEquals(2, idsOfUserName(userName).size(), "前置: 库中已有两行同名账号");

    assertThrows(EasyQuerySingleMoreElementException.class, () -> userService.selectUserByUserName(userName));
  }

  /** 同名多行时按用户名查ID同样抛异常暴露脏数据 */
  @Rollback
  @Test
  public void findUserIdByUserNameThrowsOnDuplicateUserName() {
    final String userName = "dup-user-990004";
    insertSynthUser(userName, "同名一号");
    insertSynthUser(userName, "同名二号");
    assertEquals(2, idsOfUserName(userName).size(), "前置: 库中已有两行同名账号");

    assertThrows(EasyQuerySingleMoreElementException.class, () -> userService.findUserIdByUserName(userName));
  }

  /** 同名多行时角色组查询同样抛异常暴露脏数据 */
  @Rollback
  @Test
  public void selectUserRoleGroupThrowsOnDuplicateUserName() {
    final String userName = "dup-user-990005";
    insertSynthUser(userName, "同名一号");
    insertSynthUser(userName, "同名二号");
    assertEquals(2, idsOfUserName(userName).size(), "前置: 库中已有两行同名账号");

    assertThrows(EasyQuerySingleMoreElementException.class, () -> userService.selectUserRoleGroup(userName));
  }

  /** 按用户名取全部 userId, 升序 */
  private List<Long> idsOfUserName(final String userName) {
    return entityQuery.queryable(SysUser.class)
      .where(user -> user.userName().eq(userName))
      .orderBy(user -> user.userId().asc())
      .selectColumn(SysUserProxy::userId)
      .toList();
  }

  /** 直接落库一条合成用户 */
  private void insertSynthUser(final String userName, final String nickName) {
    final SysUser user = new SysUser();
    user.setUserName(userName);
    user.setNickName(nickName);
    user.setPassword("encoded-for-test");
    assertTrue(userService.registerUser(user), "前置: 合成用户落库成功");
  }
}
