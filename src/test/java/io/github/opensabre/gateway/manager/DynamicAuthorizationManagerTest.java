package io.github.opensabre.gateway.manager;

import io.github.opensabre.gateway.config.GatewayApiAccessProperties;
import io.github.opensabre.gateway.entity.Authority;
import io.github.opensabre.gateway.service.IAuthorityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** API 显式鉴权模式优先于旧的全局权限开关。 */
class DynamicAuthorizationManagerTest {

    private DynamicAuthorizationManager manager;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        GatewayApiAccessProperties properties = new GatewayApiAccessProperties();
        properties.setRules(List.of(
                rule("GET", "/public", GatewayApiAccessProperties.AccessMode.PUBLIC),
                rule("GET", "/profile", GatewayApiAccessProperties.AccessMode.AUTHENTICATED),
                rule("GET", "/orders/{id}", GatewayApiAccessProperties.AccessMode.RESOURCE_REQUIRED)));
        manager = new DynamicAuthorizationManager(new GatewayApiAccessPolicy(properties));
        ReflectionTestUtils.setField(manager, "permission", false);
        ReflectionTestUtils.setField(manager, "authorityService", authorityService());
        authentication = new UsernamePasswordAuthenticationToken(
                "user", "token", List.of(new SimpleGrantedAuthority("order-reader")));
    }

    @Test
    void shouldAllowPublicWithoutAuthentication() {
        assertThat(check(Mono.empty(), "GET", "/public")).isTrue();
    }

    @Test
    void shouldRequireAuthenticationForAuthenticatedMode() {
        assertThat(check(Mono.empty(), "GET", "/profile")).isFalse();
        assertThat(check(Mono.just(authentication), "GET", "/profile")).isTrue();
    }

    @Test
    void shouldForceResourcePermissionEvenWhenGlobalPermissionIsDisabled() {
        assertThat(check(Mono.just(authentication), "GET", "/orders/42")).isTrue();
        assertThat(check(Mono.empty(), "GET", "/orders/42")).isFalse();
        assertThat(check(Mono.just(authentication), "POST", "/orders/42")).isTrue();
    }

    private boolean check(Mono<Authentication> auth, String method, String path) {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(org.springframework.http.HttpMethod.valueOf(method), path));
        AuthorizationDecision decision = manager.check(auth, new AuthorizationContext(exchange)).block();
        return decision != null && decision.isGranted();
    }

    private GatewayApiAccessProperties.Rule rule(String method, String path,
            GatewayApiAccessProperties.AccessMode mode) {
        GatewayApiAccessProperties.Rule rule = new GatewayApiAccessProperties.Rule();
        rule.setMethod(method);
        rule.setPath(path);
        rule.setMode(mode);
        return rule;
    }

    private IAuthorityService authorityService() {
        return new IAuthorityService() {
            @Override
            public Flux<Authority> getAuthorityForRoles(String... roles) {
                return Flux.just(new Authority("/orders/{id}", "GET", "order:view", "order-reader"));
            }

            @Override
            public Flux<Authority> getAuthorityForRoles(Set<String> roles) {
                return getAuthorityForRoles(roles.toArray(String[]::new));
            }
        };
    }
}
