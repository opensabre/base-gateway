package io.github.opensabre.gateway.online;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@Slf4j
public class OnlineUserWebFilter implements WebFilter {

    private final OnlineUserRecordService onlineUserRecordService;

    public OnlineUserWebFilter(OnlineUserRecordService onlineUserRecordService) {
        this.onlineUserRecordService = onlineUserRecordService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getPrincipal()
                .ofType(Authentication.class)
                .filter(OnlineUserRecordService::shouldRecord)
                .zipWith(exchange.getSession())
                .flatMap(tuple -> onlineUserRecordService.record(exchange, tuple.getT2(), tuple.getT1())
                        .onErrorResume(ex -> {
                            log.warn("record online user failed", ex);
                            return Mono.empty();
                        }))
                .then(Mono.defer(() -> chain.filter(exchange)));
    }
}
