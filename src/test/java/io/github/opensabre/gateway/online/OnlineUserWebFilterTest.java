package io.github.opensabre.gateway.online;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OnlineUserWebFilterTest {

    private final OnlineUserRecordService recordService = mock(OnlineUserRecordService.class);
    private final OnlineUserWebFilter filter = new OnlineUserWebFilter(recordService);
    private final ServerWebExchange exchange = mock(ServerWebExchange.class);
    private final WebFilterChain chain = mock(WebFilterChain.class);

    @Test
    void invokesFilterChainOnlyOnceForAuthenticatedUser() {
        WebSession session = mock(WebSession.class);
        AtomicInteger subscriptions = new AtomicInteger();
        var authentication = UsernamePasswordAuthenticationToken.authenticated("admin", "", java.util.List.of());
        when(exchange.getPrincipal()).thenReturn(Mono.just(authentication));
        when(exchange.getSession()).thenReturn(Mono.just(session));
        when(recordService.record(exchange, session, authentication)).thenReturn(Mono.empty());
        when(chain.filter(exchange)).thenReturn(Mono.<Void>empty()
                .doOnSubscribe(ignored -> subscriptions.incrementAndGet()));

        filter.filter(exchange, chain).block();

        verify(recordService).record(exchange, session, authentication);
        verify(chain).filter(exchange);
        assertThat(subscriptions).hasValue(1);
    }

    @Test
    void invokesFilterChainOnlyOnceWithoutPrincipal() {
        when(exchange.getPrincipal()).thenReturn(Mono.empty());
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(recordService, never()).record(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(chain).filter(exchange);
    }
}
