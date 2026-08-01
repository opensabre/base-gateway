package io.github.opensabre.gateway.manager;

import io.github.opensabre.gateway.entity.Authority;
import io.github.opensabre.gateway.service.IAuthorityService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.Optional;
import io.github.opensabre.gateway.config.GatewayApiAccessProperties;

/**
 * 请求 url 动态鉴权
 */
@Component
public class DynamicAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    // 无权限
    public static final AuthorizationDecision AUTHORIZATION_DECISION_FALSE = new AuthorizationDecision(false);
    // 有权限
    public static final AuthorizationDecision AUTHORIZATION_DECISION_TRUE = new AuthorizationDecision(true);

    @Resource
    private IAuthorityService authorityService;

    @Value("${opensabre.gateway.permission.enabled:false}")
    private boolean permission;

    private final GatewayApiAccessPolicy apiAccessPolicy;

    public DynamicAuthorizationManager(GatewayApiAccessPolicy apiAccessPolicy) {
        this.apiAccessPolicy = apiAccessPolicy;
    }

    /**
     * url 级权限校验
     *
     * @param authentication 认证信息
     * @param context        请求上下文
     * @return Mono<AuthorizationDecision> 是否有权限
     */
    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
        ServerWebExchange exchange = context.getExchange();
        // 如果是预检请求（OPTIONS），直接放行
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return Mono.just(AUTHORIZATION_DECISION_TRUE);
        }
        Optional<GatewayApiAccessProperties.AccessMode> apiMode;
        try {
            apiMode = apiAccessPolicy.resolve(exchange);
        } catch (RuntimeException exception) {
            // 动态规则损坏时失败关闭，不能退回更宽松的旧逻辑。
            return Mono.just(AUTHORIZATION_DECISION_FALSE);
        }
        if (apiMode.orElse(null) == GatewayApiAccessProperties.AccessMode.PUBLIC) {
            return Mono.just(AUTHORIZATION_DECISION_TRUE);
        }
        if (apiMode.orElse(null) == GatewayApiAccessProperties.AccessMode.AUTHENTICATED) {
            return authenticated(authentication);
        }
        boolean forceResourcePermission = apiMode.orElse(null)
                == GatewayApiAccessProperties.AccessMode.RESOURCE_REQUIRED;
        // 用户角色拥有的authorities 与 用户请求url所需authorities 进行匹配，任意包含则返回true(有权限)
        return authentication
                // 认证通过的
                .filter(Authentication::isAuthenticated)
                // 用户token中的角色 所拥有的 authorities 和请求进行匹配
                .flatMap(authToken -> hasPermission(authToken, exchange, forceResourcePermission))
                // 如果为空则返回 false 无权限
                .defaultIfEmpty(AUTHORIZATION_DECISION_FALSE);
    }

    private Mono<AuthorizationDecision> authenticated(Mono<Authentication> authentication) {
        return authentication.filter(Authentication::isAuthenticated)
                .map(ignored -> AUTHORIZATION_DECISION_TRUE)
                .defaultIfEmpty(AUTHORIZATION_DECISION_FALSE);
    }

    /**
     * 用户token中的authorities 与 请求需要的authorities进行匹配，任意包含则通过返回 有权限
     *
     * @param authToken 用户 token
     * @param exchange  请求信息
     * @return Mono<AuthorizationDecision> 是否匹配
     */
    private Mono<AuthorizationDecision> hasPermission(Authentication authToken, ServerWebExchange exchange,
            boolean forceResourcePermission) {
        // 如果权限开关关闭，则表示不进行url权限校验，则直接放行
        if (!permission && !forceResourcePermission) {
            return Mono.just(AUTHORIZATION_DECISION_TRUE);
        }
        // 用户拥有的角色集合，从token中获取角色列表
        Set<String> roles = AuthorityUtils.authorityListToSet(authToken.getAuthorities());
        // 根据请求和用户角色拥有权限 获取匹配的 Authority列表
        // 用户拥有的角色 对应的 authorities集合，根据角色code加载权限集合(用户拥有的权限集合)
        return authorityService.getAuthorityForRoles(roles)
                // 通过 path pattern进行匹配，如果匹配成功，则表明有权限，返回true
                .filterWhen(authority -> match(authority, exchange))
                .hasElements()
                // 匹配为true的，返回对应 AuthorizationDecision
                .map(AuthorizationDecision::new);
    }

    /**
     * authority权限定义元数据 与 请求进行匹配
     *
     * @param authority 权限定义元数据
     * @param exchange  请求上下文信息
     * @return Mono<Boolean> 是否匹配
     */
    private Mono<Boolean> match(Authority authority, ServerWebExchange exchange) {
        // 通过 path pattern进行匹配，如果匹配成功，则表明有权限
        ServerWebExchangeMatcher matcher = new PathPatternParserServerWebExchangeMatcher(authority.pattern(), HttpMethod.valueOf(authority.method()));
        return matcher.matches(exchange)
                .map(ServerWebExchangeMatcher.MatchResult::isMatch);
    }
}
