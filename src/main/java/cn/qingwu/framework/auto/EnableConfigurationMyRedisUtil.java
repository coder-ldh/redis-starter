package cn.qingwu.framework.auto;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author lidehua
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({EnableConfigurationMyRedisUtilRegistrar.class})
public @interface EnableConfigurationMyRedisUtil {

}
