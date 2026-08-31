package io.github.opensabre.gateway.exception;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.common.core.exception.SystemErrorType;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GateWayExceptionHandlerAdviceTest {

    @Test
    void shouldReturnUnauthorizedWhenTokenRelayRequiresLoginAgain() throws Exception {
        GateWayExceptionHandlerAdvice advice = new GateWayExceptionHandlerAdvice();

        Result<?> result = advice.handle(new ClientAuthorizationRequiredException("base-gateway-local"));

        assertThat(result.getCode()).isEqualTo(SystemErrorType.INVALID_TOKEN.getCode());
        assertThat(GateWayExceptionHandlerAdvice.class.getAnnotation(org.springframework.web.bind.annotation.RestControllerAdvice.class))
                .isNotNull();
        assertThat(OrderUtils.getOrder(GateWayExceptionHandlerAdvice.class)).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
        ResponseStatus status = GateWayExceptionHandlerAdvice.class
                .getMethod("handle", ClientAuthorizationRequiredException.class)
                .getAnnotation(ResponseStatus.class);
        assertThat(status.value()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {
        GateWayExceptionHandlerAdvice advice = new GateWayExceptionHandlerAdvice();
        OAuth2AuthorizationException exception = new OAuth2AuthorizationException(
                new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT));

        Result<?> result = advice.handle(exception);

        assertThat(result.getCode()).isEqualTo(SystemErrorType.INVALID_TOKEN.getCode());
        ResponseStatus status = GateWayExceptionHandlerAdvice.class
                .getMethod("handle", OAuth2AuthorizationException.class)
                .getAnnotation(ResponseStatus.class);
        assertThat(status.value()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
