package io.github.opensabre.gateway.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Getter
@Component
@PropertySource(value = {"classpath:base-gateway.yml"}, factory = YamlPropertyLoaderFactory.class)
public class OpensabreGatewayConfig {

    @Value("${opensabre.gateway.permit.paths}")
    private String[] permitPaths;
}

