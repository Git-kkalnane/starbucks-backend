package git_kkalnane.backend.starbucks.auth.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    // 로그인 요청을 처리할 때 사용
    EMAIL_INVALID_EXCEPTION(HttpStatus.BAD_REQUEST, "존재하지 않는 이메일입니다."),
    PASSWORD_INVALID_EXCEPTION(HttpStatus.BAD_REQUEST, "패스워드가 올바르지 않습니다."),
    // 로그아웃 요청 또는 액세스 토큰 재발급 요청을 처리할 때 사용
    TOKEN_NOT_FOUND_IN_DB(HttpStatus.NOT_FOUND, "토큰이 존재하지 않습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다."),
    REFRESH_TOKEN_IS_NOT_NULL(HttpStatus.BAD_REQUEST,"refreshToken은 null이 될 수 없습니다."),
    // 요청에 포함된 토큰을 검증할 때 사용
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "위변조된 토큰입니다."),
    // 헤더에서 토큰을 추출할 때 사용
    MISSING_PREFIX(HttpStatus.BAD_REQUEST, "Authorization 헤더에 Bearer가 포함되어 있지 않습니다."),
    INVALID_AUTH_HEADER(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 헤더입니다."),
    // 토큰 관련 - 주로 JwtTokenProvider에서 사용
    MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰 형식입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "지원하지 않는 토큰 형식입니다."),
    INVALID_TOKEN_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 토큰 파라미터입니다.");

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
