package git_kkalnane.backend.starbucks.member.common.exception;

import git_kkalnane.backend.starbucks.global.error.core.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    MEMBER_NAME_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "멤버 이름의 길이는 15글자를 넘을 수 없습니다."),
    INVALID_MEMBER_NAME(HttpStatus.BAD_REQUEST, "멤버 이름에는 한글, 영문, 숫자만 포함될 수 있습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."),

    ;

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
