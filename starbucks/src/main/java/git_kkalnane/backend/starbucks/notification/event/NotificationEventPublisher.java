package git_kkalnane.backend.starbucks.notification.event;


import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 알림 이벤트를 발행하는 퍼블리셔 클래스
 * Spring의 ApplicationEventPublisher를 사용하여 알림 이벤트를 발행합니다.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    /**
     * NotificationFlippedSendEvent를 발행하는 메서드
     *
     * @param event 발행할 알림 이벤트
     */
    public void publish(OrderNotificationSendEvent event) {
        eventPublisher.publishEvent(event);
    }
}
