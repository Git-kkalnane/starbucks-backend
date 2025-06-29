package git_kkalnane.backend.starbucks.paycard.common.exception;


import git_kkalnane.backend.starbucks._global.error.core.BaseException;

public class PayCardException extends BaseException {

    public PayCardException(PayCardErrorCode errorCode) {
        super(errorCode);
    }

    public PayCardException(PayCardErrorCode errorCode, Object ... args) {
        super(errorCode,args);
    }
}
