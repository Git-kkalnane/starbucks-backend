package git_kkalnane.backend.starbucks.notification.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationSendRequest {
    private String title;
    private String message;
    private Long senderId;
    private Long receiverId;
    private String notificationType;
    private String notificationTargetType;
}
