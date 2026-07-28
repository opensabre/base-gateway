package io.github.opensabre.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求url权限校验
 */
@Slf4j
@Component
public class AccessGatewayFilter implements GlobalFilter {

    /**
     * OAuth2 resource server and the dynamic authorization manager have already authenticated and
     * authorized the request before it is forwarded. The gateway deliberately does not issue an
     * internal token: the first Servlet application verifies the external JWT and issues the first
     * signed {@code x-client-token} when it calls another service.
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authentication = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String method = request.getMethod().name();
        String url = request.getPath().value();
        log.debug("url:{},method:{},hasAuthorization:{}", url, method, authentication != null);
        return chain.filter(exchange);
    }
}
