package io.github.opensabre.gateway.config;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配置中心变更后重新绑定并应用 Gateway 全局 CORS 配置。
 *
 * <p>Gateway 默认只在创建 HandlerMapping 时读取一次全局 CORS；这里在环境刷新完成后
 * 原子替换 HandlerMapping 使用的快照，使 CORS 白名单无需重启即可生效。</p>
 */
public class RefreshingGlobalCorsConfiguration implements ApplicationListener<EnvironmentChangeEvent> {

    static final String GLOBAL_CORS_PREFIX = "spring.cloud.gateway.server.webflux.globalcors";

    private final Environment environment;
    private final GlobalCorsProperties globalCorsProperties;
    private final RoutePredicateHandlerMapping handlerMapping;

    public RefreshingGlobalCorsConfiguration(Environment environment, GlobalCorsProperties globalCorsProperties,
            RoutePredicateHandlerMapping handlerMapping) {
        this.environment = environment;
        this.globalCorsProperties = globalCorsProperties;
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        if (event.getKeys().stream().noneMatch(key -> key.startsWith(GLOBAL_CORS_PREFIX))) return;
        refresh();
    }

    /** 从已刷新的 Environment 重建 CORS 快照并同时更新 Gateway 的运行时 HandlerMapping。 */
    void refresh() {
        GlobalCorsProperties rebound = Binder.get(environment)
                .bind(GLOBAL_CORS_PREFIX, GlobalCorsProperties.class)
                .orElseGet(GlobalCorsProperties::new);
        Map<String, CorsConfiguration> snapshot = new LinkedHashMap<>(rebound.getCorsConfigurations());
        globalCorsProperties.getCorsConfigurations().clear();
        globalCorsProperties.getCorsConfigurations().putAll(snapshot);
        handlerMapping.setCorsConfigurations(snapshot);
    }
}
