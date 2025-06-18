package git_kkalnane.backend.starbucks.notification.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
    private String eventId;
    private String emitterId;
    private Long receivingMemberId;
    private String message;
    private String notificationType;
}
