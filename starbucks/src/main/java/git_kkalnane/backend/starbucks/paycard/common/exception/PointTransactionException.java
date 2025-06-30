package git_kkalnane.backend.starbucks.paycard.common.exception;


import git_kkalnane.backend.starbucks._global.error.core.BaseException;

public class PointTransactionException extends BaseException {

    public PointTransactionException(PointTransactionErrorCode errorCode) {
        super(errorCode);
    }

    public PointTransactionException(PointTransactionErrorCode errorCode, Object ... args) {
        super(errorCode,args);
    }
}
