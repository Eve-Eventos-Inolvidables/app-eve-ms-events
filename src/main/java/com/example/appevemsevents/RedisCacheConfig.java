package com.example.appevemsevents;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@ConditionalOnProperty(name="spring.cache.type",havingValue = "redis")
public class RedisCacheConfig {
    public static final String CACHE_SD = "sd";
    public static final String CACHE_SD_EVENT = "sd-event";
    public static final String CACHE_SD_EVENT_INFO = "sd-event-info";
    @Bean
    
    public CacheManager cacheManage(
            RedisConnectionFactory factory,
            @Value("${cache.event.ttl-seconds}") long eventTtlSeconds,
            @Value("${cache.info.ttl-seconds}") long infoTtlSeconds
    ){
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        RedisCacheConfiguration defaulConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer));

        Map<String,RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put(CACHE_SD_EVENT, defaulConfiguration.entryTtl(Duration.ofSeconds(eventTtlSeconds)));
        perCache.put(CACHE_SD_EVENT_INFO, defaulConfiguration.entryTtl(Duration.ofSeconds(infoTtlSeconds)));

        return RedisCacheManager.builder(factory)
        .cacheDefaults(defaulConfiguration)
        .withInitialCacheConfigurations(perCache)
        .build();

    }
}
