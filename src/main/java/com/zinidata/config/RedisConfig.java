package com.zinidata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

/**
 * Redis 설정
 */
@Configuration
public class RedisConfig {

    /**
     * Redis Template 설정
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        
        // Key Serializer
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        
        // Value Serializer
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        return redisTemplate;
    }
    
    /**
     * Redis 세션 설정을 위한 ConfigureRedisAction 비활성화
     * 클라우드 환경에서 Redis CONFIG 명령어 사용 불가 시 필요
     */
    @Bean
    public ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
} 