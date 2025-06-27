package git_kkalnane.backend.starbucks.order;

import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageSizeOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageTemperatureOption;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import git_kkalnane.backend.starbucks.item.repository.BeverageItemRepository;
import git_kkalnane.backend.starbucks.item.repository.DessertItemRepository;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import git_kkalnane.backend.starbucks.order.common.exception.OrderErrorCode;
import git_kkalnane.backend.starbucks.order.common.exception.OrderException;
import git_kkalnane.backend.starbucks.order.domain.*;
import git_kkalnane.backend.starbucks.order.dto.request.CreateOrderDTO;
import git_kkalnane.backend.starbucks.order.dto.request.ItemOptionRequest;
import git_kkalnane.backend.starbucks.order.dto.request.OrderItemRequest;
import git_kkalnane.backend.starbucks.order.dto.response.CurrentOrderResponse;
import git_kkalnane.backend.starbucks.order.dto.response.OrderDetailResponse;
import git_kkalnane.backend.starbucks.order.dto.response.OrderListResponse;
import git_kkalnane.backend.starbucks.order.repository.OrderDailyCounterRepository;
import git_kkalnane.backend.starbucks.order.repository.OrderItemRepository;
import git_kkalnane.backend.starbucks.order.repository.OrderRepository;
import git_kkalnane.backend.starbucks.order.service.OrderService;
import git_kkalnane.backend.starbucks.payment.service.PaymentService;
import git_kkalnane.backend.starbucks.store.domain.Store;
import git_kkalnane.backend.starbucks.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderDailyCounterRepository orderDailyCounterRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private BeverageItemRepository beverageItemRepository;
    @Mock private DessertItemRepository dessertItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private PaymentService paymentService;


    @InjectMocks private OrderService orderService;

    private Store mockStore;
    private Member mockMember;
    private BeverageItem mockBeverage;
    private DessertItem mockDessert;

    @BeforeEach
    void 정보_세팅() {
        mockMember = Member.builder().id(1L).name("장원영").build();
        mockStore = Store.builder().id(1L).name("스타벅스 강남점").address("서울시 강남구 강남대로 123").build();
        mockBeverage = BeverageItem.builder().id(101L).beverageItemNameKo("아이스 아메리카노").price(4500).isCoffee(true).build();
        mockDessert = DessertItem.builder().id(201L).dessertItemNameKo("치즈케이크").price(5500).build();
    }

    @Test
    @DisplayName("정상적인 주문 생성 테스트")
    void createOrder_Success() {
        // Given
        int requestedQuantity = 2;
        int beveragePrice = mockBeverage.getPrice();

        List<ItemOptionRequest> options = new ArrayList<>();

        List<OrderItemRequest> orderItems = List.of(
                new OrderItemRequest(
                        mockBeverage.getId(),
                        ItemType.COFFEE,
                        BeverageSizeOption.TALL,
                        BeverageTemperatureOption.HOT,
                        options,
                        beveragePrice,
                        beveragePrice * requestedQuantity,
                        requestedQuantity
                )
        );
        CreateOrderDTO request = new CreateOrderDTO(
                mockStore.getId(),
                PickupType.STORE_PICKUP,
                orderItems.get(0).totalPrice(),
                OrderStatus.PLACED,
                LocalDateTime.now().plusMinutes(10),
                orderItems
        );

        given(storeRepository.findById(mockStore.getId())).willReturn(Optional.of(mockStore));
        given(memberRepository.findById(mockMember.getId())).willReturn(Optional.of(mockMember));
        given(beverageItemRepository.findById(mockBeverage.getId())).willReturn(Optional.of(mockBeverage));
        OrderDailyCounterId counterId = new OrderDailyCounterId(LocalDate.now(), mockStore.getId());
        given(orderDailyCounterRepository.findById(any(OrderDailyCounterId.class)))
                .willReturn(Optional.of(new OrderDailyCounter(counterId, 0)));
        given(orderDailyCounterRepository.save(any(OrderDailyCounter.class)))
                .willAnswer(invocation -> {
                    OrderDailyCounter counter = invocation.getArgument(0);
                    return counter; // 전달된 객체를 그대로 반환
                });
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
        Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                order = Order.builder()
                        .id(1L)
                        .orderNumber(order.getOrderNumber())
                        .store(order.getStore())
                        .member(order.getMember())
                        .orderExpectedPickupTime(order.getOrderExpectedPickupTime())
                        .orderStatus(order.getOrderStatus())
                        .orderItems(order.getOrderItems())
                        .orderTotalPrice(order.getOrderTotalPrice())
                        .pickupType(order.getPickupType())
                        .build();
            }
            for (OrderItem item : order.getOrderItems()) {
                if (item.getOrder() == null) {
                    item.setOrder(order);
                }
            }
            return order;
        });

        // When
        Order order = orderService.createOrder(request, mockMember.getId());

        assertThat(order).isNotNull();
        assertThat(order.getId()).isNotNull();
        //Then
        assertThat(order.getMember().getId()).isEqualTo(mockMember.getId());
        assertThat(order.getId()).isNotNull();
        assertThat(order.getId()).isEqualTo(1L);

        verify(storeRepository, times(1)).findById(mockStore.getId());
        verify(memberRepository, times(1)).findById(mockMember.getId());
        verify(beverageItemRepository, times(1)).findById(mockBeverage.getId());
        verify(orderDailyCounterRepository, times(1)).findById(any(OrderDailyCounterId.class));
        verify(orderDailyCounterRepository, times(1)).save(any(OrderDailyCounter.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("정상적인 디저트 주문 생성 테스트")
    void createOrder_DessertSuccess() {
        // Given
        int requestedQuantity = 1;
        int dessertPrice = mockDessert.getPrice();
        List<ItemOptionRequest> options = new ArrayList<>();

        List<OrderItemRequest> orderItems = List.of(
                new OrderItemRequest(
                        mockDessert.getId(),
                        ItemType.DESSERT,
                        null,
                        null,
                        options,
                        dessertPrice,
                        dessertPrice * requestedQuantity,
                        requestedQuantity
                )
        );
        CreateOrderDTO request = new CreateOrderDTO(
                mockStore.getId(),
                PickupType.STORE_PICKUP,
                orderItems.get(0).totalPrice(),
                OrderStatus.PLACED,
                LocalDateTime.now().plusMinutes(10),
                orderItems
        );

        given(storeRepository.findById(mockStore.getId())).willReturn(Optional.of(mockStore));
        given(memberRepository.findById(mockMember.getId())).willReturn(Optional.of(mockMember));
        given(dessertItemRepository.findById(mockDessert.getId())).willReturn(Optional.of(mockDessert));
        OrderDailyCounterId counterId = new OrderDailyCounterId(LocalDate.now(), mockStore.getId());

        given(orderDailyCounterRepository.findById(any(OrderDailyCounterId.class)))
                .willReturn(Optional.of(new OrderDailyCounter(counterId, 0)));
        // Mocking 값은 원하는 대로 유지 (예시에서는 5)

        given(orderDailyCounterRepository.save(any(OrderDailyCounter.class))).willAnswer(invocation -> {
                    OrderDailyCounter counter = invocation.getArgument(0);
                    return counter;
        });
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                order = Order.builder()
                        .id(2L)
                        .orderNumber(order.getOrderNumber())
                        .store(order.getStore())
                        .member(order.getMember())
                        .orderExpectedPickupTime(order.getOrderExpectedPickupTime())
                        .orderStatus(order.getOrderStatus())
                        .orderItems(order.getOrderItems())
                        .orderTotalPrice(order.getOrderTotalPrice())
                        .pickupType(order.getPickupType())
                        .build();
            }
            for (OrderItem item : order.getOrderItems()) {
                if (item.getOrder() == null) {
                    item.setOrder(order);
                }
            }
            return order;
        });

        // When
        Order order = orderService.createOrder(request, mockMember.getId());

        // Then
        assertThat(order.getId()).isNotNull();
        assertThat(order.getId()).isEqualTo(2L);

        verify(dessertItemRepository, times(1)).findById(mockDessert.getId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("존재하지 않는 매장 ID로 주문 생성 시 예외 발생")
    void createOrder_InvalidStoreId_ThrowsException() {
        // Given
        int requestedQuantity = 1;
        List<ItemOptionRequest> options = List.of(
                new ItemOptionRequest(1L, "WHIPPED_CREAM", false, 1, 500, 1)
        );

        List<OrderItemRequest> orderItems = List.of(new OrderItemRequest(
                mockBeverage.getId(),
                ItemType.COFFEE,
                BeverageSizeOption.TALL,
                BeverageTemperatureOption.HOT,
                options,
                4100,
                5000,
                requestedQuantity
        ));
        CreateOrderDTO request = new CreateOrderDTO(999L, PickupType.STORE_PICKUP, 5000, OrderStatus.PLACED, LocalDateTime.now().plusMinutes(10), orderItems );

        given(storeRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request, mockMember.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 매장입니다.");

        verify(storeRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 주문 생성 시 예외 발생")
    void createOrder_InvalidMemberId_ThrowsException() {
        // Given
        int requestedQuantity = 1;
        given(storeRepository.findById(mockStore.getId())).willReturn(Optional.of(mockStore));
        given(memberRepository.findById(999L)).willReturn(Optional.empty());
        List<ItemOptionRequest> options = List.of(
                new ItemOptionRequest(1L, "WHIPPED_CREAM", false, 1, 500, 1)
        );

        List<OrderItemRequest> orderItems = List.of(new OrderItemRequest(
                mockBeverage.getId(),
                ItemType.COFFEE,
                BeverageSizeOption.TALL,
                BeverageTemperatureOption.HOT,
                options,
                4100,
                5000,
                requestedQuantity
        ));
        CreateOrderDTO request = new CreateOrderDTO(mockStore.getId(), PickupType.STORE_PICKUP, 5000, OrderStatus.PLACED, LocalDateTime.now().plusMinutes(10), orderItems );

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");

        verify(memberRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_Success() {
        // Given
        Long orderId = 1L; // 조회할 주문 ID

        Order mockOrder = Order.builder()
                .id(orderId)
                .orderNumber("A-10")
                .store(mockStore)
                .member(mockMember)
                .orderExpectedPickupTime(LocalDateTime.now().plusMinutes(10))
                .orderStatus(OrderStatus.PLACED)
                .pickupType(PickupType.STORE_PICKUP)
                .orderTotalPrice(15000)
                .orderRequestMemo("부탁드립니다.")
                .orderItems(new ArrayList<>())
                .build();

        OrderItem mockOrderItem1 = OrderItem.builder()
                .id(10L)
                .itemName("아이스 아메리카노")
                .orderItemQuantity(1)
                .unitPrice(4500)
                .beverageItem(mockBeverage)
                .build();
        mockOrderItem1.setOrder(mockOrder);

        OrderItem mockOrderItem2 = OrderItem.builder()
                .id(11L)
                .itemName("치즈케이크")
                .orderItemQuantity(1)
                .unitPrice(5500)
                .dessertItem(mockDessert)
                .build();
        mockOrderItem2.setOrder(mockOrder);

        mockOrder.getOrderItems().add(mockOrderItem1);
        mockOrder.getOrderItems().add(mockOrderItem2);


        given(orderRepository.findById(orderId)).willReturn(Optional.of(mockOrder));


        // When
        OrderDetailResponse response = orderService.getOrderDetail(orderId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.orderNumber()).isEqualTo("A-10");
        assertThat(response.storeName()).isEqualTo(mockStore.getName());
        assertThat(response.memberName()).isEqualTo(mockMember.getName());
        assertThat(response.orderTotalPrice()).isEqualTo(mockOrder.getOrderTotalPrice());
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(response.orderItems()).hasSize(2);
        assertThat(response.orderItems().get(0).orderItemId()).isEqualTo(mockOrderItem1.getId());
        assertThat(response.orderItems().get(0).itemName()).isEqualTo(mockOrderItem1.getItemName());
        assertThat(response.orderItems().get(0).itemType()).isEqualTo(ItemType.COFFEE);
        assertThat(response.orderItems().get(1).orderItemId()).isEqualTo(mockOrderItem2.getId());
        assertThat(response.orderItems().get(1).itemName()).isEqualTo(mockOrderItem2.getItemName());
        assertThat(response.orderItems().get(1).itemType()).isEqualTo(ItemType.DESSERT);

        // Repository 호출 검증
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("주문 상세 조회 실패 - 주문 찾을 수 없음")
    void getOrderDetail_OrderNotFound_ThrowsException() {
        // Given
        Long nonExistentOrderId = 999L;
        given(orderRepository.findById(nonExistentOrderId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderDetail(nonExistentOrderId))
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());

        verify(orderRepository, times(1)).findById(nonExistentOrderId);
    }

    @Test
    @DisplayName("과거 주문 내역 조회 성공")
    void getOrderHistory_Success() {
        // Given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0, 5);

        Order order1 = Order.builder().id(10L).orderNumber("A-1").store(mockStore).orderStatus(OrderStatus.COMPLETED).build();
        Order order2 = Order.builder().id(11L).orderNumber("A-2").store(mockStore).orderStatus(OrderStatus.COMPLETED).build();
        List<Order> orderList = List.of(order1, order2);
        Page<Order> mockOrderPage = new PageImpl<>(orderList, pageable, orderList.size());

        given(memberRepository.findById(memberId)).willReturn(Optional.of(mockMember));
        given(orderRepository.findByMemberIdAndOrderStatusIn(eq(memberId), any(Collection.class), eq(pageable)))
                .willReturn(mockOrderPage);

        // When
        OrderListResponse response = orderService.getOrderHistory(memberId, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.currentPage()).isEqualTo(0);
        assertThat(response.orders()).hasSize(2);
        assertThat(response.orders().get(0).orderNumber()).isEqualTo("A-1");

        verify(memberRepository, times(1)).findById(memberId);
        verify(orderRepository, times(1)).findByMemberIdAndOrderStatusIn(eq(memberId), any(Collection.class), eq(pageable));
    }

    @Test
    @DisplayName("과거 주문 내역 조회 실패 - 존재하지 않는 회원")
    void getOrderHistory_MemberNotFound_ThrowsException() {
        // Given
        Long nonExistentMemberId = 999L;
        Pageable pageable = PageRequest.of(0, 5);

        given(memberRepository.findById(nonExistentMemberId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderHistory(nonExistentMemberId, pageable))
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.MEMBER_NOT_FOUND.getMessage());

        verify(memberRepository, times(1)).findById(nonExistentMemberId);
        verify(orderRepository, never()).findByMemberIdAndOrderStatusIn(anyLong(), any(Collection.class), any(Pageable.class));
    }

    @Test
    @DisplayName("나의 현재 주문 목록 조회 성공")
    void getCurrentOrders_Success() {
        // Given
        Long memberId = 1L;

        Order order1 = Order.builder().id(20L).orderNumber("B-1").member(mockMember).store(mockStore).orderStatus(OrderStatus.PREPARING).build();
        Order order2 = Order.builder().id(21L).orderNumber("B-2").member(mockMember).store(mockStore).orderStatus(OrderStatus.READY_FOR_PICKUP).build();
        List<Order> mockOrderList = List.of(order1, order2);

        given(orderRepository.findByMemberIdAndOrderStatusInOrderByCreatedAtAsc(eq(memberId), anyList()))
                .willReturn(mockOrderList);

        // When
        List<CurrentOrderResponse> result = orderService.getCurrentOrders(memberId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).orderIdUI()).isEqualTo("B-1");
        assertThat(result.get(0).storeName()).isEqualTo("스타벅스 강남점");

        verify(orderRepository, times(1)).findByMemberIdAndOrderStatusInOrderByCreatedAtAsc(eq(memberId), anyList());
    }

    @Test
    @DisplayName("나의 현재 주문 목록이 없을 경우 빈 리스트 반환")
    void getCurrentOrders_ReturnsEmptyList_WhenNoOrders() {
        // Given
        Long memberId = 2L;

        given(orderRepository.findByMemberIdAndOrderStatusInOrderByCreatedAtAsc(eq(memberId), anyList()))
                .willReturn(Collections.emptyList());

        // When
        List<CurrentOrderResponse> result = orderService.getCurrentOrders(memberId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(orderRepository, times(1)).findByMemberIdAndOrderStatusInOrderByCreatedAtAsc(eq(memberId), anyList());
    }

}
