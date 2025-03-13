package com.yooyoung.clotheser.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    // Redis에서 key에 대한 value 가져옴
    public String getData(String key){
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    // Redis에 key와 value 저장하고, 지정된 시간 후에 데이터가 만료되도록 설정
    public void setDataExpire(String key, String value, long duration){
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    // key에 해당하는 데이터 삭제
    public void deleteData(String key){
        redisTemplate.delete(key);
    }

}
