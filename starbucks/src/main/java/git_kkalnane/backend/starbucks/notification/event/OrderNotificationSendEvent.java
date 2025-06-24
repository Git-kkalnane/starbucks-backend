package git_kkalnane.backend.starbucks.notification.event;

import git_kkalnane.backend.starbucks.notification.domain.NotificationTargetType;
import git_kkalnane.backend.starbucks.notification.domain.NotificationType;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationReceiver;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationSender;
import git_kkalnane.backend.starbucks.order.domain.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderNotificationSendEvent extends ApplicationEvent {

    private Order order;
    private String title;
    private String message;
    private NotificationSender sender;
    private NotificationReceiver receiver;
    private NotificationTargetType notificationTargetType;
    private NotificationType notificationType;

    public OrderNotificationSendEvent(Object object,
                                      Order order,
                                      NotificationSender sender,
                                      NotificationReceiver receiver,
                                      NotificationType notificationType, NotificationTargetType notificationTargetType) {
        super(object);
        this.title = notificationType.getTitle();
        this.message = notificationType.getMessage();
        this.order = order;
        this.sender = sender;
        this.receiver = receiver;
        this.notificationType = notificationType;
        this.notificationTargetType = notificationTargetType;
    }
}
