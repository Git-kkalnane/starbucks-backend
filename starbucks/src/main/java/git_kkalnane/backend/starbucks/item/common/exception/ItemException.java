package git_kkalnane.backend.starbucks.item.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.BaseException;

public class ItemException extends BaseException {
    public ItemException(ItemErrorCode errorCode) {super(errorCode);}
}
