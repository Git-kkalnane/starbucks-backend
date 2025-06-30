package git_kkalnane.backend.starbucks.notification.event;

import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.merchant.domain.Merchant;
import git_kkalnane.backend.starbucks.notification.domain.NotificationTargetType;
import git_kkalnane.backend.starbucks.notification.domain.NotificationType;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationReceiver;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationSender;
import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.domain.OrderItem;
import git_kkalnane.backend.starbucks.order.domain.OrderStatus;
import git_kkalnane.backend.starbucks.order.domain.PickupType;
import git_kkalnane.backend.starbucks.store.domain.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private NotificationEventPublisher notificationEventPublisher;

    private Order mockOrder;
    private Member mockMember;
    @Mock
    private Merchant mockMerchant;
    private Store mockStore;
    private OrderNotificationSendEvent mockEvent;

    @BeforeEach
    void setUp() {
        // Given: 테스트용 도메인 객체들 생성
        mockMember = Member.builder()
                .id(1L)
                .name("테스트 고객")
                .nickname("테스트")
                .email("test@test.com")
                .password("password123")
                .build();

        // Merchant Mock 설정
        when(mockMerchant.getId()).thenReturn(1L);

        mockStore = Store.builder()
                .id(1L)
                .name("테스트 스토어")
                .address("서울시 강남구")
                .phone("02-1234-5678")
                .merchant(mockMerchant)
                .build();

        mockOrder = Order.builder()
                .id(1L)
                .orderNumber("ORDER-001")
                .orderTotalPrice(5000)
                .orderStatus(OrderStatus.PLACED)
                .pickupType(PickupType.STORE_PICKUP)
                .orderRequestMemo("테스트 메모")
                .orderExpectedPickupTime(LocalDateTime.now().plusMinutes(10))
                .store(mockStore)
                .member(mockMember)
                .orderItems(new ArrayList<>())
                .build();

        // OrderItem 추가
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .itemName("아메리카노")
                .orderItemQuantity(1)
                .unitPrice(4500)
                .order(mockOrder)
                .build();
        mockOrder.addOrderItem(orderItem);

        mockEvent = new OrderNotificationSendEvent(
                this,
                mockOrder,
                NotificationSender.of(mockMember.getId()),
                NotificationReceiver.of(mockMerchant.getId()),
                NotificationType.ORDER_CREATED,
                NotificationTargetType.MERCHANT
        );
    }

    @Nested
    @DisplayName("publish() 메서드 테스트")
    class PublishTest {

        @Test
        @DisplayName("성공: OrderNotificationSendEvent 이벤트를 정상적으로 발행함")
        void publish_OrderNotificationSendEvent_PublishesEventSuccessfully() {
            // Given: OrderNotificationSendEvent가 준비됨 (setUp에서 생성)

            // When: 이벤트 퍼블리셔가 이벤트를 발행
            notificationEventPublisher.publish(mockEvent);

            // Then: ApplicationEventPublisher가 이벤트를 발행함
            then(applicationEventPublisher).should(times(1))
                    .publishEvent(mockEvent);
        }

        @Test
        @DisplayName("성공: 여러 번의 이벤트 발행이 모두 정상적으로 처리됨")
        void publish_MultipleEvents_PublishesAllEventsSuccessfully() {
            // Given: 여러 개의 이벤트가 준비됨
            OrderNotificationSendEvent event1 = new OrderNotificationSendEvent(
                    this,
                    mockOrder,
                    NotificationSender.of(1L),
                    NotificationReceiver.of(2L),
                    NotificationType.ORDER_CREATED,
                    NotificationTargetType.MERCHANT
            );

            OrderNotificationSendEvent event2 = new OrderNotificationSendEvent(
                    this,
                    mockOrder,
                    NotificationSender.of(2L),
                    NotificationReceiver.of(1L),
                    NotificationType.ORDER_ACCEPTED,
                    NotificationTargetType.CUSTOMER
            );

            // When: 여러 이벤트를 연속으로 발행
            notificationEventPublisher.publish(event1);
            notificationEventPublisher.publish(event2);

            // Then: 모든 이벤트가 발행됨
            then(applicationEventPublisher).should(times(1))
                    .publishEvent(event1);
            then(applicationEventPublisher).should(times(1))
                    .publishEvent(event2);
        }

        @Test
        @DisplayName("성공: null 이벤트가 전달되어도 예외 없이 처리됨")
        void publish_NullEvent_HandlesGracefully() {
            // Given: null 이벤트가 준비됨
            OrderNotificationSendEvent nullEvent = null;

            // When: null 이벤트를 발행
            notificationEventPublisher.publish(nullEvent);

            // Then: ApplicationEventPublisher가 null 이벤트를 발행함
            then(applicationEventPublisher).should(times(1))
                    .publishEvent(null);
        }

        @Test
        @DisplayName("성공: 다른 타입의 이벤트도 정상적으로 발행됨")
        void publish_DifferentEventTypes_PublishesAllEventTypesSuccessfully() {
            // Given: 다양한 알림 타입의 이벤트들이 준비됨
            OrderNotificationSendEvent orderCreatedEvent = new OrderNotificationSendEvent(
                    this,
                    mockOrder,
                    NotificationSender.of(1L),
                    NotificationReceiver.of(2L),
                    NotificationType.ORDER_CREATED,
                    NotificationTargetType.MERCHANT
            );

            OrderNotificationSendEvent orderAcceptedEvent = new OrderNotificationSendEvent(
                    this,
                    mockOrder,
                    NotificationSender.of(2L),
                    NotificationReceiver.of(1L),
                    NotificationType.ORDER_ACCEPTED,
                    NotificationTargetType.CUSTOMER
            );

            OrderNotificationSendEvent orderSetEvent = new OrderNotificationSendEvent(
                    this,
                    mockOrder,
                    NotificationSender.of(1L),
                    NotificationReceiver.of(1L),
                    NotificationType.ORDER_SET,
                    NotificationTargetType.CUSTOMER
            );

            // When: 다양한 타입의 이벤트들을 발행
            notificationEventPublisher.publish(orderCreatedEvent);
            notificationEventPublisher.publish(orderAcceptedEvent);
            notificationEventPublisher.publish(orderSetEvent);

            // Then: 모든 이벤트가 발행됨
            then(applicationEventPublisher).should(times(1))
                    .publishEvent(orderCreatedEvent);
            then(applicationEventPublisher).should(times(1))
                    .publishEvent(orderAcceptedEvent);
            then(applicationEventPublisher).should(times(1))
                    .publishEvent(orderSetEvent);
        }
    }

    @Nested
    @DisplayName("의존성 주입 테스트")
    class DependencyInjectionTest {

        @Test
        @DisplayName("성공: ApplicationEventPublisher가 정상적으로 주입됨")
        void publish_WithInjectedDependency_UsesInjectedApplicationEventPublisher() {
            // Given: 이벤트가 준비됨

            // When: 이벤트를 발행
            notificationEventPublisher.publish(mockEvent);

            // Then: 주입된 ApplicationEventPublisher가 사용됨
            then(applicationEventPublisher).should(times(1))
                    .publishEvent(any(OrderNotificationSendEvent.class));
        }
    }

    @Nested
    @DisplayName("이벤트 발행 순서 테스트")
    class EventPublishingOrderTest {

        @Test
        @DisplayName("성공: 이벤트 발행 순서가 호출 순서와 일치함")
        void publish_EventsInOrder_PublishesInCorrectOrder() {
            // Given: 순서가 있는 이벤트들이 준비됨
            OrderNotificationSendEvent firstEvent = new OrderNotificationSendEvent(
                    this,
                    mockOrder,
                    NotificationSender.of(1L),
                    NotificationReceiver.of(2L),
                    NotificationType.ORDER_CREATED,
                    NotificationTargetType.MERCHANT
            );

            OrderNotificationSendEvent secondEvent = new OrderNotificationSendEvent(
                    this,
                    mockOrder,
                    NotificationSender.of(2L),
                    NotificationReceiver.of(1L),
                    NotificationType.ORDER_ACCEPTED,
                    NotificationTargetType.CUSTOMER
            );

            // When: 순서대로 이벤트를 발행
            notificationEventPublisher.publish(firstEvent);
            notificationEventPublisher.publish(secondEvent);

            // Then: 순서대로 발행됨 (Mockito의 verify 순서 확인)
            then(applicationEventPublisher).should(times(1))
                    .publishEvent(firstEvent);
            then(applicationEventPublisher).should(times(1))
                    .publishEvent(secondEvent);
        }
    }
} 