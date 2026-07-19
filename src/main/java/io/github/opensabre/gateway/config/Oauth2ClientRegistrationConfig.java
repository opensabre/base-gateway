package io.github.opensabre.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;

/** 配置网关 OAuth2 Client 的可刷新内存仓库。 */
@Configuration(proxyBeanMethods = false)
public class Oauth2ClientRegistrationConfig {
    @Bean
    public ReactiveClientRegistrationRepository reactiveClientRegistrationRepository(Environment environment) {
        return new RefreshingReactiveClientRegistrationRepository(environment);
    }
}
