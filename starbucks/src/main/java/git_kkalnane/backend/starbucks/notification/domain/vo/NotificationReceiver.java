package git_kkalnane.backend.starbucks.notification.domain.vo;


import git_kkalnane.backend.starbucks.notification.common.exception.NotificationErrorCode;
import git_kkalnane.backend.starbucks.notification.common.exception.NotificationException;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class NotificationReceiver {

    private final Long id;

    protected NotificationReceiver() {
        this.id = null;
    }

    private NotificationReceiver(Long id) {
        this.id = id;
    }

    public Long value() {
        return id;
    }

    public static NotificationReceiver of(Long id) {
        validate(id);
        return new NotificationReceiver(id);
    }

    private static void validate(Long id) {
        if (id == null || id <= 0) {
            throw new NotificationException(NotificationErrorCode.RECEIVER_ID_IS_NOT_EMPTY);
        }
    }
}
