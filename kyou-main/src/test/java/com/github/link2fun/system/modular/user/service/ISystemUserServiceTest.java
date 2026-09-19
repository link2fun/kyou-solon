package com.github.link2fun.system.modular.user.service;
import com.github.link2fun.system.modular.user.model.req.SysUserReq;

import com.github.link2fun.KyouApp;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;
import org.noear.solon.test.annotation.Rollback;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@Import(profiles = "classpath:app-test.yml", scanPackages = "com.github.link2fun")
@SolonTest(value = KyouApp.class)
@ExtendWith(SolonJUnit5Extension.class)
public class ISystemUserServiceTest {

  @Inject
  private ISystemUserService userService;

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
}