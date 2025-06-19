package git_kkalnane.backend.starbucks.notification.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
    private String eventId;
    private String emitterId;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String message;
    private String notificationType;
    private String notificationTargetType;
}
