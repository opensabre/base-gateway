package io.github.opensabre.gateway.online;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OnlineUserRecordService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final String sessionNamespace;

    public OnlineUserRecordService(ReactiveStringRedisTemplate redisTemplate,
                                   @Value("${spring.session.redis.namespace:opensabre:gateway:session}") String sessionNamespace) {
        this.redisTemplate = redisTemplate;
        this.sessionNamespace = sessionNamespace;
    }

    public Mono<Void> record(ServerWebExchange exchange, WebSession session, Authentication authentication) {
        if (!shouldRecord(authentication)) {
            return Mono.empty();
        }
        String sessionId = session.getId();
        String onlineSessionKey = OnlineUserKeys.onlineSessionKey(sessionNamespace, sessionId);
        String now = LocalDateTime.now().toString();

        return redisTemplate.opsForHash()
                .get(onlineSessionKey, "loginTime")
                .map(Object::toString)
                .defaultIfEmpty(now)
                .flatMap(loginTime -> {
                    Map<String, String> values = recordValues(exchange, sessionId, authentication, loginTime, now);
                    Duration ttl = session.getMaxIdleTime();
                    return redisTemplate.opsForHash().putAll(onlineSessionKey, values)
                            .then(redisTemplate.expire(onlineSessionKey, ttl))
                            .then(redisTemplate.opsForSet().add(OnlineUserKeys.onlineSessionsKey(sessionNamespace), sessionId))
                            .then();
                });
    }

    static boolean shouldRecord(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && StringUtils.hasText(authentication.getName())
                && !"anonymousUser".equals(authentication.getName());
    }

    static String clientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress == null) {
            return null;
        }
        return remoteAddress.getAddress() == null ? remoteAddress.getHostString() : remoteAddress.getAddress().getHostAddress();
    }

    private static Map<String, String> recordValues(ServerWebExchange exchange, String sessionId,
                                                    Authentication authentication, String loginTime, String now) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        Map<String, String> values = new LinkedHashMap<>();
        values.put("sessionId", sessionId);
        values.put("username", authentication.getName());
        values.put("displayName", authentication.getName());
        putIfPresent(values, "ip", clientIp(exchange));
        putIfPresent(values, "userAgent", headers.getFirst(HttpHeaders.USER_AGENT));
        values.put("authenticationType", authentication.getClass().getSimpleName());
        values.put("loginTime", loginTime);
        values.put("lastAccessTime", now);
        return values;
    }

    private static void putIfPresent(Map<String, String> values, String key, String value) {
        if (StringUtils.hasText(value)) {
            values.put(key, value);
        }
    }
}
