package io.github.opensabre.gateway.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OpensabreGatewayConfig {

    /**
     * 网关白名单只从已合并的 Spring Environment 读取。
     *
     * <p>base-gateway.yml 由 Nacos 动态配置提供；不能再通过本地 @PropertySource
     * 加载同名文件，否则本地 OAuth2 默认值会覆盖管理台发布的客户端配置。</p>
     */
    @Value("${opensabre.gateway.permit.paths:/v3/**,/webjars/**,/assets/**,/doc.html,/favicon.ico,/oauth2/**}")
    private String[] permitPaths;
}
