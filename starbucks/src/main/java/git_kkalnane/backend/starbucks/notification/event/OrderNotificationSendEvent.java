package git_kkalnane.backend.starbucks.notification.event;

import git_kkalnane.backend.starbucks.notification.domain.NotificationTargetType;
import git_kkalnane.backend.starbucks.notification.domain.NotificationType;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationReceiver;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationSender;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderNotificationSendEvent extends ApplicationEvent {

    private Long orderId;
    private String title;
    private String message;
    private NotificationSender sender;
    private NotificationReceiver receiver;
    private NotificationTargetType notificationTargetType;
    private NotificationType notificationType;

    public OrderNotificationSendEvent(Object object,
                                      Long orderId,
                                      NotificationSender sender,
                                      NotificationReceiver receiver,
                                      NotificationType notificationType, NotificationTargetType notificationTargetType) {
        super(object);
        this.title = notificationType.getTitle();
        this.message = notificationType.getMessage();
        this.orderId = orderId;
        this.sender = sender;
        this.receiver = receiver;
        this.notificationType = notificationType;
        this.notificationTargetType = notificationTargetType;
    }
}
