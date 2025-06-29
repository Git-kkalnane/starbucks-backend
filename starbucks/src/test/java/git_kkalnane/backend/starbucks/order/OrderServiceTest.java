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
import git_kkalnane.backend.starbucks.order.dto.response.StoreOrderResponse;
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
        mockMember = Member.builder().id(1L).name("장원영").nickname("워뇽이").build();
        mockStore = Store.builder()
                .id(1L)
                .name("스타벅스 강남점")
                .address("서울시 강남구 강남대로 123")
                .build();

        mockBeverage = BeverageItem.builder()
                .id(101L)
                .beverageItemNameKo("아이스 아메리카노")
                .beverageItemNameEn("Iced Americano")
                .description("시원한 아메리카노")
                .price(4500)
                .isCoffee(true)
                .build();

        mockDessert = DessertItem.builder()
                .id(201L)
                .dessertItemNameKo("치즈케이크")
                .dessertItemNameEn("Cheesecake")
                .description("부드러운 치즈케이크")
                .price(5500)
                .build();
    }

    @Test
    @DisplayName("정상적인 음료 주문 생성 테스트")
    void createOrder_Success() {
        // Given
        int requestedQuantity = 2;
        int beveragePrice = mockBeverage.getPrice();
        int totalPrice = beveragePrice * requestedQuantity;

        List<OrderItemRequest> orderItems = List.of(
                new OrderItemRequest(
                        mockBeverage.getId(), ItemType.COFFEE, BeverageSizeOption.TALL,
                        BeverageTemperatureOption.HOT, new ArrayList<>(), beveragePrice,
                        totalPrice, requestedQuantity
                )
        );
        CreateOrderDTO request = new CreateOrderDTO(
                mockStore.getId(), PickupType.STORE_PICKUP, totalPrice, OrderStatus.PLACED,
                LocalDateTime.now().plusMinutes(10), orderItems
        );

        Order savedOrder = Order.builder().id(1L).member(mockMember).store(mockStore).build();

        given(storeRepository.findById(mockStore.getId())).willReturn(Optional.of(mockStore));
        given(memberRepository.findById(mockMember.getId())).willReturn(Optional.of(mockMember));
        given(beverageItemRepository.findById(mockBeverage.getId())).willReturn(Optional.of(mockBeverage));
        given(orderDailyCounterRepository.findById(any(OrderDailyCounterId.class)))
                .willReturn(Optional.of(new OrderDailyCounter(new OrderDailyCounterId(LocalDate.now(), mockStore.getId()), 0)));
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        // When
        Order resultOrder = orderService.createOrder(request, mockMember.getId());

        // Then
        assertThat(resultOrder).isNotNull();
        assertThat(resultOrder.getId()).isEqualTo(1L);
        assertThat(resultOrder.getMember().getId()).isEqualTo(mockMember.getId());

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

        List<OrderItemRequest> orderItems = List.of(
                new OrderItemRequest(
                        mockDessert.getId(), ItemType.DESSERT, null, null,
                        new ArrayList<>(), dessertPrice, dessertPrice, requestedQuantity
                )
        );
        CreateOrderDTO request = new CreateOrderDTO(
                mockStore.getId(), PickupType.STORE_PICKUP, dessertPrice, OrderStatus.PLACED,
                LocalDateTime.now().plusMinutes(10), orderItems
        );

        Order savedOrder = Order.builder().id(2L).member(mockMember).store(mockStore).build();

        given(storeRepository.findById(mockStore.getId())).willReturn(Optional.of(mockStore));
        given(memberRepository.findById(mockMember.getId())).willReturn(Optional.of(mockMember));
        given(dessertItemRepository.findById(mockDessert.getId())).willReturn(Optional.of(mockDessert));
        given(orderDailyCounterRepository.findById(any(OrderDailyCounterId.class)))
                .willReturn(Optional.of(new OrderDailyCounter(new OrderDailyCounterId(LocalDate.now(), mockStore.getId()), 0)));

        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        // When
        Order resultOrder = orderService.createOrder(request, mockMember.getId());

        // Then
        assertThat(resultOrder).isNotNull();
        assertThat(resultOrder.getId()).isEqualTo(2L);
        assertThat(resultOrder.getMember()).isEqualTo(mockMember);

        verify(dessertItemRepository, times(1)).findById(mockDessert.getId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }


    @Test
    @DisplayName("존재하지 않는 매장 ID로 주문 생성 시 예외 발생")
    void createOrder_InvalidStoreId_ThrowsException() {
        // Given
        CreateOrderDTO request = new CreateOrderDTO(999L, PickupType.STORE_PICKUP, 5000, OrderStatus.PLACED, LocalDateTime.now().plusMinutes(10), List.of());
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
        long invalidMemberId = 999L;
        CreateOrderDTO request = new CreateOrderDTO(mockStore.getId(), PickupType.STORE_PICKUP, 5000, OrderStatus.PLACED, LocalDateTime.now().plusMinutes(10), List.of());

        given(storeRepository.findById(mockStore.getId())).willReturn(Optional.of(mockStore));
        given(memberRepository.findById(invalidMemberId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request, invalidMemberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");

        verify(memberRepository, times(1)).findById(invalidMemberId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_Success() {
        // Given
        Long orderId = 1L;
        Order mockOrder = Order.builder()
                .id(orderId).orderNumber("A-10").store(mockStore).member(mockMember)
                .orderExpectedPickupTime(LocalDateTime.now().plusMinutes(10))
                .orderStatus(OrderStatus.PLACED).pickupType(PickupType.STORE_PICKUP)
                .orderTotalPrice(10000).orderRequestMemo("부탁드립니다.")
                .orderItems(new ArrayList<>()).build();

        OrderItem mockOrderItem1 = OrderItem.builder().id(10L).itemName("아이스 아메리카노").orderItemQuantity(1).unitPrice(4500).beverageItem(mockBeverage).build();
        OrderItem mockOrderItem2 = OrderItem.builder().id(11L).itemName("치즈케이크").orderItemQuantity(1).unitPrice(5500).dessertItem(mockDessert).build();
        mockOrder.addOrderItem(mockOrderItem1);
        mockOrder.addOrderItem(mockOrderItem2);

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
        assertThat(response.orderItems().get(0).itemName()).isEqualTo("아이스 아메리카노");
        assertThat(response.orderItems().get(1).itemName()).isEqualTo("치즈케이크");

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
        Page<Order> mockOrderPage = new PageImpl<>(List.of(order1, order2), pageable, 2);

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
        Long memberId = mockMember.getId();
        Order order1 = Order.builder().id(20L).orderNumber("B-1").member(mockMember).store(mockStore).orderStatus(OrderStatus.PREPARING).build();
        Order order2 = Order.builder().id(21L).orderNumber("B-2").member(mockMember).store(mockStore).orderStatus(OrderStatus.READY_FOR_PICKUP).build();
        List<Order> mockOrderList = List.of(order1, order2);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(mockMember));
        given(orderRepository.findByMemberIdAndOrderStatusInOrderByCreatedAtAsc(eq(memberId), anyList()))
                .willReturn(mockOrderList);

        // When
        List<CurrentOrderResponse> result = orderService.getCurrentOrders(memberId);

        // Then
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).orderIdUI()).isEqualTo("B-1");
        assertThat(result.get(0).storeName()).isEqualTo("스타벅스 강남점");

        verify(orderRepository, times(1)).findByMemberIdAndOrderStatusInOrderByCreatedAtAsc(eq(memberId), anyList());
    }

    @Test
    @DisplayName("나의 현재 주문 목록이 없을 경우 빈 리스트 반환")
    void getCurrentOrders_ReturnsEmptyList_WhenNoOrders() {
        // Given
        Long memberId = mockMember.getId();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(mockMember));
        given(orderRepository.findByMemberIdAndOrderStatusInOrderByCreatedAtAsc(eq(memberId), anyList()))
                .willReturn(Collections.emptyList());

        // When
        List<CurrentOrderResponse> result = orderService.getCurrentOrders(memberId);

        // Then
        assertThat(result).isNotNull().isEmpty();

        verify(orderRepository, times(1)).findByMemberIdAndOrderStatusInOrderByCreatedAtAsc(eq(memberId), anyList());
    }

    @Test
    @DisplayName("매장의 현재 주문 목록 조회 성공")
    void getStoreCurrentOrders_Success() {
        // Given
        Long storeId = mockStore.getId();
        Member callingMember = Member.builder().id(2L).nickname("호출손님").build();

        Order order1 = Order.builder().id(30L).orderNumber("C-1").member(mockMember).store(mockStore).orderStatus(OrderStatus.PREPARING).build();
        Order order2 = Order.builder().id(31L).orderNumber("C-2").member(callingMember).store(mockStore).orderStatus(OrderStatus.PLACED).build();
        List<Order> mockOrderList = List.of(order1, order2);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(mockStore));
        given(orderRepository.findByStoreIdAndOrderStatusInOrderByCreatedAtAsc(eq(storeId), anyList()))
                .willReturn(mockOrderList);

        // When
        List<StoreOrderResponse> result = orderService.getStoreCurrentOrders(storeId);

        // Then
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).orderNumber()).isEqualTo("C-1");
        assertThat(result.get(0).memberNickname()).isEqualTo(mockMember.getNickname()); // "워뇽이"
        assertThat(result.get(1).orderNumber()).isEqualTo("C-2");
        assertThat(result.get(1).memberNickname()).isEqualTo(callingMember.getNickname()); // "호출손님"

        verify(storeRepository, times(1)).findById(storeId);
        verify(orderRepository, times(1)).findByStoreIdAndOrderStatusInOrderByCreatedAtAsc(eq(storeId), anyList());
    }

    @Test
    @DisplayName("매장의 현재 주문 목록 조회 실패 - 존재하지 않는 매장")
    void getStoreCurrentOrders_StoreNotFound_ThrowsException() {
        // Given
        Long nonExistentStoreId = 999L;
        given(storeRepository.findById(nonExistentStoreId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getStoreCurrentOrders(nonExistentStoreId))
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.STORE_NOT_FOUND.getMessage());

        verify(storeRepository, times(1)).findById(nonExistentStoreId);
        verify(orderRepository, never()).findByStoreIdAndOrderStatusInOrderByCreatedAtAsc(anyLong(), anyList());
    }

    @Test
    @DisplayName("매장 주문 상세 조회 성공")
    void getStoreOrderDetail_Success() {
        Long storeId = 1L;
        Long orderId = 10L;

        Store testMockStore = mock(Store.class);
        given(testMockStore.getId()).willReturn(storeId);
        Order testMockOrder = mock(Order.class);
        given(testMockOrder.getId()).willReturn(orderId);
        given(testMockOrder.getStore()).willReturn(testMockStore);
        given(orderRepository.findById(orderId)).willReturn(Optional.of(testMockOrder));

        // when
        Order foundOrder = orderService.getStoreOrderDetail(storeId, orderId);

        // then
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getId()).isEqualTo(orderId);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("매장 주문 상세 조회 실패 - 다른 매장의 주문이라 권한 없음")
    void getStoreOrderDetail_Fail_ForbiddenAccess() {
        // given
        Long myStoreId = 1L;
        Long anotherStoreId = 2L;
        Long orderId = 10L;

        Store anotherStore = mock(Store.class);
        given(anotherStore.getId()).willReturn(anotherStoreId);

        Order mockOrder = mock(Order.class);
        given(mockOrder.getStore()).willReturn(anotherStore);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(mockOrder));

        // when & then
        assertThatThrownBy(() -> orderService.getStoreOrderDetail(myStoreId, orderId))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("해당 주문에 접근할 권한이 없습니다.");
    }
}
