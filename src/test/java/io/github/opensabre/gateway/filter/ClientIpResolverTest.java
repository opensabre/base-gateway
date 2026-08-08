package io.github.opensabre.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.net.InetSocketAddress;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {
    @Test void acceptsForwardedAddressFromTrustedProxy() {
        ClientIpProperties properties = new ClientIpProperties();
        properties.setTrustedProxies(List.of("172.16.0.0/12"));
        var exchange = exchange("172.18.0.2", "203.0.113.8, 172.18.0.2");
        assertThat(new ClientIpResolver(properties).resolve(exchange)).isEqualTo("203.0.113.8");
    }

    @Test void selectsRightmostUntrustedAddressInsteadOfSpoofedPrefix() {
        ClientIpProperties properties = new ClientIpProperties();
        properties.setTrustedProxies(List.of("172.16.0.0/12"));
        var exchange = exchange("172.18.0.2", "198.51.100.99, 203.0.113.8, 172.18.0.3");
        assertThat(new ClientIpResolver(properties).resolve(exchange)).isEqualTo("203.0.113.8");
    }

    @Test void ignoresSpoofedForwardedAddressFromUntrustedPeer() {
        ClientIpProperties properties = new ClientIpProperties();
        properties.setTrustedProxies(List.of("172.16.0.0/12"));
        var exchange = exchange("198.51.100.4", "203.0.113.8");
        assertThat(new ClientIpResolver(properties).resolve(exchange)).isEqualTo("198.51.100.4");
    }

    @Test void ignoresMalformedForwardedAddress() {
        ClientIpProperties properties = new ClientIpProperties();
        properties.setTrustedProxies(List.of("172.16.0.0/12"));
        var exchange = exchange("172.18.0.2", "not-an-ip");
        assertThat(new ClientIpResolver(properties).resolve(exchange)).isEqualTo("172.18.0.2");
    }

    private MockServerWebExchange exchange(String remoteIp, String forwardedFor) {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/")
                .remoteAddress(new InetSocketAddress(remoteIp, 1234))
                .header("X-Forwarded-For", forwardedFor).build());
    }
}
