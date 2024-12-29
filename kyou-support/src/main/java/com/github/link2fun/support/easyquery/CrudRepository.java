package com.github.link2fun.support.easyquery;

import com.easy.query.api.proxy.entity.select.EntityQueryable;
import com.easy.query.core.proxy.ProxyEntity;
import com.easy.query.core.proxy.ProxyEntityAvailable;

public interface CrudRepository<TProxy extends ProxyEntity<TProxy, T>, T extends ProxyEntityAvailable<T,TProxy>> {
    Class<T> tableClass();
    EntityQueryable<TProxy, T> getQuery();//获取当前的仓储用于查询
}
