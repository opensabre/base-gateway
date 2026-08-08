package io.github.opensabre.gateway.exception;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.common.core.exception.SystemErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Converts an expired OAuth2 client session into a new-login signal for the admin UI. */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class OAuth2ClientExceptionHandler {

    @ExceptionHandler(ClientAuthorizationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<Result<?>> clientAuthorizationException(ClientAuthorizationException ex,
                                                         ServerWebExchange exchange) {
        log.warn("OAuth2 client authorization failed for registration {}; invalidating session",
                ex.getClientRegistrationId());
        Result<?> response = Result.fail(SystemErrorType.INVALID_TOKEN, "登录已过期，请重新登录");
        return exchange.getSession()
                .flatMap(session -> session.invalidate()
                        .thenReturn(response));
    }
}
