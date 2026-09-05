# Issue: Nacos 热刷新导致 Gateway 路由运行态失效

- 状态：待修复
- 优先级：高
- 发现日期：2026-08-23
- 影响范围：`base-gateway` 动态路由、OAuth client 配置发布后的热刷新

## 现象

通过网关控制面发布 `base-gateway.yml` 后，Nacos 配置查询结果包含 IQC 路由，但网关运行态没有加载该路由，访问 `/api/iqc/**` 时返回 404，并提示：

```text
No static resource api/iqc/... for request
```

## 根因

Nacos 配置刷新触发 Spring Cloud 的 `ConfigurationPropertiesRebinder`。刷新过程中尝试重新实例化没有无参构造器的 Bean，失败并中断整次配置刷新：

- `RequestRateLimiterGatewayFilterFactory`
- `DefaultRedisRateLimiter`

典型异常：

```text
NoSuchMethodException: RequestRateLimiterGatewayFilterFactory.<init>()
No default constructor found: DefaultRedisRateLimiter
```

因此控制面中的配置版本与网关运行态不一致。当前只能通过重启网关，使完整配置冷启动后恢复路由。

## 修复目标

1. Nacos 路由/OAuth 配置热刷新不能被非配置 Bean 的重绑定失败阻断。
2. 自定义 Gateway Filter Factory 和 `DefaultRedisRateLimiter` 不应被错误地按 `@ConfigurationProperties` 重新实例化。
3. 热刷新失败时应暴露明确的运行态版本/状态，避免控制面显示已发布但请求实际走不到路由。
4. 增加包含 `RequestRateLimiter`、IQC 应用路由和 OAuth client 的配置刷新回归测试。

## 临时措施

重启 `base-gateway` 使当前完整 Nacos 配置冷启动；重启后需验证 `/api/iqc/**` 返回后端响应（未认证时应为 401/403，而不是 404）。

2026-08-23 已验证：控制面配置查询显示 IQC 路由存在，但网关运行态因热刷新失败返回 404；使用标准 `base-gateway.yml` 恢复 Nacos 配置并重启后，IQC 接口恢复为未认证 401，路由已生效。问题仍待修复热刷新机制。
