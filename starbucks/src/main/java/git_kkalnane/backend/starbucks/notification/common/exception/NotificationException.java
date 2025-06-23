package git_kkalnane.backend.starbucks.notification.common.exception;


import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.global.error.core.BaseException;

public class NotificationException extends BaseException {

    public NotificationException(NotificationErrorCode errorCode) {
        super(errorCode);
    }

    public NotificationException(NotificationErrorCode errorCode, Object ... args) {
        super(errorCode,args);
    }
}
