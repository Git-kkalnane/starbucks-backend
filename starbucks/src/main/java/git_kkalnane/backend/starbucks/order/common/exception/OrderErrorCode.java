package git_kkalnane.backend.starbucks.order.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderErrorCode implements ErrorCode {

    ORDER_ERROR_CODE(HttpStatus.BAD_REQUEST, "주문생성이 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    OrderErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
