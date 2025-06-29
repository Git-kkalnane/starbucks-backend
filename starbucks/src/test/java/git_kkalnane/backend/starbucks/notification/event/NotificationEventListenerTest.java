package git_kkalnane.backend.starbucks.notification.event;

import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.merchant.domain.Merchant;
import git_kkalnane.backend.starbucks.notification.domain.NotificationTargetType;
import git_kkalnane.backend.starbucks.notification.domain.NotificationType;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationReceiver;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationSender;
import git_kkalnane.backend.starbucks.notification.dto.response.OrderNotificationSendResponse;
import git_kkalnane.backend.starbucks.notification.service.NotificationService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener notificationEventListener;

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
    @DisplayName("handle() 메서드 테스트")
    class HandleTest {

        @Test
        @DisplayName("성공: 주문 알림 이벤트 처리 시 고객과 매장 모두에게 알림 전송")
        void handle_OrderNotificationEvent_SendsNotificationsToCustomerAndMerchant() {
            // Given: 이벤트가 준비됨 (setUp에서 생성)

            // When: 이벤트 리스너가 이벤트를 처리
            notificationEventListener.handle(mockEvent);

            // Then: 총 2번의 알림 전송이 발생함 (고객 1번, 매장 1번)
            then(notificationService).should(times(2))
                    .sendNotificationWithOrder(
                            any(OrderNotificationSendResponse.class),
                            anyString(),
                            anyString(),
                            anyLong(),
                            anyLong(),
                            any(NotificationType.class),
                            any(NotificationTargetType.class)
                    );
        }

        @Test
        @DisplayName("성공: 고객에게 전송되는 알림 메시지가 올바른 형식으로 구성됨")
        void handle_OrderNotificationEvent_CustomerNotificationHasCorrectMessageFormat() {
            // Given: 이벤트가 준비됨

            // When: 이벤트 리스너가 이벤트를 처리
            notificationEventListener.handle(mockEvent);

            // Then: 고객 알림 메시지가 올바른 형식으로 전송됨
            then(notificationService).should(times(1))
                    .sendNotificationWithOrder(
                            any(OrderNotificationSendResponse.class),
                            eq("주문 접수 완료"),
                            eq(String.format("%s님의 주문을 준비 중입니다. (%s)", 
                                    mockMember.getName(), 
                                    mockOrder.getOrderNumber())),
                            anyLong(),
                            anyLong(),
                            eq(NotificationType.ORDER_ACCEPTED),
                            eq(NotificationTargetType.CUSTOMER)
                    );
        }

        @Test
        @DisplayName("성공: 매장에게 전송되는 알림이 이벤트의 원본 정보를 사용함")
        void handle_OrderNotificationEvent_MerchantNotificationUsesOriginalEventData() {
            // Given: 이벤트가 준비됨

            // When: 이벤트 리스너가 이벤트를 처리
            notificationEventListener.handle(mockEvent);

            // Then: 매장 알림이 이벤트의 원본 정보를 사용함
            then(notificationService).should(times(1))
                    .sendNotificationWithOrder(
                            any(OrderNotificationSendResponse.class),
                            eq(mockEvent.getTitle()),
                            eq(mockEvent.getMessage()),
                            eq(mockEvent.getSender().value()),
                            eq(mockEvent.getReceiver().value()),
                            eq(mockEvent.getNotificationType()),
                            eq(mockEvent.getNotificationTargetType())
                    );
        }

        @Test
        @DisplayName("성공: 두 개의 알림이 모두 OrderNotificationSendResponse와 함께 전송됨")
        void handle_OrderNotificationEvent_BothNotificationsIncludeOrderResponseData() {
            // Given: 이벤트가 준비됨

            // When: 이벤트 리스너가 이벤트를 처리
            notificationEventListener.handle(mockEvent);

            // Then: 두 번의 sendNotificationWithOrder 호출이 모두 OrderNotificationSendResponse와 함께 이루어짐
            then(notificationService).should(times(2))
                    .sendNotificationWithOrder(
                            any(OrderNotificationSendResponse.class),
                            anyString(),
                            anyString(),
                            anyLong(),
                            anyLong(),
                            any(NotificationType.class),
                            any(NotificationTargetType.class)
                    );
        }

        @Test
        @DisplayName("성공: 고객 알림과 매장 알림이 서로 다른 파라미터로 전송됨")
        void handle_OrderNotificationEvent_CustomerAndMerchantNotificationsHaveDifferentParameters() {
            // Given: 이벤트가 준비됨

            // When: 이벤트 리스너가 이벤트를 처리
            notificationEventListener.handle(mockEvent);

            // Then: 고객 알림 (첫 번째 호출)
            then(notificationService).should(times(1))
                    .sendNotificationWithOrder(
                            any(OrderNotificationSendResponse.class),
                            eq("주문 접수 완료"),
                            eq(String.format("%s님의 주문을 준비 중입니다. (%s)", 
                                    mockMember.getName(), 
                                    mockOrder.getOrderNumber())),
                            eq(mockEvent.getReceiver().value()), // merchantId
                            eq(mockEvent.getSender().value()),   // memberId
                            eq(NotificationType.ORDER_ACCEPTED),
                            eq(NotificationTargetType.CUSTOMER)
                    );

            // Then: 매장 알림 (두 번째 호출)
            then(notificationService).should(times(1))
                    .sendNotificationWithOrder(
                            any(OrderNotificationSendResponse.class),
                            eq(mockEvent.getTitle()),
                            eq(mockEvent.getMessage()),
                            eq(mockEvent.getSender().value()),   // memberId
                            eq(mockEvent.getReceiver().value()), // merchantId
                            eq(mockEvent.getNotificationType()),
                            eq(mockEvent.getNotificationTargetType())
                    );
        }
    }

    @Nested
    @DisplayName("비동기 처리 테스트")
    class AsyncProcessingTest {

        @Test
        @DisplayName("성공: @Async 어노테이션으로 인해 비동기로 처리됨")
        void handle_OrderNotificationEvent_ProcessedAsynchronously() {
            // Given: 이벤트가 준비됨

            // When: 이벤트 리스너가 이벤트를 처리 (비동기로 실행됨)
            notificationEventListener.handle(mockEvent);

            // Then: 메서드가 정상적으로 호출됨 (비동기 실행이므로 즉시 반환)
            // 실제 비동기 테스트는 별도의 통합 테스트에서 수행
            then(notificationService).should(times(2))
                    .sendNotificationWithOrder(
                            any(OrderNotificationSendResponse.class),
                            anyString(),
                            anyString(),
                            anyLong(),
                            anyLong(),
                            any(NotificationType.class),
                            any(NotificationTargetType.class)
                    );
        }
    }
} 