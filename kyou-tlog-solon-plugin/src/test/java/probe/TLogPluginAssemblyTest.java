package probe;

import com.github.link2fun.tlog.filter.TLogFilter;
import com.github.link2fun.tlog.interceptor.TLogJobInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.Solon;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 插件自装配契约: 测试类自身作启动类, 包名刻意不在 com.github.link2fun 下, 宿主包扫描够不到 tlog 的包
 */
@ExtendWith(SolonJUnit5Extension.class)
@SolonTest
public class TLogPluginAssemblyTest {

  @Test
  @DisplayName("宿主扫描不覆盖 tlog 包时, TLogFilter 与 TLogJobInterceptor 仍由插件注册")
  public void pluginRegistersItsOwnBeans() {
    assertTrue(Solon.context().hasWrap(TLogFilter.class), "TLogFilter 未注册");
    assertTrue(Solon.context().hasWrap(TLogJobInterceptor.class), "TLogJobInterceptor 未注册");
  }
}
