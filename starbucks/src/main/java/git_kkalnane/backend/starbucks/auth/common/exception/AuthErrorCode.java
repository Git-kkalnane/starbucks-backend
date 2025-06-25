package git_kkalnane.backend.starbucks.auth.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    REFRESH_TOKEN_IS_NOT_NULL(HttpStatus.BAD_REQUEST,"refreshToken은 null이 될 수 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않습니다."),
    EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "토큰 값이 비어있습니다."),
    EMAIL_INVALID_EXCEPTION(HttpStatus.BAD_REQUEST, "존재하지 않는 이메일입니다."),
    PASSWORD_INVALID_EXCEPTION(HttpStatus.BAD_REQUEST, "패스워드가 올바르지 않습니다.");

    public static final String PREFIX = "[AUTH ERROR] ";

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
