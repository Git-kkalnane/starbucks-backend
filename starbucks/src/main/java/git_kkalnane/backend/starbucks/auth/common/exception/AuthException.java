package git_kkalnane.backend.starbucks.auth.common.exception;


import git_kkalnane.backend.starbucks.global.error.core.BaseException;

public class AuthException extends BaseException {

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(AuthErrorCode errorCode, Object ... args) {
        super(errorCode,args);
    }
}
