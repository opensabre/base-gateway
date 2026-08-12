package io.github.opensabre.gateway.actuator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.mock.env.MockEnvironment;

class GatewayRuntimeEndpointTest {

    @Test
    void exposesOnlyAllowListedEffectiveSettingsThroughActuator() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.cloud.gateway.httpclient.pool.max-connections", "700")
                .withProperty("spring.cloud.gateway.httpclient.response-timeout", "PT8S")
                .withProperty("spring.security.oauth2.client.registration.secret.client-secret", "do-not-leak");
        Properties build = new Properties();
        build.setProperty("version", "1.2.3");

        var endpoint = new GatewayRuntimeEndpoint(environment, mock(RouteLocator.class),
                new BuildProperties(build), "revision-1");
        var snapshot = endpoint.createSnapshot(12);

        assertThat(GatewayRuntimeEndpoint.class.getAnnotation(Endpoint.class).id())
                .isEqualTo("gatewayruntime");
        assertThat(snapshot.revision()).isEqualTo("revision-1");
        assertThat(snapshot.applicationVersion()).isEqualTo("1.2.3");
        assertThat(snapshot.routeCount()).isEqualTo(12);
        assertThat(snapshot.httpClient().maxConnections()).isEqualTo(700);
        assertThat(snapshot.httpClient().responseTimeoutMillis()).isEqualTo(8000);
        assertThat(snapshot.sources()).doesNotContainKey(
                "spring.security.oauth2.client.registration.secret.client-secret");
    }
}
