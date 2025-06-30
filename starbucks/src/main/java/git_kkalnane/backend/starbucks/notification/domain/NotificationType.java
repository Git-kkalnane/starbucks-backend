package git_kkalnane.backend.starbucks.notification.domain;

import git_kkalnane.backend.starbucks.notification.common.exception.NotificationErrorCode;
import git_kkalnane.backend.starbucks.notification.common.exception.NotificationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    SUBSCRIBE("알림 구독", "알림 구독이 완료되었습니다."),
    ORDER_ACCEPTED("주문 접수 완료","%s님의 주문을 준비 중입니다. (%s)"),
    ORDER_SET("메뉴 준비 완료","메뉴가 모두 준비되었어요. (%s) 픽업대에서 메뉴를 픽업해주세요!"),
    ORDER_CREATED("주문 발생", "새로운 주문이 발생했습니다."),
    ;

    private final String title;
    private final String message;

    public String getTitle(Object ... args) {
        return title.formatted(args);
    }

    public String getMessage() {
        return message;
    }

    public String getMessage(Object ... args) {
        return message.formatted(args);
    }

    public static NotificationType findByName(String givenName){
        try{
            return NotificationType.valueOf(givenName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotificationException(
                NotificationErrorCode.INVALID_NOTIFICATION_TYPE, givenName
            );
        }
    }
}
