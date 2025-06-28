package git_kkalnane.backend.starbucks.paycard.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PayCardErrorCode implements ErrorCode {
    PAY_CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 카드가 존재하지 않습니다."),
    PAY_CARD_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 보유 중인 결제 카드가 있습니다."),
    NOT_ENOUGH_PAY_CARD_AMOUNT(HttpStatus.BAD_REQUEST, "결제 카드의 잔액이 부족합니다.");

    public static final String PREFIX = "[PAY_CARD ERROR] ";

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
