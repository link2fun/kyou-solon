package com.github.link2fun.system.modular.authorization;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.KyouApp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Import;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 后端注解集必须与根目录 permission-codes.txt 一致
 */
@Import(profiles = "classpath:app-test.yml", scanPackages = "com.github.link2fun")
@SolonTest(value = KyouApp.class)
@ExtendWith(SolonJUnit5Extension.class)
public class PermissionCodeArtifactIT {

  /** 收集全部 bean 上 @SaCheckPermission 的码值 */
  private static Set<String> collectAnnotatedCodes() {
    final Set<String> codes = new TreeSet<>();
    Solon.context().beanForeach(beanWrap -> {
      final Class<?> clz = beanWrap.rawClz();
      final SaCheckPermission onClass = clz.getAnnotation(SaCheckPermission.class);
      if (onClass != null) {
        codes.addAll(List.of(onClass.value()));
      }
      for (final Method method : clz.getMethods()) {
        final SaCheckPermission onMethod = method.getAnnotation(SaCheckPermission.class);
        if (onMethod != null) {
          codes.addAll(List.of(onMethod.value()));
        }
      }
    });
    return codes;
  }

  /** 读取签入的权限码清单 */
  private static Set<String> readArtifact() throws IOException {
    final Set<String> codes = new TreeSet<>();
    for (final String line : Files.readAllLines(Path.of("..", "permission-codes.txt"), StandardCharsets.UTF_8)) {
      final String trimmed = line.trim();
      if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
        codes.add(trimmed);
      }
    }
    return codes;
  }

  @Test
  @DisplayName("后端注解集与签入的权限码清单完全一致")
  public void artifactMatchesAnnotations() throws IOException {
    final Set<String> annotated = collectAnnotatedCodes();
    final Set<String> artifact = readArtifact();
    assertFalse(annotated.isEmpty(), "未收集到任何权限码, 扫描配置可能变了");
    assertEquals(artifact, annotated, "权限码漂移: 请同步根目录 permission-codes.txt");
  }
}
