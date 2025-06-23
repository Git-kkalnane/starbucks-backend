package git_kkalnane.backend.starbucks.cart.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.BaseException;

public class CartException extends BaseException {
    public CartException(CartErrorCode errorCode) {super(errorCode);}
}
