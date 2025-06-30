package git_kkalnane.backend.starbucks.notification.event;

import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.merchant.domain.Merchant;
import git_kkalnane.backend.starbucks.notification.domain.NotificationTargetType;
import git_kkalnane.backend.starbucks.notification.domain.NotificationType;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationReceiver;
import git_kkalnane.backend.starbucks.notification.domain.vo.NotificationSender;
import git_kkalnane.backend.starbucks.notification.dto.response.OrderNotificationSendResponse;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderNotificationSendEventTest {
    private Order mockOrder;
    private Member mockMember;
    @Mock
    private Merchant mockMerchant;
    private Store mockStore;
    private OrderNotificationSendEvent event;

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

        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .itemName("아메리카노")
                .orderItemQuantity(1)
                .unitPrice(4500)
                .order(mockOrder)
                .build();
        mockOrder.addOrderItem(orderItem);

        event = new OrderNotificationSendEvent(
                this,
                mockOrder,
                NotificationSender.of(mockMember.getId()),
                NotificationReceiver.of(mockMerchant.getId()),
                NotificationType.ORDER_CREATED,
                NotificationTargetType.MERCHANT
        );
    }

    @Nested
    @DisplayName("OrderNotificationSendResponse 변환 테스트")
    class OrderNotificationSendResponseTest {
        @Test
        @DisplayName("OrderNotificationSendEvent_Item_변환_정상")
        void item_ConvertedToOrderNotificationSendResponseCorrectly() {
            // When
            OrderNotificationSendResponse item = event.getItem();
            // Then
            assertThat(item).isNotNull();
            assertThat(item.getOrderId()).isEqualTo(mockOrder.getId());
            assertThat(item.getOrderNumber()).isEqualTo(mockOrder.getOrderNumber());
            assertThat(item.getPickupType()).isEqualTo(mockOrder.getPickupType());
            assertThat(item.getOrderRequestMemo()).isEqualTo(mockOrder.getOrderRequestMemo());
            assertThat(item.getOrderExpectedPickupTime()).isEqualTo(mockOrder.getOrderExpectedPickupTime());
            assertThat(item.getStoreId()).isEqualTo(mockOrder.getStore().getId());
            assertThat(item.getMemberId()).isEqualTo(mockOrder.getMember().getId());
            assertThat(item.getMemberName()).isEqualTo(mockOrder.getMember().getName());
        }
    }

    @Nested
    @DisplayName("알림 타입별 생성 테스트")
    class NotificationTypeTest {
        @Test
        @DisplayName("OrderNotificationSendEvent_ORDER_ACCEPTED_생성_정상")
        void constructor_OrderAcceptedType_CreatesEventSuccessfully() {
            // When
            OrderNotificationSendEvent acceptedEvent = new OrderNotificationSendEvent(
                    this,
                    mockOrder,
                    NotificationSender.of(mockMember.getId()),
                    NotificationReceiver.of(mockMerchant.getId()),
                    NotificationType.ORDER_ACCEPTED,
                    NotificationTargetType.CUSTOMER
            );
            // Then
            assertThat(acceptedEvent).isNotNull();
            assertThat(acceptedEvent.getNotificationType()).isEqualTo(NotificationType.ORDER_ACCEPTED);
            assertThat(acceptedEvent.getTitle()).isEqualTo(NotificationType.ORDER_ACCEPTED.getTitle());
            assertThat(acceptedEvent.getMessage()).isEqualTo(NotificationType.ORDER_ACCEPTED.getMessage());
        }
        @Test
        @DisplayName("OrderNotificationSendEvent_ORDER_SET_생성_정상")
        void constructor_OrderSetType_CreatesEventSuccessfully() {
            // When
            OrderNotificationSendEvent setEvent = new OrderNotificationSendEvent(
                    this,
                    mockOrder,
                    NotificationSender.of(mockMember.getId()),
                    NotificationReceiver.of(mockMerchant.getId()),
                    NotificationType.ORDER_SET,
                    NotificationTargetType.CUSTOMER
            );
            // Then
            assertThat(setEvent).isNotNull();
            assertThat(setEvent.getNotificationType()).isEqualTo(NotificationType.ORDER_SET);
            assertThat(setEvent.getTitle()).isEqualTo(NotificationType.ORDER_SET.getTitle());
            assertThat(setEvent.getMessage()).isEqualTo(NotificationType.ORDER_SET.getMessage());
        }
    }

    @Nested
    @DisplayName("주문 아이템 처리 테스트")
    class OrderItemProcessingTest {
        @Test
        @DisplayName("OrderNotificationSendEvent_음료아이템_포함_정상처리")
        void orderWithBeverageItem_ProcessesCorrectly() {
            // Given
            Order orderWithBeverage = Order.builder()
                    .id(2L)
                    .orderNumber("ORDER-002")
                    .orderTotalPrice(4500)
                    .orderStatus(OrderStatus.PLACED)
                    .pickupType(PickupType.STORE_PICKUP)
                    .orderRequestMemo("음료 주문")
                    .orderExpectedPickupTime(LocalDateTime.now().plusMinutes(10))
                    .store(mockStore)
                    .member(mockMember)
                    .orderItems(new ArrayList<>())
                    .build();
            OrderItem beverageItem = OrderItem.builder()
                    .id(2L)
                    .itemName("카페라떼")
                    .orderItemQuantity(1)
                    .unitPrice(5000)
                    .order(orderWithBeverage)
                    .build();
            orderWithBeverage.addOrderItem(beverageItem);
            // When
            OrderNotificationSendEvent beverageEvent = new OrderNotificationSendEvent(
                    this,
                    orderWithBeverage,
                    NotificationSender.of(mockMember.getId()),
                    NotificationReceiver.of(mockMerchant.getId()),
                    NotificationType.ORDER_CREATED,
                    NotificationTargetType.MERCHANT
            );
            // Then
            assertThat(beverageEvent).isNotNull();
            assertThat(beverageEvent.getItem()).isNotNull();
            assertThat(beverageEvent.getItem().getOrderId()).isEqualTo(orderWithBeverage.getId());
        }
        @Test
        @DisplayName("OrderNotificationSendEvent_디저트아이템_포함_정상처리")
        void orderWithDessertItem_ProcessesCorrectly() {
            // Given
            Order orderWithDessert = Order.builder()
                    .id(3L)
                    .orderNumber("ORDER-003")
                    .orderTotalPrice(3500)
                    .orderStatus(OrderStatus.PLACED)
                    .pickupType(PickupType.STORE_PICKUP)
                    .orderRequestMemo("디저트 주문")
                    .orderExpectedPickupTime(LocalDateTime.now().plusMinutes(10))
                    .store(mockStore)
                    .member(mockMember)
                    .orderItems(new ArrayList<>())
                    .build();
            OrderItem dessertItem = OrderItem.builder()
                    .id(3L)
                    .itemName("티라미수")
                    .orderItemQuantity(1)
                    .unitPrice(3500)
                    .order(orderWithDessert)
                    .build();
            orderWithDessert.addOrderItem(dessertItem);
            // When
            OrderNotificationSendEvent dessertEvent = new OrderNotificationSendEvent(
                    this,
                    orderWithDessert,
                    NotificationSender.of(mockMember.getId()),
                    NotificationReceiver.of(mockMerchant.getId()),
                    NotificationType.ORDER_CREATED,
                    NotificationTargetType.MERCHANT
            );
            // Then
            assertThat(dessertEvent).isNotNull();
            assertThat(dessertEvent.getItem()).isNotNull();
            assertThat(dessertEvent.getItem().getOrderId()).isEqualTo(orderWithDessert.getId());
        }
        @Test
        @DisplayName("OrderNotificationSendEvent_빈주문아이템_정상처리")
        void orderWithEmptyItems_CreatesEventSuccessfully() {
            // Given
            Order emptyOrder = Order.builder()
                    .id(4L)
                    .orderNumber("ORDER-004")
                    .orderTotalPrice(0)
                    .orderStatus(OrderStatus.PLACED)
                    .pickupType(PickupType.STORE_PICKUP)
                    .orderRequestMemo("빈 주문")
                    .orderExpectedPickupTime(LocalDateTime.now().plusMinutes(10))
                    .store(mockStore)
                    .member(mockMember)
                    .orderItems(new ArrayList<>())
                    .build();
            // When
            OrderNotificationSendEvent emptyEvent = new OrderNotificationSendEvent(
                    this,
                    emptyOrder,
                    NotificationSender.of(mockMember.getId()),
                    NotificationReceiver.of(mockMerchant.getId()),
                    NotificationType.ORDER_CREATED,
                    NotificationTargetType.MERCHANT
            );
            // Then
            assertThat(emptyEvent).isNotNull();
            assertThat(emptyEvent.getItem()).isNotNull();
            assertThat(emptyEvent.getItem().getOrderId()).isEqualTo(emptyOrder.getId());
            assertThat(emptyEvent.getItem().getBeverageItems()).isEmpty();
            assertThat(emptyEvent.getItem().getDessertItems()).isEmpty();
        }
    }
} 