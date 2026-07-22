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
     * 外部前缀必须转发到服务发现中的 base-gateway-admin，并移除一层前缀。
     */
    @Test
    void shouldRouteGatewayAdminPrefixToControlPlane() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("base-gateway.yml"));
        Properties properties = factory.getObject();

        assertThat(properties).isNotNull();
        assertThat(properties.getProperty("spring.cloud.gateway.routes[0].id"))
                .isEqualTo("base-gateway-admin-api");
        assertThat(properties.getProperty("spring.cloud.gateway.routes[0].uri"))
                .isEqualTo("lb://base-gateway-admin");
        assertThat(properties.getProperty("spring.cloud.gateway.routes[0].predicates[0]"))
                .isEqualTo("Path=/gateway-admin/**");
        assertThat(properties.getProperty("spring.cloud.gateway.routes[0].filters[0]"))
                .isEqualTo("StripPrefix=1");
    }
}
