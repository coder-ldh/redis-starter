package cn.qingwu.framework.lock.aspect;

import cn.qingwu.framework.base.exception.MyResultException;
import cn.qingwu.framework.lock.annotation.DLock;
import cn.qingwu.framework.lock.service.DistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.aspectj.lang.reflect.MethodSignature;
import java.lang.reflect.Method;

/**
 * @author ldh
 */
@Aspect
@Component
@Order(100)
public class DistributedLockAspect {
    private final DistributedLock distributedLock;

    @Autowired
    public DistributedLockAspect(DistributedLock distributedLock) {
        this.distributedLock = distributedLock;
    }

    @Around("@annotation(dLock)")
    public Object lock(ProceedingJoinPoint point, DLock dLock) {
        String lockKey = parseKey(dLock.value(), ((MethodSignature) point.getSignature()).getMethod(), point.getArgs());
        int timeout = 100;
        try {
            timeout = Integer.parseInt(dLock.timeout());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            return distributedLock.lock(lockKey, () -> {
                try {
                    return point.proceed();
                } catch (Throwable throwable) {
                    if (throwable instanceof MyResultException) {
                        throw (MyResultException) throwable;
                    }
                    throw new RuntimeException(throwable);
                }
            }, timeout);
        } catch (Exception e) {
            if (e instanceof MyResultException) {
                throw (MyResultException) e;
            }
            throw new RuntimeException(e);
        }
    }

    private String parseKey(String key, Method method, Object[] args) {
        //获取被拦截方法参数名列表(使用Spring支持类库)
        LocalVariableTableParameterNameDiscoverer u =
                new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = u.getParameterNames(method);

        //使用SPEL进行key的解析
        ExpressionParser parser = new SpelExpressionParser();
        //SPEL上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        //把方法参数放入SPEL上下文中
        if (paraNameArr != null) {
            for (int i = 0; i < paraNameArr.length; i++) {
                context.setVariable(paraNameArr[i], args[i]);
            }
        }
        return parser.parseExpression(key).getValue(context, String.class);
    }
}
