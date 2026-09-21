package com.github.link2fun.system.modular.datascope;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.KyouApp;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.easyquery.DataScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据范围判定矩阵与生成 SQL 的渲染测试: 判定断言落在作用域对象上(白名单 / 本人轴 / 两态),
 * SQL 只构造查询取 toSQL 不执行; 每种谓词形状另配一条真实执行断言(同构查询跑真库取回行)。
 *
 * <p>结果正确性由 DataScopeSmokeTest 断言, 这里钉的是「角色解析成什么白名单」与 SQL 形状 ——
 * 白名单并集、in 绑定参数、or 分组结构、恒假兜底; 以及这些 SQL 在真库上能执行、行集符合种子数据
 * (种子: admin=用户1/部门103, ry=用户2/部门105, 角色2 的自定义范围=100/101/105)。
 * 改坏解析或谓词翻译时这里先红。解析需要真实数据(sys_role_dept / sys_dept),
 * 判定矩阵从 kyou-support 的免容器测试迁到这里。
 */
@Import(profiles = "classpath:app-test.yml", scanPackages = "com.github.link2fun")
@SolonTest(value = KyouApp.class)
@ExtendWith(SolonJUnit5Extension.class)
public class DataScopeSqlTest {

  /** 种子数据里的超管用户 ID */
  private static final long SUPER_ADMIN_ID = 1L;
  /** 普通用户 ID / 部门 ID(合成值, 只影响判定与 SQL 文本不影响数据) */
  private static final long USER_ID = 100L;
  private static final long DEPT_ID = 103L;
  /** 种子数据里自定义数据范围的角色 ID, sys_role_dept 挂 100/101/105 */
  private static final long CUSTOM_SCOPE_ROLE_ID = 2L;
  /** 种子数据里「深圳总公司」(101) 的子树: 自身 + 103~107 五个部门 */
  private static final Set<Long> SUBTREE_OF_101 = Set.of(101L, 103L, 104L, 105L, 106L, 107L);

  @Db
  private EasyEntityQuery entityQuery;

  /** 造一个持有指定数据范围角色的操作上下文 */
  private static ActionContext contextOf(final long userId, final long deptId, final long roleId, final String dataScope) {
    return contextOf(userId, deptId, List.of(role(roleId, dataScope)));
  }

  /** 造一个持有多个角色的操作上下文 */
  private static ActionContext contextOf(final long userId, final long deptId, final List<RoleDTO> roles) {
    final SysUserDTO user = new SysUserDTO();
    user.setUserId(userId);
    user.setDeptId(deptId);
    user.setRoles(roles);

    final SessionUser sessionUser = new SessionUser();
    sessionUser.setUserId(userId);
    sessionUser.setDeptId(deptId);
    sessionUser.setUser(user);

    return ActionContext.builder().withUserId(userId).withDeptId(deptId).withSessionUser(sessionUser).build();
  }

  /** 按上下文解析数据范围 */
  private DataScope scopeOf(final ActionContext context) {
    return DataScope.of(entityQuery, context);
  }

  /** 用户列表形态的查询(部门 join 声明别名), 与 SystemUserServiceImpl.selectUserList 同构 */
  private String userListSQL(final ActionContext context) {
    final DataScope dataScope = scopeOf(context);
    return entityQuery.queryable(SysUser.class).asAlias(SysUser.TABLE_ALIAS)
      .leftJoin(SysDept.class, (user, dept) -> user.deptId().eq(dept.deptId())).asAlias(SysDept.TABLE_ALIAS)
      .where((user, dept) -> {
        user.delFlag().eq("0");
        dataScope.applyTo(user, dept.deptId(), user.userId());
      })
      .toSQL();
  }

  /** 执行用户列表形态的查询, 返回用户名集合(与 selectUserList 同构) */
  private Set<String> userListUserNames(final ActionContext context) {
    final DataScope dataScope = scopeOf(context);
    return entityQuery.queryable(SysUser.class).asAlias(SysUser.TABLE_ALIAS)
      .leftJoin(SysDept.class, (user, dept) -> user.deptId().eq(dept.deptId())).asAlias(SysDept.TABLE_ALIAS)
      .where((user, dept) -> {
        user.delFlag().eq("0");
        dataScope.applyTo(user, dept.deptId(), user.userId());
      })
      .toList().stream().map(SysUser::getUserName).collect(Collectors.toSet());
  }

  /** 执行部门列表形态的查询, 返回部门 ID 集合(与 selectDeptList 同构, 没有用户列) */
  private Set<Long> deptListDeptIds(final ActionContext context) {
    final DataScope dataScope = scopeOf(context);
    return entityQuery.queryable(SysDept.class).asAlias(SysDept.TABLE_ALIAS)
      .where(_dept -> {
        _dept.delFlag().eq("0");
        dataScope.applyTo(_dept, _dept.deptId(), null);
      })
      .toList().stream().map(SysDept::getDeptId).collect(Collectors.toSet());
  }

  /** 数 SQL 里 SELECT 子句次数(主查询一次, 多出的就是子查询) */
  private static long selectCount(final String sql) {
    return Pattern.compile("\\bSELECT\\b").matcher(sql).results().count();
  }

  /** 从 SQL 里取部门表在 JOIN 上的实际别名 */
  private static String deptJoinAlias(final String sql) {
    final Matcher matcher = Pattern.compile("LEFT JOIN `sys_dept` (\\S+) ON").matcher(sql);
    assertTrue(matcher.find(), "应能找到 sys_dept 的 JOIN: " + sql);
    return matcher.group(1);
  }

  //
  // 判定矩阵: 角色解析成什么白名单
  //

  @Test
  @DisplayName("超管不过滤, 没有白名单也没有本人轴")
  public void superAdminDoesNotFilter() {
    final DataScope dataScope = scopeOf(contextOf(SUPER_ADMIN_ID, DEPT_ID, 1L, "1"));
    assertFalse(dataScope.filters());
    assertTrue(dataScope.containsDept(DEPT_ID));
  }

  @Test
  @DisplayName("普通用户持有「全部数据」角色时同样不过滤")
  public void allScopeRoleDoesNotFilter() {
    assertFalse(scopeOf(contextOf(USER_ID, DEPT_ID, 999L, "1")).filters());
  }

  @Test
  @DisplayName("自定义数据范围解析成 sys_role_dept 白名单 {100,101,105}")
  public void customScopeResolvesRoleDeptWhitelist() {
    final DataScope dataScope = scopeOf(contextOf(USER_ID, DEPT_ID, CUSTOM_SCOPE_ROLE_ID, "2"));
    assertTrue(dataScope.filters());
    assertEquals(Set.of(100L, 101L, 105L), dataScope.deptScope().visibleDeptIds());
  }

  @Test
  @DisplayName("自定义 + 本部门两个角色并成同一份白名单(OR 即并集)")
  public void customAndDeptScopesUnionIntoOneWhitelist() {
    final DataScope dataScope = scopeOf(contextOf(USER_ID, 108L,
      List.of(role(CUSTOM_SCOPE_ROLE_ID, "2"), role(998L, "3"))));
    assertEquals(Set.of(100L, 101L, 105L, 108L), dataScope.deptScope().visibleDeptIds());
    assertTrue(dataScope.deptScope().contains(108L));
    assertTrue(dataScope.deptScope().contains(100L));
  }

  @Test
  @DisplayName("本部门及以下解析成本部门 + 祖先链后代(深圳总公司 → 101,103~107)")
  public void subtreeScopeResolvesDescendants() {
    final DataScope dataScope = DataScope.of(entityQuery, List.of(role(999L, "4")), USER_ID, 101L);
    assertEquals(SUBTREE_OF_101, dataScope.deptScope().visibleDeptIds());
  }

  @Test
  @DisplayName("仅本人数据范围: 部门白名单为空, 本人轴携带用户 ID")
  public void selfScopeCarriesUserIdOnly() {
    final DataScope dataScope = scopeOf(contextOf(USER_ID, DEPT_ID, 999L, "5"));
    assertTrue(dataScope.filters());
    assertTrue(dataScope.deptScope().visibleDeptIds().isEmpty());
    assertTrue(dataScope.containsUser(USER_ID, null), "本人恒可见");
    assertFalse(dataScope.containsUser(2L, 103L), "他人按部门判定, 白名单为空则不可见");
  }

  @Test
  @DisplayName("未知的数据范围类型视同不授权, 单角色时过滤到空")
  public void unknownScopeFiltersToEmpty() {
    final DataScope dataScope = scopeOf(contextOf(USER_ID, DEPT_ID, 999L, "9"));
    assertTrue(dataScope.filters(), "过滤到空也要加条件(恒假)");
    assertTrue(dataScope.deptScope().visibleDeptIds().isEmpty());
  }

  @Test
  @DisplayName("无角色(未登录)同样过滤到空")
  public void noRolesFiltersToEmpty() {
    final DataScope dataScope = scopeOf(ActionContext.builder().build());
    assertTrue(dataScope.filters());
    assertTrue(dataScope.deptScope().visibleDeptIds().isEmpty());
  }

  @Test
  @DisplayName("部门类范围但上下文没有部门 ID 时给不出白名单, 过滤到空")
  public void deptScopeWithoutDeptIdFiltersToEmpty() {
    final DataScope dataScope = DataScope.of(entityQuery, List.of(role(999L, "3")), USER_ID, null);
    assertTrue(dataScope.deptScope().visibleDeptIds().isEmpty());
  }

  @Test
  @DisplayName("解析不修改传入的角色集合")
  public void rolesAreNotMutated() {
    final List<RoleDTO> roles = new ArrayList<>(List.of(role(999L, "3")));
    DataScope.of(entityQuery, roles, USER_ID, DEPT_ID);
    assertEquals(1, roles.size());
  }

  //
  // SQL 形状: 谓词翻译
  //

  @Test
  @DisplayName("超管不过滤: 生成的 SQL 与不加工条件的基准完全一致")
  public void superAdminAddsNothing() {
    final String baseline = entityQuery.queryable(SysUser.class).asAlias(SysUser.TABLE_ALIAS)
      .leftJoin(SysDept.class, (user, dept) -> user.deptId().eq(dept.deptId())).asAlias(SysDept.TABLE_ALIAS)
      .where((user, dept) -> user.delFlag().eq("0"))
      .toSQL();

    assertEquals(baseline, userListSQL(contextOf(SUPER_ADMIN_ID, DEPT_ID, 1L, "1")), "超管不应给查询加任何条件");
  }

  @Test
  @DisplayName("自定义范围: 白名单以绑定参数内联, 不再往主查询里牵子查询")
  public void customScopeRendersInWhitelist() {
    final String sql = userListSQL(contextOf(USER_ID, DEPT_ID, CUSTOM_SCOPE_ROLE_ID, "2"));

    assertTrue(sql.contains("dept.`dept_id` IN (?,?,?)"), "白名单三个部门应是绑定参数的 IN: " + sql);
    assertEquals(1, selectCount(sql), "白名单已解析内联, 主查询不应再有子查询: " + sql);
    assertFalse(sql.contains("sys_role_dept"), "自定义范围在解析期查掉, SQL 里不应出现: " + sql);
  }

  @Test
  @DisplayName("仅本人: 只有用户列等值谓词, 不给部门列加条件")
  public void selfScopeRendersUserIdPredicateOnly() {
    final String sql = userListSQL(contextOf(USER_ID, DEPT_ID, 999L, "5"));

    assertTrue(sql.endsWith("WHERE user.`del_flag` = ? AND user.`user_id` = ?"),
      "WHERE 里应只有用户列等值谓词, 不给部门列加条件: " + sql);
    assertFalse(sql.contains(" OR "), "白名单为空时本人条件独自救场, 不应有 OR 分组: " + sql);
  }

  @Test
  @DisplayName("部门及以下: 白名单同样以绑定参数内联(子树在解析期查掉)")
  public void subtreeScopeRendersInWhitelist() {
    final String sql = userListSQL(contextOf(USER_ID, 101L, 999L, "4"));

    assertTrue(sql.contains("dept.`dept_id` IN ("), "应是 IN 白名单: " + sql);
    assertFalse(sql.contains("find_in_set"), "祖先链在解析期展开, 主查询不应再有 find_in_set: " + sql);
    assertFalse(sql.contains("sys_role_dept"), "不应牵出自定义范围子查询: " + sql);
  }

  @Test
  @DisplayName("白名单与本人授权同时在场: 一个括号分组内以 OR 连接")
  public void whitelistAndSelfJoinedWithOr() {
    final ActionContext both = contextOf(USER_ID, DEPT_ID,
      List.of(role(CUSTOM_SCOPE_ROLE_ID, "2"), role(999L, "5")));
    final String sql = userListSQL(both);

    assertTrue(sql.contains(" OR "), "白名单与本人条件应以 OR 连接: " + sql);
    assertTrue(sql.contains("dept.`dept_id` IN (?,?,?)"), "含白名单片段: " + sql);
    assertTrue(sql.contains("user.`user_id` = ?"), "含本人片段: " + sql);
  }

  @Test
  @DisplayName("过滤到空(不放行): 渲染成部门列的恒假谓词")
  public void denyRendersFalsePredicate() {
    final String sql = userListSQL(ActionContext.builder().build());

    assertTrue(sql.contains("dept.`dept_id` = ?"), "不放行是 dept_id = 0 的等值谓词: " + sql);
  }

  @Test
  @DisplayName("查询没写 asAlias 时, 谓词列落到 JOIN 的自动别名 —— 别名失配在结构上不可能")
  public void predicateAliasFollowsQueryWithoutAsAlias() {
    final DataScope dataScope = scopeOf(contextOf(USER_ID, DEPT_ID, CUSTOM_SCOPE_ROLE_ID, "2"));
    final String sql = entityQuery.queryable(SysUser.class).asAlias(SysUser.TABLE_ALIAS)
      .leftJoin(SysDept.class, (user, dept) -> user.deptId().eq(dept.deptId()))
      .where((user, dept) -> {
        user.delFlag().eq("0");
        dataScope.applyTo(user, dept.deptId(), user.userId());
      })
      .toSQL();

    final String joinAlias = deptJoinAlias(sql);
    assertTrue(sql.contains(joinAlias + ".`dept_id` IN ("),
      "谓词里的部门列应与 JOIN 用同一个自动别名 " + joinAlias + ": " + sql);
  }

  //
  // 真实执行: 同构查询跑真库, 断言行集符合种子数据
  // (admin=用户1/部门103, ry=用户2/部门105)
  //

  @Test
  @DisplayName("自定义范围执行: 用户查询只回范围内部门的 ry, 部门查询只回白名单三个部门")
  public void customScopeExecutesAndFiltersRows() {
    final ActionContext context = contextOf(USER_ID, DEPT_ID, CUSTOM_SCOPE_ROLE_ID, "2");

    assertEquals(Set.of("ry"), userListUserNames(context), "admin 的部门 103 不在白名单 {100,101,105}: ");
    assertEquals(Set.of(100L, 101L, 105L), deptListDeptIds(context));
  }

  @Test
  @DisplayName("部门及以下执行: 子树 101 覆盖 admin(103) 与 ry(105), 部门查询回整棵子树")
  public void subtreeScopeExecutesAndCoversDescendants() {
    final ActionContext context = contextOf(USER_ID, 101L, 999L, "4");

    assertEquals(Set.of("admin", "ry"), userListUserNames(context));
    assertEquals(SUBTREE_OF_101, deptListDeptIds(context));
  }

  @Test
  @DisplayName("仅本人执行: 用户查询回自己; 部门查询没有用户列, 恒假回空且 SQL 可执行")
  public void selfScopeExecutesOnBothQueryShapes() {
    final ActionContext context = contextOf(2L, 105L, 999L, "5");

    assertEquals(Set.of("ry"), userListUserNames(context));
    assertTrue(deptListDeptIds(context).isEmpty(), "本人轴在部门查询上给不出条件, 恒假谓词应可执行并回空");
  }

  @Test
  @DisplayName("白名单与本人 OR 执行: admin 靠部门(103 在子树内)、ry 靠本人, 两人都能查出来")
  public void whitelistAndSelfBothRescueRows() {
    // 子树(103) = {103}, ry 的部门 105 不在其中, 靠本人轴救场
    final ActionContext context = contextOf(2L, 103L, List.of(role(999L, "4"), role(998L, "5")));

    assertEquals(Set.of("admin", "ry"), userListUserNames(context));
  }

  @Test
  @DisplayName("不放行执行: 恒假谓词在两种查询上都能执行, 行集为空而非报错")
  public void denyPredicateExecutesToEmptyRows() {
    final ActionContext context = ActionContext.builder().build();

    assertTrue(userListUserNames(context).isEmpty());
    assertTrue(deptListDeptIds(context).isEmpty());
  }

  @Test
  @DisplayName("部门查询携带本人轴时: 本人条件静默丢弃, 只按白名单过滤")
  public void selfAxisDroppedOnDeptQuery() {
    final ActionContext context = contextOf(2L, 105L,
      List.of(role(CUSTOM_SCOPE_ROLE_ID, "2"), role(999L, "5")));

    assertEquals(Set.of(100L, 101L, 105L), deptListDeptIds(context), "本人轴不该影响部门查询");
    assertEquals(Set.of("ry"), userListUserNames(context), "用户查询上两条轴都在场, ry 两条路都通");
  }

  /** 造角色 */
  private static RoleDTO role(final Long roleId, final String dataScope) {
    final RoleDTO role = new RoleDTO();
    role.setRoleId(roleId);
    role.setDataScope(dataScope);
    return role;
  }
}
