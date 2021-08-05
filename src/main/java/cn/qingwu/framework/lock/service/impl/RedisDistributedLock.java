package cn.qingwu.framework.lock.service.impl;

import cn.qingwu.framework.lock.exception.UnableToAcquireLockException;
import cn.qingwu.framework.lock.service.AcquiredLockWrapper;
import cn.qingwu.framework.lock.service.DistributedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Created by SunLeqi on 9/4/2017
 */
@Component
public class RedisDistributedLock implements DistributedLock {

    private final static String LOCKER_PREFIX = "DLOCK:";
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RedissonClient redisson;

    @Autowired
    public RedisDistributedLock(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public <T> T lock(String resourceName, AcquiredLockWrapper<T> worker) throws InterruptedException, UnableToAcquireLockException, Exception {
        return lock(resourceName, worker, 200, 100);
    }

    @Override
    public <T> T lock(String resourceName, AcquiredLockWrapper<T> worker, int lockTime) throws UnableToAcquireLockException, Exception {
        return lock(resourceName, worker, lockTime, 100);
    }

    @Override
    public <T> T lock(String resourceName, AcquiredLockWrapper<T> worker, int lockTime, int waitTime) throws UnableToAcquireLockException, Exception {
        logger.info("redisLock, tryGetLock=" + LOCKER_PREFIX + resourceName);
        RLock lock = redisson.getLock(LOCKER_PREFIX + resourceName);
        logger.info("redisLock, getLockSuccess=" + LOCKER_PREFIX + resourceName);
        // Wait for 100 seconds seconds and automatically unlock it after lockTime seconds
        boolean success = lock.tryLock(waitTime, lockTime, TimeUnit.SECONDS);
        logger.info("redisLock, tryLock=" + LOCKER_PREFIX + resourceName + ", result" + success);
        if (success) {
            try {
                return worker.invokeAfterLockAcquire();
            } finally {
                logger.info("redisLock, tryLock=" + LOCKER_PREFIX + resourceName + ", unlock");
                lock.unlock();
            }
        }
        throw new UnableToAcquireLockException();
    }
}