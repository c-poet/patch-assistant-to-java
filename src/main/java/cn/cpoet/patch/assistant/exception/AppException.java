package cn.cpoet.patch.assistant.exception;

/**
 * 应用异常
 *
 * @author CPoet
 */
public class AppException extends RuntimeException {

    private static final long serialVersionUID = -2689260496095807077L;

    public AppException(String message) {
        super(message);
    }

    public AppException(Throwable cause) {
        super(cause);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
