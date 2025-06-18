package git_kkalnane.backend.starbucks.notification.domain;


import git_kkalnane.backend.starbucks.global.entity.BaseTimeEntity;
import git_kkalnane.backend.starbucks.notification.dto.response.NotificationResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String emitterId; // 알림을 발생시킨 Member의 ID
    private String eventId;
    private Long receivingMemberId;
    private String content;
    private boolean isRead;
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;


    public NotificationResponse toDto(){
        return NotificationResponse.builder()
                .emitterId(emitterId)
                .eventId(eventId)
                .receivingMemberId(receivingMemberId)
                .notificationType(notificationType.name())
                .build();
    }

}
