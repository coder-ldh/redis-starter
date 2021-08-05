package cn.qingwu.framework.lock.service;

/**
 * ldh
 */
public interface AcquiredLockWrapper<T> {

    T invokeAfterLockAcquire() throws Throwable;
}
