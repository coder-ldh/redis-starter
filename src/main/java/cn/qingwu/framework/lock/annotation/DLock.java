package cn.qingwu.framework.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁的注解使用方式
 *
 * <p>注意：该锁的切面顺序值为100
 * <p>若需要包住事务的切面逻辑，需要在Application中配置@EnableTransactionManagement(order = 200)，可不设置为200，只需要大于锁的切面顺序值100即可
 *
 * @author ldh
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DLock {

    /**
     * 锁的key，支持SPEL表达式
     */
    String value();

    /**
     * 锁的超时时间，单位为秒
     */
    String timeout() default "100";
}
