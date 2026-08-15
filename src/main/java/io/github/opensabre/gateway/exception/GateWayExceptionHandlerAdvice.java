package io.github.opensabre.gateway.exception;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.common.core.exception.SystemErrorType;
import io.netty.channel.ConnectTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GateWayExceptionHandlerAdvice {

    /**
     * OAuth2 登录身份存在、但 Authorized Client 已失效时，要求客户端重新登录。
     * TokenRelay 在此场景抛出的异常不能被通用异常处理器转换成 HTTP 500。
     */
    @ExceptionHandler(ClientAuthorizationRequiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handle(ClientAuthorizationRequiredException ex) {
        log.warn("OAuth2 client authorization required: registrationId={}", ex.getClientRegistrationId());
        return Result.fail(SystemErrorType.INVALID_TOKEN);
    }

    @ExceptionHandler(value = {ResponseStatusException.class})
    public Result<?> handle(ResponseStatusException ex) {
        log.error("response status exception:{}", ex.getMessage());
        return Result.fail(SystemErrorType.GATEWAY_ERROR);
    }

    @ExceptionHandler(value = {ConnectTimeoutException.class})
    public Result<?> handle(ConnectTimeoutException ex) {
        log.error("connect timeout exception:{}", ex.getMessage());
        return Result.fail(SystemErrorType.GATEWAY_CONNECT_TIME_OUT);
    }

    @ExceptionHandler(value = {NoResourceFoundException.class, NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handle(NoResourceFoundException ex) {
        log.error("not found exception:{}", ex.getMessage());
        return Result.fail(SystemErrorType.GATEWAY_NOT_FOUND_SERVICE);
    }

//    @ExceptionHandler(value = {ExpiredJwtException.class})
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public Result handle(ExpiredJwtException ex) {
//        log.error("ExpiredJwtException:{}", ex.getMessage());
//        return Result.fail(SystemErrorType.INVALID_TOKEN);
//    }
//
//    @ExceptionHandler(value = {SignatureException.class})
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public Result handle(SignatureException ex) {
//        log.error("SignatureException:{}", ex.getMessage());
//        return Result.fail(SystemErrorType.INVALID_TOKEN);
//    }
//
//    @ExceptionHandler(value = {MalformedJwtException.class})
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public Result handle(MalformedJwtException ex) {
//        log.error("MalformedJwtException:{}", ex.getMessage());
//        return Result.fail(SystemErrorType.INVALID_TOKEN);
//    }

    @ExceptionHandler(value = {RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handle(RuntimeException ex) {
        log.error("runtime exception:{}", ex.getMessage());
        return Result.fail();
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handle(Exception ex) {
        log.error("exception:{}", ex.getMessage());
        return Result.fail();
    }

    @ExceptionHandler(value = {Throwable.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handle(Throwable throwable) {
        Result<?> result = Result.fail();
        if (throwable instanceof NoResourceFoundException) {
            result = handle((NoResourceFoundException) throwable);
        } else if (throwable instanceof ConnectTimeoutException) {
            result = handle((ConnectTimeoutException) throwable);
        } else if (throwable instanceof NotFoundException) {
            result = handle((NotFoundException) throwable);
        } else if (throwable instanceof RuntimeException) {
            result = handle((RuntimeException) throwable);
        } else if (throwable instanceof Exception) {
            result = handle((Exception) throwable);
        }
        return result;
    }
}
