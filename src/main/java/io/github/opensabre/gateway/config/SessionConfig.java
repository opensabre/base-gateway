package io.github.opensabre.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

@Configuration(proxyBeanMethods = false)
@EnableRedisWebSession(
        maxInactiveIntervalInSeconds = 7200,
        redisNamespace = "${spring.session.redis.namespace:opensabre:gateway:session}"
)
public class SessionConfig {
}
