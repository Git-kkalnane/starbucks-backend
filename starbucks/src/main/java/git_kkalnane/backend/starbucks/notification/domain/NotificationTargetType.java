package git_kkalnane.backend.starbucks.notification.domain;

import git_kkalnane.backend.starbucks.notification.common.exception.NotificationErrorCode;
import git_kkalnane.backend.starbucks.notification.common.exception.NotificationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTargetType {

    CUSTOMER("고객"),
    STORE("지점")
    ;

    private final String type;

    public static NotificationTargetType findByName(String givenName){
        try{
            return NotificationTargetType.valueOf(givenName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotificationException(
                NotificationErrorCode.INVALID_NOTIFICATION_TYPE, givenName
            );
        }

    }
}
