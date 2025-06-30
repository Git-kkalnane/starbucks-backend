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
@Table(name = "order_notifications")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder
public class OrderNotification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long notificationId;

    @Column(nullable = false)
    private Long orderId;
}
