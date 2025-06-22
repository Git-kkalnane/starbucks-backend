package git_kkalnane.backend.starbucks.order.common.success;

import git_kkalnane.backend.starbucks._global.success.SuccessCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderSuccessCode implements SuccessCode {
    ORDER_SUCCESS_CREATED(HttpStatus.CREATED, "주문이 성공적으로 생성되었습니다.");

    private final HttpStatus status;
    private final String message;

    OrderSuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
