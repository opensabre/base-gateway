package com.springboot.cloud.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * 自定义限流标志的key，多个维度可以从这里入手
 * exchange对象中获取服务ID、请求信息，用户信息等
 */
@Component
public class RequestRateLimiterConfig {

    /**
     * ip地址限流
     *
     * @return 限流key
     */
    @Bean
    @Primary
    public KeyResolver remoteAddressKeyResolver() {
        return exchange -> Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getHostName());
    }

    /**
     * 请求路径限流
     *
     * @return 限流key
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }

    /**
     * username限流
     *
     * @return 限流key
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(Objects.requireNonNull(exchange.getRequest().getQueryParams().getFirst("username")));
    }

    /**
     * sentinel限流
     *
     * @return GlobalFilter
     */
    @Bean
    @Order(-1)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }
}
