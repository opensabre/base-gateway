package io.github.opensabre.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/** Route filter enforcing an effective IP allowlist or denylist. */
@Component
public class OpenSabreIpAccessControlGatewayFilterFactory
        extends AbstractGatewayFilterFactory<OpenSabreIpAccessControlGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(OpenSabreIpAccessControlGatewayFilterFactory.class);
    private final ClientIpResolver clientIpResolver;

    public OpenSabreIpAccessControlGatewayFilterFactory(ClientIpResolver clientIpResolver) {
        super(Config.class);
        this.clientIpResolver = clientIpResolver;
    }

    @Override
    public GatewayFilter apply(Config config) {
        AccessMode mode = AccessMode.valueOf(config.getMode().trim().toUpperCase());
        List<IpCidrMatcher> matchers = Arrays.stream(config.getCidrs().split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).map(IpCidrMatcher::parse).toList();
        if (matchers.isEmpty()) throw new IllegalArgumentException("IP 黑白名单至少需要一条 CIDR");
        return (exchange, chain) -> {
            String clientIp = clientIpResolver.resolve(exchange);
            boolean matched = matchers.stream().anyMatch(matcher -> matcher.matches(clientIp));
            boolean allowed = mode == AccessMode.ALLOWLIST ? matched : !matched;
            if (allowed) return chain.filter(exchange);
            log.warn("Gateway IP access denied: mode={}, clientIp={}, method={}, path={}",
                    mode, clientIp, exchange.getRequest().getMethod(), exchange.getRequest().getPath());
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        };
    }

    public enum AccessMode { ALLOWLIST, DENYLIST }

    public static class Config {
        private String mode;
        private String cidrs;
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getCidrs() { return cidrs; }
        public void setCidrs(String cidrs) { this.cidrs = cidrs; }
    }
}
