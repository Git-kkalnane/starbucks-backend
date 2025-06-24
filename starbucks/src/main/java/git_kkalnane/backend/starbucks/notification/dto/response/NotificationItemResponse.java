package git_kkalnane.backend.starbucks.notification.dto.response;


import lombok.Getter;

@Getter
public class NotificationItemResponse<T> {
    private NotificationResponse notification;
    private T item;

    private NotificationItemResponse(NotificationResponse notificationResponse, T item) {
        this.notification = notificationResponse;
        this.item = item;
    }

    public static <T> NotificationItemResponse<T> of(NotificationResponse notificationResponse, T item) {
        return new NotificationItemResponse<>(notificationResponse, item);
    }
}
