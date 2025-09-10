package com.lgcms.leveltest.config;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;
    @Value("${spring.data.redis.database}")
    private int database;

    @Bean
    public RedisConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .clientResources(clientResources).build();
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(host, port);
        redisConfig.setDatabase(database);
        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    @Bean(name = "stringTemplate")
    @Primary
    public RedisTemplate<String, String> stringTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public ClientResources clientResources(ObservationRegistry observationRegistry) {
        return ClientResources.builder()
            .tracing(new MicrometerTracing(observationRegistry, "redis-cache"))
            .build();
    }
}