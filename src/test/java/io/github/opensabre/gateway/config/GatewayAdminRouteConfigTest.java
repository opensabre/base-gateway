package io.github.opensabre.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证独立网关控制面的稳定外部路由。
 */
class GatewayAdminRouteConfigTest {

    /**
     * 前端统一使用 /api 前缀，网关必须移除 /api/gateway-admin 两层路径。
     */
    @Test
    void shouldRouteGatewayAdminPrefixToControlPlane() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("base-gateway.yml"));
        Properties properties = factory.getObject();

        assertThat(properties).isNotNull();
        String prefix = "spring.cloud.gateway.server.webflux.routes[0]";
        assertThat(properties.getProperty(prefix + ".id"))
                .isEqualTo("base-gateway-admin-api");
        assertThat(properties.getProperty(prefix + ".uri"))
                .isEqualTo("lb://base-gateway-admin");
        assertThat(properties.getProperty(prefix + ".predicates[0]"))
                .isEqualTo("Path=/api/gateway-admin/**");
        assertThat(properties.getProperty(prefix + ".filters[0]"))
                .isEqualTo("StripPrefix=2");
        assertThat(properties.stringPropertyNames())
                .noneMatch(name -> name.startsWith("spring.cloud.gateway.routes["));
    }
}
