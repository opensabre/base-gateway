package io.github.opensabre.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class OpenSabreIpAccessControlGatewayFilterFactoryTest {
    private final OpenSabreIpAccessControlGatewayFilterFactory factory = factory();

    @Test void denylistRejectsMatchingClient() {
        var exchange = exchange("10.0.0.8");
        AtomicBoolean called = new AtomicBoolean();
        factory.apply(config("DENYLIST", "10.0.0.0/8")).filter(exchange, chain(called)).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(called).isFalse();
    }

    @Test void allowlistAllowsMatchingClient() {
        var exchange = exchange("10.0.0.8");
        AtomicBoolean called = new AtomicBoolean();
        factory.apply(config("ALLOWLIST", "10.0.0.0/8")).filter(exchange, chain(called)).block();
        assertThat(called).isTrue();
    }

    private OpenSabreIpAccessControlGatewayFilterFactory factory() {
        ClientIpProperties properties = new ClientIpProperties();
        return new OpenSabreIpAccessControlGatewayFilterFactory(new ClientIpResolver(properties));
    }

    private OpenSabreIpAccessControlGatewayFilterFactory.Config config(String mode, String cidrs) {
        var config = new OpenSabreIpAccessControlGatewayFilterFactory.Config();
        config.setMode(mode); config.setCidrs(cidrs); return config;
    }

    private MockServerWebExchange exchange(String ip) {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/api/test")
                .remoteAddress(new InetSocketAddress(ip, 1234)).build());
    }

    private GatewayFilterChain chain(AtomicBoolean called) {
        return exchange -> { called.set(true); return Mono.empty(); };
    }
}
