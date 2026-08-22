package io.github.opensabre.gateway.online;

import io.github.opensabre.gateway.filter.ClientIpResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OnlineUserRecordService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final String sessionNamespace;
    private final ClientIpResolver clientIpResolver;

    public OnlineUserRecordService(ReactiveStringRedisTemplate redisTemplate,
                                   @Value("${spring.session.redis.namespace:opensabre:gateway:session}") String sessionNamespace,
                                   ClientIpResolver clientIpResolver) {
        this.redisTemplate = redisTemplate;
        this.sessionNamespace = sessionNamespace;
        this.clientIpResolver = clientIpResolver;
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

    private Map<String, String> recordValues(ServerWebExchange exchange, String sessionId,
                                                    Authentication authentication, String loginTime, String now) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        Map<String, String> values = new LinkedHashMap<>();
        values.put("sessionId", sessionId);
        // OAuth2/OIDC sessions may expose the display name through Authentication#getName.
        // Persist the stable login identifier separately for the online-user username column.
        values.put("username", resolveUsername(authentication));
        values.put("displayName", authentication.getName());
        putIfPresent(values, "ip", clientIpResolver.resolve(exchange));
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

    static String resolveUsername(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
            String username = firstText(principal.getAttribute("sub"),
                    principal.getAttribute("preferred_username"),
                    principal.getAttribute("user_name"));
            if (StringUtils.hasText(username)) {
                return username;
            }
        }
        return authentication.getName();
    }

    private static String firstText(Object... values) {
        for (Object value : values) {
            if (value != null && StringUtils.hasText(value.toString())) {
                return value.toString();
            }
        }
        return null;
    }
}
