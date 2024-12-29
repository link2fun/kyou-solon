package com.github.link2fun;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserProxy;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserRoleProxy;
import org.junit.jupiter.api.Test;

import java.util.List;

public class EasyQueryTest {

  @Db
  private EasyEntityQuery entityQuery;

  @Test
  public void test() {


    // 根据ID 查询第一条数据
    SysUser firstUser = entityQuery.queryable(SysUser.class)
      .whereById("1").firstOrNull();
    assert firstUser != null;

    // 断言只有一条
    SysUser singleUser = entityQuery.queryable(SysUser.class)
      .whereById("1").singleNotNull();
    assert singleUser != null;

    // 根据登录名查询唯一一条
    SysUser systemUser = entityQuery.queryable(SysUser.class)
      .where(user -> user.userName().eq("admin"))
      .singleNotNull();
    assert systemUser != null;

    // 判断 admin 用户是否存在
    boolean adminExists = entityQuery.queryable(SysUser.class)
      .where(user -> user.userName().eq("admin"))
      .any();

    assert adminExists;


    // 联查， 查询有 roleId = 1 的用户
    List<SysUserRole> userList_hasRole = entityQuery.queryable(SysUser.class).asAlias("user")
      .innerJoin(SysUserRole.class, (user, userRole) -> user.userId().eq(userRole.userId())).asAlias("user_role")
      .where((user, userRole) -> userRole.roleId().eq(1L))
      .select((user, userRole) -> new SysUserRoleProxy()
        .roleId().set(userRole.roleId())
        .userId().set(user.userId()))
      .toList();

    System.out.println(userList_hasRole.size());


    List<SysUserDTO> list = entityQuery.queryable(SysUser.class)
      .leftJoin(SysDept.class, (user, dept) -> user.deptId().eq(dept.deptId()))
      .leftJoin(SysUserRole.class, (user, dept, userRole) -> user.userId().eq(userRole.userId()))
      .leftJoin(SysRole.class, (user, dept, userRole, role) -> userRole.roleId().eq(role.roleId()))
//      .select(SysUserDTO.class, (user, dept, userRole, role) -> Select.of(
//        user.FETCHER.allFields(),
//        dept.deptName()
//      ))
      .selectAutoInclude(SysUserDTO.class)
      .toList();

    System.out.println(list);


    List<SysUser> list1 = entityQuery.queryable(SysUser.class)

      .leftJoin(SysDept.class, (user, dept) -> user.deptId().eq(dept.deptId()))
      .leftJoin(SysUserRole.class, (user, dept, userRole) -> user.userId().eq(userRole.userId()))
      .leftJoin(SysRole.class, (user, dept, userRole, role) -> userRole.roleId().eq(role.roleId()))
//      .where(s -> s.dept().deptName().eq("测试部门"))
      .where((user, dept, userRole, role) -> dept.deptName().eq("测试部门"))
      .include(SysUserProxy::dept)
      .includes(SysUserProxy::roles)
      .toList();
    System.out.println(list1.size());

  }

}
