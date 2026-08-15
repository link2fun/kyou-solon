# Docker 开发环境

MySQL 8.0 + Redis 7 一键启动，自动初始化 RuoYi 数据库。

## 前置条件

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

## 快速启动

```bash
cd env/docker

# 首次使用：复制环境变量（按需修改）
cp .env.example .env

# 启动
docker-compose up -d
```

## 连接信息

| 服务 | Host | Port | 数据库/用户 | 密码 |
|------|------|------|------------|------|
| MySQL | 127.0.0.1 | 3606 | kyou_solon / kyou | kyou_pass |
| MySQL root | 127.0.0.1 | 3606 | - / root | root |
| Redis | 127.0.0.1 | 6679 | - | 无 |

## 常用命令

```bash
# 启动
docker-compose up -d

# 停止
docker-compose down

# 查看日志
docker-compose logs -f mysql
docker-compose logs -f redis

# 重建数据库（清除数据卷，重新执行初始化 SQL）
docker-compose down -v
docker-compose up -d
```

## 应用配置

启动 Docker 后，在项目中创建 `app-local.yml`（已被 .gitignore 忽略）连接容器。端口以 `.env.example` 为准，如你在 `.env` 中修改过端口，请相应调整：

```yaml
kyou:
  redis:
    config: |
      singleServerConfig:
        address: "redis://127.0.0.1:6679"
        database: 15

datasource:
  default:
    type: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://127.0.0.1:3606/kyou_solon?useUnicode=true&characterEncoding=utf8&autoReconnect=true&allowMultiQueries=true&rewriteBatchedStatements=true
    username: kyou
    password: kyou_pass
```

## 注意事项

- 初始化 SQL（`doc/sqls/ry_20240629.sql`）仅在 MySQL 数据卷为空时执行
- 如需重新初始化，执行 `docker-compose down -v` 删除数据卷后重新启动
- 端口冲突时修改 `.env` 中的 `MYSQL_PORT` / `REDIS_PORT`
