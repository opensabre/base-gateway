package io.github.opensabre.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.gateway.discovery.locator.enabled=false",
        "spring.security.oauth2.client.provider.custom-issuer.issuer-uri=",
        "spring.security.oauth2.client.provider.custom-issuer.authorization-uri=http://localhost/oauth2/authorize",
        "spring.security.oauth2.client.provider.custom-issuer.token-uri=http://localhost/oauth2/token",
        "spring.security.oauth2.client.provider.custom-issuer.jwk-set-uri=http://localhost/oauth2/jwks"
})
public class GatewayApplicationTests {

    @Test
    public void contextLoads() {

    }
}
