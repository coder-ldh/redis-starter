package cn.qingwu.framework.lock.exception;

/**
 *
 * @author lidehua
 */
public class UnableToAcquireLockException extends RuntimeException {

    private static final long serialVersionUID = -2282343993942224648L;

    public UnableToAcquireLockException() {
    }

    public UnableToAcquireLockException(String message) {
        super(message);
    }

    public UnableToAcquireLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
