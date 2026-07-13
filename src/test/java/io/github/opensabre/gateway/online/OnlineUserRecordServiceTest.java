package io.github.opensabre.gateway.online;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
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

    @Test
    void shouldUseFirstForwardedIp() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/")
                .header("X-Forwarded-For", "10.0.0.1, 10.0.0.2"));

        assertThat(OnlineUserRecordService.clientIp(exchange)).isEqualTo("10.0.0.1");
    }
}
