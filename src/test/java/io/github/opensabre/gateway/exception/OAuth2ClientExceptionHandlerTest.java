package io.github.opensabre.gateway.exception;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.common.core.exception.SystemErrorType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2ClientExceptionHandlerTest {

    @Test
    void shouldInvalidateSessionAndReturnUnauthorizedWhenRefreshFails() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest.get("/api/test").build()
        );
        var session = exchange.getSession().block();
        assertThat(session).isNotNull();

        OAuth2ClientExceptionHandler handler = new OAuth2ClientExceptionHandler();
        Result<?> result = handler.clientAuthorizationException(
                new ClientAuthorizationException(new OAuth2Error("invalid_grant"), "base-gateway-local"),
                exchange
        ).block();

        assertThat(session.isExpired()).isTrue();
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(SystemErrorType.INVALID_TOKEN.getCode());
    }

    @Test
    void shouldExposeRefreshFailureAsHttp401() {
        WebTestClient.bindToController(new FailingController())
                .controllerAdvice(new OAuth2ClientExceptionHandler())
                .build()
                .get()
                .uri("/failure")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
                .expectBody()
                .jsonPath("$.code").isEqualTo(SystemErrorType.INVALID_TOKEN.getCode());
    }

    @RestController
    static class FailingController {

        @GetMapping("/failure")
        Result<?> failure() {
            throw new ClientAuthorizationException(new OAuth2Error("invalid_grant"), "base-gateway-local");
        }
    }
}
