package git_kkalnane.backend.starbucks.notification.domain;

import git_kkalnane.backend.starbucks.notification.common.exception.NotificationErrorCode;
import git_kkalnane.backend.starbucks.notification.common.exception.NotificationException;

public enum NotificationType {

    SUBSCRIBE("알림 구독"),
    ORDER_ACCEPTED("주문 접수가 완료 되었습니다. 주문번호 %s"),
    ORDER_REJECTED("주문이 거절 되었습니다. 사유 : %s"),
    ORDER_SET("주문번호 : %s 준비 완료되었습니다."),

    ;

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getDescription(Object ... args) {
        return description.formatted(args);
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
