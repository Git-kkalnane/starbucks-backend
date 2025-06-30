package git_kkalnane.backend.starbucks.order.common.success;

import git_kkalnane.backend.starbucks._global.success.SuccessCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderSuccessCode implements SuccessCode {
    ORDER_SUCCESS_CREATED(HttpStatus.CREATED, "주문이 성공적으로 생성되었습니다."),
    ORDER_DETAIL_VIEWED(HttpStatus.OK, "주문 상세 정보 조회가 성공했습니다."),
    ORDER_CURRENT_VIEWED(HttpStatus.OK, "현재 진행중인 주문 목록 조회가 성공했습니다."),
    STORE_ORDER_DETAIL_VIEWED(HttpStatus.OK, "매장 주문 상세 정보 조회 성공"),
    STORE_ORDERS_VIEWED(HttpStatus.OK, "매장의 현재 주문 목록 조회가 성공했습니다."),
    STORE_ORDER_HISTORY_VIEWED(HttpStatus.OK, "매장 과거 주문 내역 조회 성공");

    private final HttpStatus status;
    private final String message;

    OrderSuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
