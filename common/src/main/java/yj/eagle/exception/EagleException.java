package yj.eagle.exception;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/18 13:17
 */

public class EagleException extends RuntimeException {
    public EagleException(String message) {
        super(message);
    }

    public EagleException(Throwable cause) {
        super(cause);
    }

    public EagleException(String message, Throwable cause) {
        super(message, cause);
    }
}
