package git_kkalnane.backend.starbucks.merchant.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum MerchantErrorCode implements ErrorCode {

    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    MERCHANT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 매장을 찾을 수 없습니다.");

    public static final String PREFIX = "[MERCHANT ERROR] ";

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
