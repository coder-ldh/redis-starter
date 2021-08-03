package cn.qingwu.framework.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lidehua
 */
@Data
@Getter
@Setter
@ConfigurationProperties("spring.redis")
public class MyRedisProperties {

    /**
     * Database index used by the connection factory.
     */
    private int database = 0;

    private String prefix;

    private String password;
}
