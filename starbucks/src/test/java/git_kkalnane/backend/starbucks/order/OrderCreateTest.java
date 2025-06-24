package git_kkalnane.backend.starbucks.order.service;

import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageSizeOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageTemperatureOption;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import git_kkalnane.backend.starbucks.item.repository.BeverageItemRepository;
import git_kkalnane.backend.starbucks.item.repository.DessertItemRepository;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import git_kkalnane.backend.starbucks.order.domain.*;
import git_kkalnane.backend.starbucks.order.dto.request.CreateRequest;
import git_kkalnane.backend.starbucks.order.dto.request.ItemOptionRequest;
import git_kkalnane.backend.starbucks.order.dto.request.OrderItemRequest;
import git_kkalnane.backend.starbucks.order.dto.response.CreateResponse;
import git_kkalnane.backend.starbucks.order.repository.OrderDailyCounterRepository;
import git_kkalnane.backend.starbucks.order.repository.OrderRepository;
import git_kkalnane.backend.starbucks.store.domain.Store;
import git_kkalnane.backend.starbucks.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCreateTest {

    @Mock private OrderDailyCounterRepository orderDailyCounterRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private BeverageItemRepository beverageItemRepository;
    @Mock private DessertItemRepository dessertItemRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks private OrderService orderService;

    private Store mockStore;
    private Member mockMember;
    private BeverageItem mockBeverage;
    private DessertItem mockDessert;

    @BeforeEach
    void 정보_세팅() {
        mockStore = Store.builder().id(1L).build();
        mockMember = Member.builder().id(1L).build();
        mockBeverage = BeverageItem.builder().id(101L).beverageItemNameKo("아이스 아메리카노").price(4500).build();
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
                        ItemType.DRINK,
                        BeverageSizeOption.TALL,
                        BeverageTemperatureOption.HOT,
                        options,
                        beveragePrice,
                        beveragePrice * requestedQuantity,
                        requestedQuantity
                )
        );
        CreateRequest request = new CreateRequest(
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
        given(orderDailyCounterRepository.findById(any(LocalDate.class)))
                .willReturn(Optional.of(new OrderDailyCounter(LocalDate.now(), 0))); // 처음에는 0, increment 후 1
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
        CreateResponse response = orderService.createOrder(request, mockMember.getId());

        // Then
        assertThat(response.httpStatus()).isEqualTo(200);
        assertThat(response.message()).contains("성공");
        assertThat(response.orderId()).isNotNull();
        assertThat(response.orderId()).isEqualTo(1L);

        verify(storeRepository, times(1)).findById(mockStore.getId());
        verify(memberRepository, times(1)).findById(mockMember.getId());
        verify(beverageItemRepository, times(1)).findById(mockBeverage.getId());
        verify(orderDailyCounterRepository, times(1)).findById(any(LocalDate.class));
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
        CreateRequest request = new CreateRequest(
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
        given(orderDailyCounterRepository.findById(any(LocalDate.class))).willReturn(Optional.of(new OrderDailyCounter(LocalDate.now(), 5))); // Mocking 값은 원하는 대로 유지 (예시에서는 5)

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
        CreateResponse response = orderService.createOrder(request, mockMember.getId());

        // Then
        assertThat(response.httpStatus()).isEqualTo(200);
        assertThat(response.message()).contains("성공");
        assertThat(response.orderId()).isNotNull();
        assertThat(response.orderId()).isEqualTo(2L);

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
                ItemType.DRINK,
                BeverageSizeOption.TALL,
                BeverageTemperatureOption.HOT,
                options,
                4100,
                5000,
                requestedQuantity
        ));
        CreateRequest request = new CreateRequest(999L, PickupType.STORE_PICKUP, 5000, OrderStatus.PLACED, LocalDateTime.now().plusMinutes(10), orderItems );

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
                ItemType.DRINK,
                BeverageSizeOption.TALL,
                BeverageTemperatureOption.HOT,
                options,
                4100,
                5000,
                requestedQuantity
        ));
        CreateRequest request = new CreateRequest(mockStore.getId(), PickupType.STORE_PICKUP, 5000, OrderStatus.PLACED, LocalDateTime.now().plusMinutes(10), orderItems );

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");

        verify(memberRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }
}
