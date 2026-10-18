package com.github.link2fun.config.satoken;

import cn.dev33.satoken.json.SaJsonTemplateForJackson;
import cn.dev33.satoken.strategy.SaJsonStrategy;
import com.github.link2fun.support.core.domain.dto.DeptDTO;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.model.SessionUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * sa-token 1.46 起 Jackson 多态反序列化启用全局类型白名单(SaJsonStrategy),
 * 会话里缓存的业务对象必须经 kyou-support 的 META-INF/satoken/sa-json-type.list 声明。
 *
 * <p>这里钉两件事: SPI 声明被 SaJsonStrategy 拾取; 会话对象图(SessionUser -> SysUserDTO -> DeptDTO/RoleDTO)
 * 能带着 @class 类型信息经 sa-token-jackson 模板往返。新增会话缓存类型而漏登记时这里先红。
 */
public class SaJsonWhitelistTest {

  @Test
  @DisplayName("SPI 声明的会话业务类型全部进入 SaJsonStrategy 白名单")
  public void spiDeclaredTypesEnterWhitelist() {
    final List<Class<?>> allowTypes = SaJsonStrategy.instance.getSaJsonAllowTypeList();
    assertTrue(allowTypes.containsAll(List.of(
      SessionUser.class, SysUserDTO.class, DeptDTO.class, RoleDTO.class)),
      "白名单缺少会话业务类型, 检查 sa-json-type.list: " + allowTypes);
  }

  @Test
  @DisplayName("SessionUser 对象图经 sa-token-jackson 模板多态往返不丢字段")
  public void sessionUserRoundTripThroughSaTokenJackson() {
    final DeptDTO dept = new DeptDTO();
    dept.setDeptId(103L);
    dept.setDeptName("研发部门");

    final RoleDTO role = new RoleDTO();
    role.setRoleId(2L);
    role.setRoleKey("common");
    role.setStatus("0");

    final SysUserDTO userDTO = new SysUserDTO();
    userDTO.setUserId(1L);
    userDTO.setUserName("admin");
    userDTO.setDept(dept);
    userDTO.setRoles(List.of(role));

    final SessionUser sessionUser = new SessionUser();
    sessionUser.setUserId(1L);
    sessionUser.setDeptId(103L);
    sessionUser.setPermissions(List.of("system:user:list"));
    sessionUser.setUser(userDTO);

    final SaJsonTemplateForJackson template = new SaJsonTemplateForJackson();
    final String json = template.objectToJson(sessionUser);
    final SessionUser back = template.jsonToObject(json, SessionUser.class);

    assertEquals(sessionUser.getUserId(), back.getUserId());
    assertEquals(sessionUser.getPermissions(), back.getPermissions());
    assertEquals(userDTO.getUserName(), back.getUser().getUserName());
    assertEquals(dept.getDeptName(), back.getUser().getDept().getDeptName());
    assertEquals(role.getRoleKey(), back.getUser().getRoles().get(0).getRoleKey());
  }
}
