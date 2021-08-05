package cn.qingwu.framework.lock.service.impl;

import cn.qingwu.framework.lock.constant.LockConstant;
import cn.qingwu.framework.lock.exception.UnableToAcquireLockException;
import cn.qingwu.framework.lock.service.AcquiredLockWrapper;
import cn.qingwu.framework.lock.service.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * @author lidehua
 */
@Slf4j
@Component
public class RedisDistributedLock implements DistributedLock {


    @Autowired
    private RedissonClient redissonClient;

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
        log.info("redisLock, tryGetLock=" + LockConstant.LOCK_PREFIX + resourceName);
        RLock lock = redissonClient.getLock(LockConstant.LOCK_PREFIX + resourceName);
        log.info("redisLock, getLockSuccess=" + LockConstant.LOCK_PREFIX + resourceName);
        /*Wait for 100 seconds seconds and automatically unlock it after lockTime seconds*/
        boolean success = lock.tryLock(waitTime, lockTime, TimeUnit.SECONDS);
        log.info("redisLock, tryLock=" + LockConstant.LOCK_PREFIX + resourceName + ", result" + success);
        if (success) {
            try {
                return worker.invokeAfterLockAcquire();
            } finally {
                log.info("redisLock, tryLock=" + LockConstant.LOCK_PREFIX + resourceName + ", unlock");
                lock.unlock();
            }
        }
        throw new UnableToAcquireLockException();
    }
}