package git_kkalnane.backend.starbucks.notification.event;


import git_kkalnane.backend.starbucks.notification.domain.NotificationType;
import git_kkalnane.backend.starbucks.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 알림 이벤트를 처리하는 리스너 클래스
 * NotificationFlippedSendEvent 이벤트를 비동기로 처리하여 알림을 전송합니다.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener(OrderNotificationSendEvent.class)
    @Async // 이벤트 발생 -> 알림 전송 로직을 비동기로 실행, TODO: 비동기 설정 시 쓰레드 풀 컨트롤
    public void handle(OrderNotificationSendEvent event) {

        // 멤버에게 주문 접수 알림 전송
        notificationService.sendNotificationWithOrder(
                event.getItem(),
                NotificationType.ORDER_ACCEPTED.getTitle(),
                NotificationType.ORDER_ACCEPTED.getMessage(
                        event.getItem().getMemberName(),
                        String.valueOf(event.getItem().getOrderNumber())
                ),
                event.getReceiver().value(),
                event.getSender().value(),
                event.getNotificationType(),
                event.getNotificationTargetType()
        );

        // 지점에게 주문 발생 알림 전송
        notificationService.sendNotificationWithOrder(
                event.getItem(),
                event.getTitle(),
                event.getMessage(),
                event.getSender().value(),
                event.getReceiver().value(),
                event.getNotificationType(),
                event.getNotificationTargetType()
        );
    }
}
