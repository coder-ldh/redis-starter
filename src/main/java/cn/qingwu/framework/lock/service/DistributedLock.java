package cn.qingwu.framework.lock.service;


import cn.qingwu.framework.lock.exception.UnableToAcquireLockException;

/**
 *
 * @author lidehua
 */
public interface DistributedLock {

    /**
     * 获取锁
     *
     * @param resourceName 锁的名称
     * @param wrapper      获取锁后的处理类
     * @param <T>
     * @return 处理完具体的业务逻辑要返回的数据
     * @throws UnableToAcquireLockException
     * @throws Exception
     */
    <T> T lock(String resourceName, AcquiredLockWrapper<T> wrapper) throws UnableToAcquireLockException, Exception;

    <T> T lock(String resourceName, AcquiredLockWrapper<T> wrapper, int lockTime) throws UnableToAcquireLockException, Exception;

    <T> T lock(String resourceName, AcquiredLockWrapper<T> worker, int lockTime, int waitTime) throws UnableToAcquireLockException, Exception;
}