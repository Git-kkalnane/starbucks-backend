package git_kkalnane.backend.starbucks.payment.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보가 존재하지 않습니다."),
    NOT_ENOUGH_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "잔액이 부족하여 결제에 실패했습니다. 요청 금액: %s, 현재 잔액: %s"),
    ;

    public static final String PREFIX = "[PAYMENT ERROR] ";

    private final HttpStatus status;
    private final String rawMessage;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return PREFIX + rawMessage;
    }
}
