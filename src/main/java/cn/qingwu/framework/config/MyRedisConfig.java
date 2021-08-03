package cn.qingwu.framework.config;

import cn.qingwu.framework.auto.EnableConfigurationMyRedisUtilRegistrar;
import cn.qingwu.framework.util.MyRedisUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import org.springframework.lang.Nullable;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.time.Duration;


/**
 * @author lidehua
 */
@Configuration
@EnableConfigurationProperties(MyRedisProperties.class)
@EnableCaching
public class MyRedisConfig extends CachingConfigurerSupport {


    @Resource
    private MyRedisProperties myRedisProperties;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }

    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory lettuceConnectionFactory) {
        //缓存配置对象
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        /*设置缓存的默认超时时间：30分钟*/
        redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofMinutes(30L))
                /*如果是空值，不缓存*/
                .disableCachingNullValues()
                /*设置key序列化器*/
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                /*设置value序列化器*/
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer((valueSerializer())));
        return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(createConnectionFactory(lettuceConnectionFactory,myRedisProperties.getDatabase())))
                .cacheDefaults(redisCacheConfiguration).build();
    }

    @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer() {

            private final Charset charset = Charset.forName("UTF8");

            @Override
            public byte[] serialize(@Nullable String string) {
                return string == null ? null : (myRedisProperties.getPrefix() + "_" + string).getBytes(this.charset);
            }

            @Override
            public String deserialize(@Nullable byte[] bytes) {
                String saveKey = new String(bytes, charset);
                int indexOf = saveKey.indexOf(myRedisProperties.getPrefix());
                if (indexOf > 0) {
                    return null;//key缺少前缀
                } else {
                    saveKey = saveKey.substring(indexOf);
                }
                return (saveKey.getBytes() == null ? null : saveKey);
            }
        };
    }

    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();

    }

    /*使用Jackson序列化器*/
    private RedisSerializer<Object> valueSerializer() {
        return new GenericJackson2JsonRedisSerializer();

    }


    /**
     * redisTemplate相关配置
     * @param lettuceConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 配置连接工厂
        template.setConnectionFactory(createConnectionFactory(lettuceConnectionFactory, myRedisProperties.getDatabase()));

        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
        Jackson2JsonRedisSerializer jacksonSerial = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jacksonSerial.setObjectMapper(om);

        // 值采用json序列化
        template.setValueSerializer(jacksonSerial);
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());

        // 设置hash key 和value序列化模式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jacksonSerial);
        template.afterPropertiesSet();

        return template;
    }

    public  LettuceConnectionFactory createConnectionFactory(LettuceConnectionFactory lettuceConnectionFactory, int dbIndex) {
        LettuceClientConfiguration clientConfiguration = lettuceConnectionFactory.getClientConfiguration();
        RedisStandaloneConfiguration standaloneConfiguration = lettuceConnectionFactory.getStandaloneConfiguration();
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(standaloneConfiguration.getHostName(), standaloneConfiguration.getPort());
        serverConfig.setDatabase(dbIndex);
        serverConfig.setPassword(myRedisProperties.getPassword());
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(serverConfig, clientConfiguration);
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }


    @ConditionalOnBean(EnableConfigurationMyRedisUtilRegistrar.class)
    @Bean
    public MyRedisUtil myRedisUtil(){
        return new MyRedisUtil(redisTemplate);
    }

}