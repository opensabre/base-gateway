package io.github.opensabre.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4jBulkheadProvider;
import org.springframework.cloud.gateway.config.GatewayResilience4JCircuitBreakerAutoConfiguration;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerResilience4JFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证 Sentinel 已占用通用工厂场景下仍可装配 Gateway Resilience4j 过滤器。 */
class Resilience4JGatewayConfigurationTest {

    private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
            .withUserConfiguration(TestDependencies.class,
                    Resilience4JGatewayConfiguration.class,
                    GatewayResilience4JCircuitBreakerAutoConfiguration.class);

    @Test
    void registersGatewayCircuitBreakerFilter() {
        contextRunner.run(context -> assertThat(context)
                .hasSingleBean(SpringCloudCircuitBreakerResilience4JFilterFactory.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class TestDependencies {
        @Bean
        CircuitBreakerRegistry circuitBreakerRegistry() {
            return CircuitBreakerRegistry.ofDefaults();
        }

        @Bean
        TimeLimiterRegistry timeLimiterRegistry() {
            return TimeLimiterRegistry.ofDefaults();
        }

        @Bean
        ReactiveResilience4jBulkheadProvider reactiveResilience4jBulkheadProvider() {
            return new ReactiveResilience4jBulkheadProvider(BulkheadRegistry.ofDefaults());
        }

        @Bean
        Resilience4JConfigurationProperties resilience4JConfigurationProperties() {
            return new Resilience4JConfigurationProperties();
        }
    }
}
