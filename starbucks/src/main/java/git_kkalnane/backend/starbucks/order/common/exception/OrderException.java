package git_kkalnane.backend.starbucks.order.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.BaseException;

public class OrderException extends BaseException {
    public OrderException(OrderErrorCode errorCode) { super(errorCode);}
}
