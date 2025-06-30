package git_kkalnane.backend.starbucks.payment.common.exception;


import git_kkalnane.backend.starbucks._global.error.core.BaseException;

public class PaymentException extends BaseException {

    public PaymentException(PaymentErrorCode errorCode) {
        super(errorCode);
    }

    public PaymentException(PaymentErrorCode errorCode, Object ... args) {
        super(errorCode,args);
    }
}
