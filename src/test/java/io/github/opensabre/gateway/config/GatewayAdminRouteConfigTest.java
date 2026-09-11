package io.github.opensabre.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

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
        String iqcDocs = "spring.cloud.gateway.server.webflux.routes[1]";
        assertThat(properties.getProperty(iqcDocs + ".id"))
                .isEqualTo("iqc-platform-docs");
        assertThat(properties.getProperty(iqcDocs + ".predicates[0]"))
                .isEqualTo("Path=/api/iqc/v3/api-docs/**");
        assertThat(properties.getProperty(iqcDocs + ".filters[0]"))
                .isEqualTo("StripPrefix=2");
        String authorization = "spring.cloud.gateway.server.webflux.routes[3]";
        assertThat(properties.getProperty(authorization + ".id"))
                .isEqualTo("base-authorization");
        assertThat(properties.getProperty(authorization + ".predicates[0]"))
                .isEqualTo("Path=/oauth2/**,/login,/logout,/assets/**");
        String authorizationApi = "spring.cloud.gateway.server.webflux.routes[4]";
        assertThat(properties.getProperty(authorizationApi + ".id"))
                .isEqualTo("base-authorization-api");
        assertThat(properties.getProperty(authorizationApi + ".predicates[0]"))
                .isEqualTo("Path=/api/auth/**");
        assertThat(properties.getProperty(authorizationApi + ".filters[0]"))
                .isEqualTo("StripPrefix=2");
        assertThat(properties.stringPropertyNames())
                .noneMatch(name -> name.startsWith("spring.cloud.gateway.routes["));
    }

    /**
     * 聚合文档通过已有应用路由读取各服务的 OpenAPI 描述。
     */
    @Test
    void shouldNotHardCodeAggregatedOpenApiDocuments() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new FileSystemResource("src/main/resources/application.yml"));
        Properties properties = factory.getObject();

        assertThat(properties).isNotNull();
        assertThat(properties.stringPropertyNames())
                .noneMatch(name -> name.startsWith("springdoc.swagger-ui.urls["));
    }
}
