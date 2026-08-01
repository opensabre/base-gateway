package io.github.opensabre.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** 由网关控制面发布并随 Nacos 刷新的 API Method + Path 鉴权规则。 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "opensabre.gateway.api-access")
public class GatewayApiAccessProperties {
    private List<Rule> rules = new ArrayList<>();

    /** 单条 API 访问规则；应用级通配路由不写入这里。 */
    @Data
    public static class Rule {
        private String routeId;
        private String method;
        private String path;
        private AccessMode mode;
    }

    /** API 对外入口的认证授权强度。 */
    public enum AccessMode {
        PUBLIC,
        AUTHENTICATED,
        RESOURCE_REQUIRED
    }
}
