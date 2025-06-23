package git_kkalnane.backend.starbucks.notification.domain.vo;

import git_kkalnane.backend.starbucks.notification.common.exception.NotificationErrorCode;
import git_kkalnane.backend.starbucks.notification.common.exception.NotificationException;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class NotificationSender {

    private final Long id;

    protected NotificationSender() {
        this.id = null;
    }

    private NotificationSender(Long id) {
        this.id = id;
    }

    public Long value() {
        return id;
    }

    public static NotificationSender of(Long id) {
        validate(id);
        return new NotificationSender(id);
    }

    private static void validate(Long id) {
        if (id == null || id <= 0) {
            throw new NotificationException(NotificationErrorCode.SENDER_ID_IS_NOT_EMPTY);
        }
    }
}
