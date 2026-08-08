package io.github.opensabre.gateway.online;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

class OnlineUserRecordServiceTest {

    @Test
    void shouldSkipAnonymousUser() {
        var anonymous = new TestingAuthenticationToken("anonymousUser", "n/a");
        anonymous.setAuthenticated(true);

        assertThat(OnlineUserRecordService.shouldRecord(anonymous)).isFalse();
    }

    @Test
    void shouldRecordAuthenticatedUser() {
        var authentication = new TestingAuthenticationToken("admin", "n/a");
        authentication.setAuthenticated(true);

        assertThat(OnlineUserRecordService.shouldRecord(authentication)).isTrue();
    }

}
