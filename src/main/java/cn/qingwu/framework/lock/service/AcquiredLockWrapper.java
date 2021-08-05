package cn.qingwu.framework.lock.service;

/**
 * ldh
 */
public interface AcquiredLockWrapper<T> {

    /**
     * do
     * @return
     * @throws RuntimeException
     */
    T invokeAfterLockAcquire() throws RuntimeException;
}
