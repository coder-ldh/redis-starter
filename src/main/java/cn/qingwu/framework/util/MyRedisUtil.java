package cn.qingwu.framework.util;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;


/**
 * @author lidehua
 */

public class MyRedisUtil {

    public static final Long MILLISECOND = 1L;
    public static final Long SECOND = 1000 * MILLISECOND;
    public static final Long MINUTE = 60 * SECOND;
    public static final Long HOUR = 60 * MINUTE;
    public static final Long DAY = 24 * HOUR;

    private RedisTemplate redisTemplate;

    public MyRedisUtil(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value, Long time) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMillis(time));
    }

    public void set(String key, Object value) {
        set(key, value, DAY);
    }

    public <T extends Object> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void delete(Collection collection) {
        redisTemplate.delete(collection);
    }

    public <T extends Object> Set<T> keys(String key) {
        return redisTemplate.keys(key);
    }

    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // list operation
    public void lPush(String key, String... value) {
        redisTemplate.opsForList().leftPushAll(key, value);
    }

    public void rPush(String key, String... value) {
        redisTemplate.opsForList().rightPushAll(key, value);
    }

    public <T extends Object> T lPop(String key) {
        return (T) redisTemplate.opsForList().leftPop(key);
    }

    public <T extends Object> T rPop(String key) {
        return (T) redisTemplate.opsForList().leftPop(key);
    }

    public long lLen(String key) {
        return redisTemplate.opsForList().size(key);
    }

    public void lSet(String key, long index, String value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    public void lRem(String key, long index, String value) {
        redisTemplate.opsForList().remove(key, index, value);
    }
}
