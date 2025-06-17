package git_kkalnane.backend.starbucks.store.common.exception;

import git_kkalnane.backend.starbucks.global.error.core.BaseException;

public class StoreException extends BaseException {

    public StoreException(StoreErrorCode errorCode) {
        super(errorCode);
    }
}
