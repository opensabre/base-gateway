package io.github.opensabre.gateway.config;

import io.github.opensabre.gateway.provider.AuthorityMetaProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class HttpExchangeConfig {

    @Bean
    @LoadBalanced // 关键注解，让WebClient具备服务发现能力
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder()
                .filter(logRequestAndResponse());
    }

    @Bean
    public AuthorityMetaProvider authorityMetaProvider(WebClient.Builder webClientBuilder) {
        // 使用WebClientAdapter和HttpServiceProxyFactory创建接口代理
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClientBuilder.build()))
                .build();
        return factory.createClient(AuthorityMetaProvider.class);
    }

    // 创建日志过滤器
    public static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction
                .ofRequestProcessor(request -> {
                    log.info(">>> 请求: {} {}", request.method(), request.url());
                    return Mono.just(request);
                });
    }

    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction
                .ofResponseProcessor(response -> {
                    log.info("<<< 响应状态: {}", response.statusCode());
                    return Mono.just(response);
                });
    }

    // 完整的请求响应日志过滤器
    public static ExchangeFilterFunction logRequestAndResponse() {
        return (request, next) -> {
            long startTime = System.currentTimeMillis();
            // 记录请求信息
            log.info(">>> [{}] {} {}", request.method(), request.url(), getRequestInfo(request));
            return next.exchange(request)
                    .doOnSuccess(response -> {
                        long duration = System.currentTimeMillis() - startTime;
                        log.info("<<< [{}] {} - 状态: {} - 耗时: {}ms", request.method(), request.url(), response.statusCode(), duration);
                    })
                    .doOnError(error -> {
                        long duration = System.currentTimeMillis() - startTime;
                        log.error("<<< [{}] {} - 错误: {} - 耗时: {}ms", request.method(), request.url(), error.getMessage(), duration);
                    });
        };
    }

    private static String getRequestInfo(ClientRequest request) {
        StringBuilder info = new StringBuilder();
        if (!request.headers().isEmpty()) {
            info.append("- 头信息: ").append(request.headers());
        }
        return info.toString();
    }
}