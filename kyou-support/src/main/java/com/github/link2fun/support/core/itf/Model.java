package com.github.link2fun.support.core.itf;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;

import java.io.Serializable;

/**
 * 基础模型
 * 提供增强功能
 */
public interface Model extends Serializable, Cloneable {


    /**
     * 将 当前对象 转换成 另一类型 借助 {@link BeanUtil#copyProperties } 进行拷贝属性
     *
     * @param targetClass 目标类型
     * @param <T>         目标类泛型
     * @return 转换后的对象
     */
    default <T> T transferTo(Class<T> targetClass) {
        T target = ReflectUtil.newInstanceIfPossible(targetClass);
        BeanUtil.copyProperties(this, target);
        return target;
    }

    /**
     * 从外部对象 拷贝属性
     *
     * @param target 来源对象
     * @param <T>    被赋值的对象泛型
     * @param <R>    来源对象泛型
     * @return 被赋值后的对象
     */
    @SuppressWarnings("unchecked")
    default <T extends Model, R> T copyPropertiesFromTarget(R target) {
        BeanUtil.copyProperties(target, this);
        return (T) this;
    }

    /**
     * 序列化后拷贝流的方式克隆<br>
     * 对象必须实现Serializable接口
     *
     * @param <T> 对象类型
     * @return 克隆后的对象
     * @throws UtilException IO异常和ClassNotFoundException封装
     */
    @SuppressWarnings("unchecked")
    default <T extends Model, R> T cloneByStream() {
        return (T) ObjectUtil.cloneByStream(this);
    }
}
