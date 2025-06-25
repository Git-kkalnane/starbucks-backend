package git_kkalnane.backend.starbucks.notification.aop;


import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.merchant.domain.Merchant;
import git_kkalnane.backend.starbucks.notification.domain.NotificationTargetType;
import git_kkalnane.backend.starbucks.notification.domain.NotificationType;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationReceiver;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationSender;
import git_kkalnane.backend.starbucks.notification.event.NotificationEventPublisher;
import git_kkalnane.backend.starbucks.notification.event.OrderNotificationSendEvent;
import git_kkalnane.backend.starbucks.order.domain.Order;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 알림 서비스에 대한 AOP 처리를 담당하는 어스펙트 클래스
 * NotificationService의 sendNotification 메서드 실행 후 이벤트를 발행하고 예외를 로깅합니다.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class NotificationAspect {

    private final NotificationEventPublisher notificationEventPublisher;

    /**
     * NotificationService의 sendNotification 메서드에 대한 포인트컷 정의
     * NotificationSendRequest를 파라미터로 받는 메서드를 대상으로 합니다.
     */
    @Pointcut("execution(* git_kkalnane.backend.starbucks.notification.service" +
            ".NotificationService" +
            ".sendNotification(git_kkalnane.backend.starbucks.notification.dto.request.OrderNotificationSendRequest))")
    public void notificationServiceMethods() {}

    /**
     * 주문 생성 실행 후 지점에게 알림을 보내는 어드바이스
     * OrderNotificationSendEvent를 발행하여 지점에게 알림을 전송합니다.
     *
     * @param order  주문 생성 후 반환된 주문 엔티티
     */
    @AfterReturning(pointcut = "notificationServiceMethods()", returning = "order")
    public void orderAfter(Order order) {
        Merchant merchant = order.getStore().getMerchant();
        Member member = order.getMember();

        notificationEventPublisher.publish(new OrderNotificationSendEvent(
                this,
                order,
                NotificationSender.of(member.getId()),
                NotificationReceiver.of(merchant.getId()),
                NotificationType.ORDER_CREATED,
                NotificationTargetType.MERCHANT
        ));
    }
}
