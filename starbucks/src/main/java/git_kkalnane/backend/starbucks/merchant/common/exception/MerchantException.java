package git_kkalnane.backend.starbucks.merchant.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.BaseException;

public class MerchantException extends BaseException {

    public MerchantException(MerchantErrorCode errorCode) {
        super(errorCode);
    }

    public MerchantException(MerchantErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
