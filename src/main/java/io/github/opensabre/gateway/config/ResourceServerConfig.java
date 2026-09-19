package io.github.opensabre.gateway.config;

import io.github.opensabre.security.actuator.ActuatorMonitoringAccess;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

/**
 * 资源服务器配置
 *
 * @author zhoutaoo
 */
@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class ResourceServerConfig {

    @Resource
    private OpensabreGatewayConfig opensabreGatewayConfig;

    @Resource
    private ReactiveAuthorizationManager<AuthorizationContext> authorizationManager;

    /**
     * 配置认证相关的过滤器链
     *
     * @param http Spring Security的核心配置类
     * @return 过滤器链
     */
    @Bean
    @Order(0)
    public SecurityWebFilterChain actuatorSecurityFilterChain(ServerHttpSecurity http) {
        http.securityMatcher(ServerWebExchangeMatchers.pathMatchers("/actuator/**"));
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.authorizeExchange(authorize -> authorize
                // This filter verifies the short-lived internal token on fixed metric paths.
                .pathMatchers(ActuatorMonitoringAccess.metricPathArray()).permitAll()
                .anyExchange().hasAuthority("SCOPE_actuator.read"));
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)));
        http.oauth2ResourceServer(resourceServer -> resourceServer
                .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor())));
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityWebFilterChain defaultSecurityFilterChain(ServerHttpSecurity http) {
        // 禁用csrf与cors
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.cors(ServerHttpSecurity.CorsSpec::disable);

        // 开启全局验证
        http.authorizeExchange((authorize) -> authorize
                // 仅返回不透明发布修订号，供控制面逐实例确认配置已刷新。
                .pathMatchers("/internal/gateway/revision", "/internal/gateway/routes/probe").permitAll()
                // 不需要认证的资源或服务
                .pathMatchers(opensabreGatewayConfig.getPermitPaths()).permitAll()
                // url权限校验
                .anyExchange().access(authorizationManager)
        );

        // 开启OAuth2登录
        http.oauth2Login(Customizer.withDefaults());

        // 设置当前服务为资源服务，解析请求头中的token
        http.oauth2ResourceServer((resourceServer) -> resourceServer
                        // 使用jwt ,请求中携带token访问时会触发该解析器适配器
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
                // xhr请求未携带Token处理
//                 .authenticationEntryPoint(this::authenticationEntryPoint)
                // 权限不足处理
//                 .accessDeniedHandler(this::accessDeniedHandler)
                // Token解析失败处理
//                 .authenticationFailureHandler(this::failureHandler)

        );
        return http.build();
    }

    /**
     * 自定义jwt解析器，设置解析出来的权限信息的前缀与在jwt中的key
     *
     * @return jwt解析器适配器 ReactiveJwtAuthenticationConverterAdapter
     */
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // 设置解析权限信息的前缀，设置为空是去掉前缀
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        // 设置权限信息在jwt claims中的key
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        JwtGrantedAuthoritiesConverter scopeAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt ->
                Stream.concat(grantedAuthoritiesConverter.convert(jwt).stream(),
                        scopeAuthoritiesConverter.convert(jwt).stream())
                        .distinct()
                        .toList());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}
