package com.github.link2fun.schedule.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 调用目标判据: 保存期(checkJobFields)与运行期(invokeMethod)共用的同一份结论
 */
class JobInvokeUtilTest {

  @Test
  @DisplayName("白名单内的方法通过: 无参、有参、多参")
  void whitelistedTargetsPass() {
    assertNull(JobInvokeUtil.violation("ryTask.ryNoParams"));
    assertNull(JobInvokeUtil.violation("ryTask.ryParams('ry')"));
    assertNull(JobInvokeUtil.violation("ryTask.ryMultipleParams('ry', true, 2000L, 316.50D, 100)"));
  }

  @Test
  @DisplayName("白名单外的方法被拒: 同 bean 的其它方法")
  void unknownMethodOfWhitelistedBeanRejected() {
    final String violation = JobInvokeUtil.violation("ryTask.noSuchMethod");

    assertNotNull(violation);
    assertTrue(violation.contains("调用目标不在方法白名单内"), "原因应指明方法级白名单: " + violation);
  }

  @Test
  @DisplayName("其它 bean 的方法被拒")
  void otherBeanRejected() {
    assertNotNull(JobInvokeUtil.violation("demoTask.run()"));
  }

  @Test
  @DisplayName("协议前缀被拒: rmi/ldap/http(s)")
  void protocolPrefixesRejected() {
    assertNotNull(JobInvokeUtil.violation("rmi://127.0.0.1/x"));
    assertNotNull(JobInvokeUtil.violation("ldap://127.0.0.1/x"));
    assertNotNull(JobInvokeUtil.violation("ldaps://127.0.0.1/x"));
    assertNotNull(JobInvokeUtil.violation("http://127.0.0.1/x"));
    assertNotNull(JobInvokeUtil.violation("https://127.0.0.1/x"));
  }

  @Test
  @DisplayName("类名形式被拒, 不走反射执行任意类")
  void classNameRejected() {
    final String violation = JobInvokeUtil.violation("com.foo.Bar.baz()");

    assertNotNull(violation);
    assertTrue(violation.contains("不支持类名形式"), "原因应指明类名形式: " + violation);
  }

  @Test
  @DisplayName("空白调用目标被拒")
  void blankTargetRejected() {
    assertEquals("调用目标不能为空", JobInvokeUtil.violation(null));
    assertEquals("调用目标不能为空", JobInvokeUtil.violation("  "));
  }

  @Test
  @DisplayName("违规字符被拒")
  void illegalCharactersRejected() {
    assertNotNull(JobInvokeUtil.violation("ryTask.ryParams('org.springframework.context.ApplicationContext')"));
  }
}
