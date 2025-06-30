package git_kkalnane.backend.starbucks.merchant.common.success;

import git_kkalnane.backend.starbucks._global.success.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum MerchantSuccessCode implements SuccessCode {

    SIGN_UP_COMPLETED(HttpStatus.CREATED, "회원가입이 성공적으로 완료 되었습니다.");

    private final HttpStatus status;
    private final String rawMessage;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return rawMessage;
    }
}
