package io.github.opensabre.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证网关提供 Knife4j 页面及下游服务文档聚合配置。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.gateway.server.webflux.discovery.locator.enabled=false",
        "spring.security.oauth2.client.provider.custom-issuer.issuer-uri=",
        "spring.security.oauth2.client.provider.custom-issuer.authorization-uri=http://localhost/oauth2/authorize",
        "spring.security.oauth2.client.provider.custom-issuer.token-uri=http://localhost/oauth2/token",
        "spring.security.oauth2.client.provider.custom-issuer.jwk-set-uri=http://localhost/oauth2/jwks",
        "springdoc.swagger-ui.urls[0].name=授权服务",
        "springdoc.swagger-ui.urls[0].url=/api/auth/v3/api-docs",
        "springdoc.swagger-ui.urls[1].name=组织服务",
        "springdoc.swagger-ui.urls[1].url=/api/org/v3/api-docs",
        "springdoc.swagger-ui.urls[2].name=系统管理服务",
        "springdoc.swagger-ui.urls[2].url=/api/sysadmin/v3/api-docs"
})
class DocumentationAggregationTest {

    @Value("${local.server.port}")
    private int port;

    @MockitoBean
    private ReactiveClientRegistrationRepository clientRegistrationRepository;

    /**
     * Knife4j 页面和 Springdoc 聚合清单必须同时可用。
     */
    @Test
    void shouldExposeKnife4jWithAggregatedDocuments() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> page = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/doc.html")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> config = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs/swagger-config"))
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofString());

            assertThat(page.statusCode()).isEqualTo(200);
            assertThat(page.body()).contains("webjars/js/app");
            assertThat(config.statusCode()).isEqualTo(200);
            assertThat(config.body())
                    .contains("/api/auth/v3/api-docs")
                    .contains("/api/org/v3/api-docs")
                    .contains("/api/sysadmin/v3/api-docs");
        }
    }
}
