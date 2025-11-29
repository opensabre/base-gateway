package io.github.opensabre.gateway.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CaffeineCacheManager caffeineCache() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES); // 缓存时间
        caffeineCacheManager.setCaffeine(caffeine);
        // 设置为异步
        caffeineCacheManager.setAsyncCacheMode(true);
        return caffeineCacheManager;
    }
}