package io.github.opensabre.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class RefreshingGlobalCorsConfigurationTest {

    @Test
    void shouldRebindAndApplyCorsAfterEnvironmentChange() {
        String prefix = RefreshingGlobalCorsConfiguration.GLOBAL_CORS_PREFIX;
        MockEnvironment environment = new MockEnvironment()
                .withProperty(prefix + ".cors-configurations.[/**].allowed-origins[0]", "http://opensabre:3010")
                .withProperty(prefix + ".cors-configurations.[/**].allowed-methods[0]", "GET")
                .withProperty(prefix + ".cors-configurations.[/**].allowed-methods[1]", "POST");
        GlobalCorsProperties properties = new GlobalCorsProperties();
        RoutePredicateHandlerMapping handlerMapping = mock(RoutePredicateHandlerMapping.class);
        RefreshingGlobalCorsConfiguration refresher =
                new RefreshingGlobalCorsConfiguration(environment, properties, handlerMapping);

        refresher.onApplicationEvent(new EnvironmentChangeEvent(Set.of(prefix + ".cors-configurations")));

        assertThat(properties.getCorsConfigurations()).containsKey("/**");
        CorsConfiguration cors = properties.getCorsConfigurations().get("/**");
        assertThat(cors.getAllowedOrigins()).containsExactly("http://opensabre:3010");
        assertThat(cors.getAllowedMethods()).containsExactly("GET", "POST");
        verify(handlerMapping).setCorsConfigurations(properties.getCorsConfigurations());
    }

    @Test
    void shouldIgnoreUnrelatedEnvironmentChanges() {
        RoutePredicateHandlerMapping handlerMapping = mock(RoutePredicateHandlerMapping.class);
        RefreshingGlobalCorsConfiguration refresher = new RefreshingGlobalCorsConfiguration(
                new MockEnvironment(), new GlobalCorsProperties(), handlerMapping);

        refresher.onApplicationEvent(new EnvironmentChangeEvent(Set.of("opensabre.gateway.revision")));

        verifyNoInteractions(handlerMapping);
    }
}
