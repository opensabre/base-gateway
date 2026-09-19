package io.github.opensabre.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceServerConfigActuatorTest {

    @Test
    void clientCredentialsScopeDoesNotNeedAnApplicationRole() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("opensabre-prometheus")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("scope", "actuator.read")
                .build();

        var authentication = Mono.from(new ResourceServerConfig()
                .grantedAuthoritiesExtractor().convert(jwt)).block();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .contains("SCOPE_actuator.read")
                .doesNotContain("ADMIN");
    }
}
