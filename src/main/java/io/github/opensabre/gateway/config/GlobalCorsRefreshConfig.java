package io.github.opensabre.gateway.config;

import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** 配置 Gateway 全局 CORS 的运行时刷新监听器。 */
@Configuration(proxyBeanMethods = false)
public class GlobalCorsRefreshConfig {

    @Bean
    public RefreshingGlobalCorsConfiguration refreshingGlobalCorsConfiguration(Environment environment,
            GlobalCorsProperties globalCorsProperties, RoutePredicateHandlerMapping handlerMapping) {
        return new RefreshingGlobalCorsConfiguration(environment, globalCorsProperties, handlerMapping);
    }
}
