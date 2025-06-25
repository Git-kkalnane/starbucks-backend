package git_kkalnane.backend.starbucks.notification.common.exception;

import git_kkalnane.backend.starbucks._global.error.core.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 알림입니다. 알림 타입을 확인해주세요. 알림 타입: %s"),
    INVALID_NOTIFICATION_TARGET_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 알림 대상입니다. 알림 대상 타입을 확인해주세요. 알림 대상 타입: %s"),
    EVENT_ID_IS_NOT_EMPTY(HttpStatus.BAD_REQUEST, "이벤트 아이디는 비어있을 수 없습니다."),
    EVENT_ID_INVALID(HttpStatus.BAD_REQUEST, "이벤트 아이디가 형식을 만족하지 않습니다."),
    SENDER_ID_IS_NOT_EMPTY(HttpStatus.BAD_REQUEST, "전송자(Sender) 아이디는 0 미만이거나 비어있을 수 없습니다."),
    RECEIVER_ID_IS_NOT_EMPTY(HttpStatus.BAD_REQUEST, "수신자(Receiver) 아이디는 0 미만이거나 비어있을 수 없습니다."),
    ;

    public static final String PREFIX = "[NOTIFICATION ERROR] ";

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
