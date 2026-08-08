package io.github.opensabre.gateway.filter;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetSocketAddress;
import java.util.List;

/** Resolves a forwarded client IP only when the direct peer is explicitly trusted. */
@Component
public class ClientIpResolver {
    private final List<IpCidrMatcher> trustedProxies;

    public ClientIpResolver(ClientIpProperties properties) {
        this.trustedProxies = properties.getTrustedProxies().stream().map(IpCidrMatcher::parse).toList();
    }

    public String resolve(ServerWebExchange exchange) {
        String remoteIp = remoteIp(exchange.getRequest().getRemoteAddress());
        if (remoteIp == null || trustedProxies.stream().noneMatch(matcher -> matcher.matches(remoteIp))) {
            return remoteIp;
        }
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (!StringUtils.hasText(forwardedFor)) return remoteIp;
        String[] chain = forwardedFor.split(",");
        for (int index = chain.length - 1; index >= 0; index--) {
            String candidate = chain[index].trim();
            try {
                if (!IpCidrMatcher.parse(candidate).matches(candidate)) return remoteIp;
            } catch (IllegalArgumentException ignored) {
                return remoteIp;
            }
            if (trustedProxies.stream().noneMatch(matcher -> matcher.matches(candidate))) {
                return candidate;
            }
        }
        return chain[0].trim();
    }

    private String remoteIp(InetSocketAddress remoteAddress) {
        if (remoteAddress == null) return null;
        return remoteAddress.getAddress() == null
                ? remoteAddress.getHostString() : remoteAddress.getAddress().getHostAddress();
    }
}
