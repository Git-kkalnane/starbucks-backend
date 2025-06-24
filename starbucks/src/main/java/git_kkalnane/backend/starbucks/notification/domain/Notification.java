package git_kkalnane.backend.starbucks.notification.domain;


import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationEvent;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationReceiver;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationSender;
import git_kkalnane.backend.starbucks.notification.dto.response.NotificationItemResponse;
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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)


    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "event_id", nullable = false, unique = true))
    private NotificationEvent event;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "sender_id", nullable = false))
    private NotificationSender sender;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "receiver_id", nullable = false))
    private NotificationReceiver receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationTargetType notificationTargetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;


    public NotificationResponse toDto() {
        return NotificationResponse.builder()
                .eventId(event.value())
                .message(message)
                .title(title)
                .senderId(sender.value())
                .receiverId(receiver.value())
                .notificationTargetType(notificationTargetType.name())
                .notificationType(notificationType.name())
                .build();
    }

    public <T> NotificationItemResponse<T> toDto(T item) {
        return NotificationItemResponse.of(toDto(), item);
    }
}
