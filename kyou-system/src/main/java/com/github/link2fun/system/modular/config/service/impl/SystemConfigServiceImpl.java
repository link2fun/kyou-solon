package com.github.link2fun.system.modular.config.service.impl;

import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.context.config.ConfigContext;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.system.modular.config.model.SysConfig;
import com.github.link2fun.system.modular.config.model.proxy.SysConfigProxy;
import com.github.link2fun.system.modular.config.service.ISystemConfigService;
import com.google.common.base.Preconditions;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Tran;
import org.noear.solon.data.tran.TranPolicy;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class SystemConfigServiceImpl implements ISystemConfigService {

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 查询参数配置信息
   *
   * @param context     上下文
   * @param configId    参数配置ID
   * @param resultClass 结果类型
   * @return 参数配置信息
   */
  @Override
  @Tran(policy = TranPolicy.supports)
  public <T> T findOneByConfigId(ActionContext context, final Integer configId, Class<T> resultClass) {
    Preconditions.checkNotNull(configId, "configId is null");

    return entityQuery.queryable(SysConfig.class)
        .whereById(configId)
        .selectAutoInclude(resultClass)
        .singleOrNull();
  }

  /**
   * 根据键名查询参数配置信息
   *
   * @param configKey 参数键名
   * @return 参数键值
   */
  @Override
  public String selectConfigByKey(final String configKey) {
    Preconditions.checkArgument(StrUtil.isNotBlank(configKey), "configKey is blank");

    String configValue = ConfigContext.getString(configKey);
    if (Objects.nonNull(configValue)) {
      // 数据库中有缓存的值
      return configValue;
    }

    final SysConfig config = entityQuery.queryable(SysConfig.class)
        .where(_config -> _config.configKey().eq(configKey)).singleOrNull();
    if (Objects.nonNull(config)) {
      // 缓存配置
      ConfigContext.put(configKey, config.getConfigValue());
      return config.getConfigValue();
    } else {
      // 就算数据库中没有，也要缓存空值，防止缓存穿透
      ConfigContext.put(configKey, StrUtil.EMPTY);
    }

    return StringUtils.EMPTY;
  }

  /**
   * 查询参数配置列表
   *
   * @param searchReq   查询条件
   * @param resultClass 结果类型
   * @return 参数配置集合
   */
  @Override
  public <T> List<T> selectConfigList(final SysConfig searchReq, Class<T> resultClass) {
    if (Objects.isNull(searchReq)) {
      return entityQuery.queryable(SysConfig.class).selectAutoInclude(resultClass).toList();
    }

    return entityQuery.queryable(SysConfig.class)
        .where(s -> {
          s.configName().like(StrUtil.isNotBlank(searchReq.getConfigName()), searchReq.getConfigName()); // 参数名称
          s.configType().eq(StrUtil.isNotBlank(searchReq.getConfigType()), searchReq.getConfigType()); // 参数类型
          s.configKey().like(StrUtil.isNotBlank(searchReq.getConfigKey()), searchReq.getConfigKey()); // 参数键名

          s.createTime().ge(Objects.nonNull(searchReq.getParams().getBeginTime()),
              searchReq.getParams().getBeginTime()); // 创建时间
          s.createTime().le(Objects.nonNull(searchReq.getParams().getEndTime()), searchReq.getParams().getEndTime()); // 创建时间
        })
        .selectAutoInclude(resultClass)
        .toList();
  }

  /**
   * 根据条件查询系统配置信息并分页返回结果。
   *
   * @param page      分页适配器
   * @param searchReq 系统配置信息
   * @return 分页结果
   */
  @Override
  @Tran(policy = TranPolicy.supports)
  public <T> Page<T> pageSearchConfig(final ActionContext context, final Page<T> page, final SysConfig searchReq,
      Class<T> resultClass) {
    if (Objects.isNull(searchReq)) {

      EasyPageResult<T> pageResult = entityQuery.queryable(SysConfig.class)
          .selectAutoInclude(resultClass)
          .toPageResult(page.getPageNum(), page.getPageSize());
      return Page.of(pageResult);
    }

    EasyPageResult<T> pageResult = entityQuery.queryable(SysConfig.class)
        .where(config -> {
          config.configName().like(StrUtil.isNotBlank(searchReq.getConfigName()), searchReq.getConfigName()); // 参数名称
          config.configType().eq(StrUtil.isNotBlank(searchReq.getConfigType()), searchReq.getConfigType()); // 参数类型
          config.configKey().like(StrUtil.isNotBlank(searchReq.getConfigKey()), searchReq.getConfigKey()); // 参数键名

          config.createTime().ge(Objects.nonNull(searchReq.getParams().getBeginTime()),
              searchReq.getParams().getBeginTime()); // 创建时间
          config.createTime().le(Objects.nonNull(searchReq.getParams().getEndTime()),
              searchReq.getParams().getEndTime()); // 创建时间
        })
        .selectAutoInclude(resultClass)
        .toPageResult(page.getPageNum(), page.getPageSize(), page.getTotal());
    return Page.of(pageResult);
  }


  /**
   * 新增参数配置
   *
   * @param config 参数配置信息
   * @return 结果
   */
  @Override
  public long insertConfig(final SysConfig config) {
    // 查询数据库中是否有该参数
    final SysConfig sysConfig = entityQuery.queryable(SysConfig.class)
        .where(_config -> _config.configKey().eq(config.getConfigKey())).singleOrNull();
    if (StringUtils.isNotNull(sysConfig)) {
      throw new ServiceException(StrUtil.format("参数键名{}已存在", config.getConfigKey()));
    }
    final long row = entityQuery.insertable(config).setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS).executeRows();
    if (row > 0) {
      ConfigContext.put(config.getConfigKey(), config.getConfigValue());
    }
    return row;
  }

  /**
   * 修改参数配置
   *
   * @param config 参数配置信息
   * @return 结果
   */
  @Override
  public long updateConfig(final SysConfig config) {
    // final SysConfig temp = getById(config.getConfigId());
    final SysConfig temp = entityQuery.queryable(SysConfig.class)
        .whereById(config.getConfigId()).singleOrNull();
    if (StringUtils.isNotNull(temp)) {
      ConfigContext.remove(temp.getConfigKey());

    }
    // final int row = getBaseMapper().updateById(config);
    final long row = entityQuery.updatable(config).setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS).executeRows();
    if (row > 0) {
      ConfigContext.put(config.getConfigKey(), config.getConfigValue());
    }
    return row;
  }

  /**
   * 批量删除参数信息
   *
   * @param configIds 需要删除的参数ID
   */
  @Override
  public void deleteConfigByIds(final List<Long> configIds) {

    entityQuery.queryable(SysConfig.class)
        .whereByIds(configIds)
        .selectColumn(SysConfigProxy::configKey)
        .toList()
        .forEach(ConfigContext::remove);

    entityQuery.deletable(SysConfig.class)
        .whereByIds(configIds)
        .allowDeleteStatement(true)
        .executeRows();
  }

  /**
   * 重置参数缓存数据
   */
  @Override
  public void resetConfigCache() {
    ConfigContext.reloadAll();

  }

  /**
   * 校验参数键名是否唯一
   *
   * @param config 参数信息
   * @return 结果
   */
  @Override
  public boolean checkConfigKeyUnique(final SysConfig config) {
    final SysConfig info = entityQuery.queryable(SysConfig.class)
        .where(_config -> _config.configKey().eq(config.getConfigKey())).singleOrNull();
    if (Objects.nonNull(info) && !Objects.equals(info.getConfigId(), config.getConfigId())) {
      return UserConstants.NOT_UNIQUE;
    }
    return UserConstants.UNIQUE;
  }

  @Override
  public List<SysConfig> listAll() {
    return entityQuery.queryable(SysConfig.class).toList();
  }

}
