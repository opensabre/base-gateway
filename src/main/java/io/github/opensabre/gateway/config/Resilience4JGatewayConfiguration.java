package io.github.opensabre.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 保证 Sentinel 与 Resilience4j 共存时仍注册 Gateway 使用的 reactive Resilience4j 工厂。
 * Sentinel 会先提供通用 ReactiveCircuitBreakerFactory，导致 Spring Cloud 的默认
 * Resilience4j 自动配置退让，因此需要按具体类型补齐工厂。
 */
@Configuration(proxyBeanMethods = false)
public class Resilience4JGatewayConfiguration {

    /** 仅在 Resilience4j 专用工厂缺失时创建，不覆盖框架后续提供的原生实现。 */
    @Bean
    @ConditionalOnMissingBean(ReactiveResilience4JCircuitBreakerFactory.class)
    public ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory(
            CircuitBreakerRegistry circuitBreakerRegistry,
            TimeLimiterRegistry timeLimiterRegistry,
            Resilience4JConfigurationProperties properties) {
        return new ReactiveResilience4JCircuitBreakerFactory(
                circuitBreakerRegistry, timeLimiterRegistry, properties);
    }
}
