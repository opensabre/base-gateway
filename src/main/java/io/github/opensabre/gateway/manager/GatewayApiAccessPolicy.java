package io.github.opensabre.gateway.manager;

import io.github.opensabre.gateway.config.GatewayApiAccessProperties;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Optional;

/** 在现有角色资源鉴权前解析 API 级显式访问模式。 */
@Component
public class GatewayApiAccessPolicy {

    private final GatewayApiAccessProperties properties;
    private final PathPatternParser pathPatternParser = new PathPatternParser();

    public GatewayApiAccessPolicy(GatewayApiAccessProperties properties) {
        this.properties = properties;
    }

    /** 按 Method + PathPattern 返回首个 API 级规则；未命中时交回原权限逻辑。 */
    public Optional<GatewayApiAccessProperties.AccessMode> resolve(ServerWebExchange exchange) {
        String method = exchange.getRequest().getMethod().name();
        PathContainer path = exchange.getRequest().getPath().pathWithinApplication();
        for (GatewayApiAccessProperties.Rule rule : properties.getRules()) {
            if (rule.getMode() == null || rule.getMethod() == null || rule.getPath() == null) continue;
            if (method.equalsIgnoreCase(rule.getMethod())
                    && pathPatternParser.parse(rule.getPath()).matches(path)) {
                return Optional.of(rule.getMode());
            }
        }
        return Optional.empty();
    }
}
