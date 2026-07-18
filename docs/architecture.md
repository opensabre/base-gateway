# 架构与边界

`gateway-web` 是 OpenSabre 的运行时边缘网关，处理请求转发、权限校验、在线用户记录及网关级过滤器。路由和应用配置入口包括 `base-gateway.yml`、`application.yml` 与 `bootstrap.yml`；实际环境中的配置中心内容优先于文档描述。

| 区域 | 位置 | 职责 |
| --- | --- | --- |
| 过滤器 | `filter/` | 网关请求处理 |
| 鉴权 | `service/Authority*` | 资源权限元数据与授权判定 |
| 在线用户 | `online/` | 在线记录 |
| 配置 | `config/`、`resources/*.yml` | 网关与应用配置 |
