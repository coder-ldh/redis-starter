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
        //??????????????????
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        /*????????????????????????????????????30??????*/
        redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofMinutes(30L))
                /*???????????????????????????*/
                .disableCachingNullValues()
                /*??????key????????????*/
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                /*??????value????????????*/
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
                    return null;//key????????????
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

    /*??????Jackson????????????*/
    private RedisSerializer<Object> valueSerializer() {
        return new GenericJackson2JsonRedisSerializer();

    }


    /**
     * redisTemplate????????????
     * @param lettuceConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // ??????????????????
        template.setConnectionFactory(createConnectionFactory(lettuceConnectionFactory, myRedisProperties.getDatabase()));

        //??????Jackson2JsonRedisSerializer???????????????????????????redis???value??????????????????JDK?????????????????????
        Jackson2JsonRedisSerializer jacksonSerial = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper om = new ObjectMapper();
        // ???????????????????????????field,get???set,????????????????????????ANY???????????????private???public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // ????????????????????????????????????????????????final????????????final?????????????????????String,Integer??????????????????
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jacksonSerial.setObjectMapper(om);

        // ?????????json?????????
        template.setValueSerializer(jacksonSerial);
        //??????StringRedisSerializer???????????????????????????redis???key???
        template.setKeySerializer(new StringRedisSerializer());

        // ??????hash key ???value???????????????
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