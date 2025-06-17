package git_kkalnane.backend.starbucks.notification.common.success;

import git_kkalnane.backend.starbucks.global.success.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum NotificationSuccessCode implements SuccessCode {
    NOTIFICATION_SUBSCRIBED(HttpStatus.CREATED, "알림 구독에 성공하였습니다. %s"),
    NOTIFICATION_SUBSCRIPTION_RETRIEVED(HttpStatus.OK, "알림 구독 목록이 성공적으로 조회되었습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
