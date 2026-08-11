package io.github.opensabre.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerResilience4JFilterFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @MockBean
    private ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    public void contextLoads() {
        assertThat(applicationContext.getBeansOfType(SpringCloudCircuitBreakerResilience4JFilterFactory.class))
                .isNotEmpty();
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("base-gateway");
        assertThat(environment.getProperty("server.port")).isEqualTo("8443");
    }
}
