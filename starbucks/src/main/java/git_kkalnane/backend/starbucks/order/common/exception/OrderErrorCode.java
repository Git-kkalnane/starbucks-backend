package git_kkalnane.backend.starbucks.order.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderErrorCode implements ErrorCode {

    ORDER_ERROR_CODE(HttpStatus.BAD_REQUEST, "주문생성이 실패했습니다."),
    INVALID_ORDER_ITEM_REFERENCE(HttpStatus.BAD_REQUEST, "주문 상품이 유효한 메뉴를 참조하지 않습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 주문을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다");

    private final HttpStatus status;
    private final String message;

    OrderErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
