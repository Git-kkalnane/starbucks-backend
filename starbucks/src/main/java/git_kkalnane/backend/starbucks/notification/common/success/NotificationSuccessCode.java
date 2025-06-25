package git_kkalnane.backend.starbucks.notification.common.success;

import git_kkalnane.backend.starbucks._global.success.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum NotificationSuccessCode implements SuccessCode {
    NOTIFICATION_SUBSCRIBED(HttpStatus.CREATED, "알림 구독에 성공하였습니다. %s"),
    NOTIFICATION_SUBSCRIPTION_RETRIEVED(HttpStatus.OK, "알림 구독 목록이 성공적으로 조회되었습니다."),
    NOTIFICATION_RETRIEVED(HttpStatus.OK, "알림 목록이 성공적으로 조회되었습니다."),
    NOTIFICATION_DELIVERED(HttpStatus.OK, "알림이 성공적으로 전송되었습니다.")
    ;

    private final HttpStatus status;
    private final String message;

    public String getMessage(Object... args) {
        return String.format(message, args);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
