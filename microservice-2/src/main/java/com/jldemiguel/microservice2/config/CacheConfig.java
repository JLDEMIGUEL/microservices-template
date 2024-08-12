package com.jldemiguel.microservice2.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_NAME = "productCache";

    @Bean
    public CacheManager cacheManager() {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES);
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CACHE_NAME);
        cacheManager.setCaffeine(caffeine);
        cacheManager.setAsyncCacheMode(true);
        return cacheManager;
    }
}
