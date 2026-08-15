package io.github.opensabre.gateway.config;

import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * OAuth2 客户端注册信息的内存快照仓库。
 *
 * <p>网关登录请求只查询当前内存快照。配置中心刷新时重新绑定配置并原子替换快照，
 * 不会在请求链路上访问数据库、管理 API 或配置中心。</p>
 */
public class RefreshingReactiveClientRegistrationRepository
        implements ReactiveClientRegistrationRepository, ApplicationListener<EnvironmentChangeEvent> {

    private static final String OAUTH2_CLIENT_PREFIX = "spring.security.oauth2.client";
    private final Environment environment;
    private final AtomicReference<Map<String, ClientRegistration>> registrations = new AtomicReference<>(Map.of());

    public RefreshingReactiveClientRegistrationRepository(Environment environment) {
        this.environment = environment;
        refresh();
    }

    @Override
    public Mono<ClientRegistration> findByRegistrationId(String registrationId) {
        return Mono.justOrEmpty(registrations.get().get(registrationId));
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        if (event.getKeys().stream().anyMatch(key -> key.startsWith(OAUTH2_CLIENT_PREFIX))) refresh();
    }

    /** 从已刷新的 Spring Environment 重建并替换整份客户端快照。 */
    private void refresh() {
        OAuth2ClientProperties properties = Binder.get(environment)
                .bind(OAUTH2_CLIENT_PREFIX, OAuth2ClientProperties.class)
                .orElseGet(OAuth2ClientProperties::new);
        Set<String> disabledRegistrations = Set.copyOf(Binder.get(environment)
                .bind("opensabre.gateway.oauth2.disabled-registration-ids", String[].class)
                .map(List::of).orElse(List.of()));
        Map<String, ClientRegistration> snapshot = new LinkedHashMap<>();
        properties.getRegistration().forEach((registrationId, registration) -> {
            if (disabledRegistrations.contains(registrationId)) return;
            OAuth2ClientProperties.Provider provider = properties.getProvider().get(registration.getProvider());
            if (provider == null || provider.getIssuerUri() == null || provider.getIssuerUri().isBlank()) {
                throw new IllegalStateException("OAuth2 客户端缺少 issuer provider：" + registrationId);
            }
            ClientRegistration clientRegistration = ClientRegistrations.fromIssuerLocation(provider.getIssuerUri())
                    .registrationId(registrationId)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    .clientAuthenticationMethod(new ClientAuthenticationMethod(registration.getClientAuthenticationMethod()))
                    .authorizationGrantType(new AuthorizationGrantType(registration.getAuthorizationGrantType()))
                    .redirectUri(registration.getRedirectUri())
                    .scope(registration.getScope())
                    .clientName(registration.getClientName())
                    .userNameAttributeName(provider.getUserNameAttribute())
                    .build();
            snapshot.put(registrationId, clientRegistration);
        });
        registrations.set(Map.copyOf(snapshot));
    }
}
