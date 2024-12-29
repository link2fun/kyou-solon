package com.github.link2fun.framework.tool;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * 日志工具类<br/>
 * 1. 用于获取方法行号
 * 
 * @author link2fun
 */
@Slf4j
public class LogTool {

  /** 获取方法行号 caffeine LoadingCache */
  private static final LoadingCache<Method, Integer> METHOD_LINE_NUMBER_CACHE = Caffeine.newBuilder().maximumSize(10000)
      .build(method -> {
        try {
          return getMethodLineNumber(method);
        } catch (Exception e) {
          log.error("获取方法行号失败", e);
          return 0;
        }
      });

  /**
   * 获取方法行号, 在控制台打印 以便定位
   *
   * @param method 方法
   * @return 方法行号
   * @throws NotFoundException 未找到异常
   */
  public static int getMethodLineNumber(final Method method) throws NotFoundException {
    final Class<?> declaringClass = method.getDeclaringClass();
    final String methodName = method.getName();

    // 从池中获取类
    ClassPool pool = ClassPool.getDefault();
    // 获取类文件
    CtClass classFile = pool.get(declaringClass.getName());
    // 获取方法
    CtMethod methodFile = classFile.getDeclaredMethod(methodName);
    // 获取方法行号
    return methodFile.getMethodInfo().getLineNumber(0);
  }

  /** 从缓存中获取方法行号 */
  public static Integer getMethodLineNumberFromCache(Method method) {
    return METHOD_LINE_NUMBER_CACHE.get(method);
  }
}
