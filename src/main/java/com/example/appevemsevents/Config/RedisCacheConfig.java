package com.example.appevemsevents.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import java.util.HashMap;
import java.util.Map;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    public static final String CACHE_SD_EVENT_SECTOR = "event-sector";
    public static final String CACHE_SD_EVENT_PRECIO = "event-precio";
    public static final String CACHE_SD_EVENT_BATCH = "event-batch";
    public static final String CACHE_SD_EVENT= "event";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,
                                     @Value("${cache.event.ttl-seconds}") long eventTtlSeconds,
                                     @Value("${cache.info.ttl-seconds}") long infoTtlSeconds) {

        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        GenericJacksonJsonRedisSerializer valueSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(typeValidator)
                .enableSpringCacheNullValueSupport()
                .build();

        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer)
                );

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put(CACHE_SD_EVENT, redisCacheConfiguration.entryTtl(Duration.ofSeconds(eventTtlSeconds)));
        perCache.put(CACHE_SD_EVENT_BATCH, redisCacheConfiguration.entryTtl(Duration.ofSeconds(infoTtlSeconds)));
        perCache.put(CACHE_SD_EVENT_SECTOR, redisCacheConfiguration.entryTtl(Duration.ofSeconds(infoTtlSeconds)));
        perCache.put(CACHE_SD_EVENT_PRECIO, redisCacheConfiguration.entryTtl(Duration.ofSeconds(infoTtlSeconds)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration.entryTtl(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(perCache)
                .build();
    }

}
