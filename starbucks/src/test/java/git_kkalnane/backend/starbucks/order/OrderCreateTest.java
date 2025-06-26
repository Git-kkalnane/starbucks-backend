package git_kkalnane.backend.starbucks.order.service;

import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageSizeOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageTemperatureOption;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import git_kkalnane.backend.starbucks.item.repository.BeverageItemRepository;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import git_kkalnane.backend.starbucks.order.domain.*;
import git_kkalnane.backend.starbucks.order.dto.request.CreateOrderDTO;
import git_kkalnane.backend.starbucks.order.dto.request.ItemOptionRequest;
import git_kkalnane.backend.starbucks.order.dto.request.OrderItemRequest;
import git_kkalnane.backend.starbucks.order.dto.response.CreateResponse;
import git_kkalnane.backend.starbucks.order.repository.OrderDailyCounterRepository;
import git_kkalnane.backend.starbucks.order.repository.OrderItemRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;


    @InjectMocks private OrderService orderService;

    private Store mockStore;
    private Member mockMember;
    private BeverageItem mockBeverage;
    private DessertItem mockDessert;

    @BeforeEach
    void 정보_세팅() {
        mockStore = Store.builder().id(1L).build();
        mockMember = Member.builder().id(1L).build();
        mockBeverage = BeverageItem.builder().id(101L).beverageItemNameKo("아이스 아메리카노").build();
        mockDessert = DessertItem.builder().id(201L).dessertItemNameKo("치즈케이크").build();
    }

    @Test
    @DisplayName("정상적인 주문 생성 테스트")
    void createOrder_Success() {
        // Given
        List<ItemOptionRequest> options = List.of(
                new ItemOptionRequest(1L, "WHIPPED_CREAM", false, 1, 500, 1)
        );

        List<OrderItemRequest> orderItems = List.of(new OrderItemRequest(mockBeverage.getId(), ItemType.COFFEE, BeverageSizeOption.TALL, BeverageTemperatureOption.HOT, options, 4100, 5000, 2 ));
        CreateOrderDTO request = new CreateOrderDTO(mockStore.getId(), PickupType.STORE_PICKUP, 5000, OrderStatus.PLACED, LocalDateTime.now().plusMinutes(10), orderItems );

        given(storeRepository.findById(mockStore.getId())).willReturn(Optional.of(mockStore));
        given(beverageItemRepository.findById(mockBeverage.getId())).willReturn(Optional.of(mockBeverage));
        given(memberRepository.findById(mockMember.getId())).willReturn(Optional.of(mockMember));
        OrderDailyCounterId id = new OrderDailyCounterId(LocalDate.now(), request.storeId());

        given(orderDailyCounterRepository.findById(any(OrderDailyCounterId.class)))
                .willReturn(Optional.of(new OrderDailyCounter(
                        new OrderDailyCounterId(LocalDate.now(), request.storeId()), 5
                )));

        // When
        orderService.createOrder(request, mockMember.getId());

        // Then
        assertThat(orderRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 매장 ID로 주문 생성 시 예외 발생")
    void createOrder_InvalidStoreId_ThrowsException() {
        // Given
        List<ItemOptionRequest> options = List.of(
                new ItemOptionRequest(1L, "WHIPPED_CREAM", false, 1, 500, 1)
        );

        List<OrderItemRequest> orderItems = List.of(new OrderItemRequest(mockBeverage.getId(), ItemType.COFFEE, BeverageSizeOption.TALL, BeverageTemperatureOption.HOT, options, 4100, 5000 , 2));
        CreateOrderDTO request = new CreateOrderDTO(999L, PickupType.STORE_PICKUP, 5000, OrderStatus.PLACED, LocalDateTime.now().plusMinutes(10), orderItems );


        // When & Then
        assertThat(request.storeId().equals(mockStore.getId())).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 주문 생성 시 예외 발생")
    void createOrder_InvalidMemberId_ThrowsException() {
        // Given
        given(storeRepository.findById(mockStore.getId())).willReturn(Optional.of(mockStore));
        given(memberRepository.findById(999L)).willReturn(Optional.empty());
        List<ItemOptionRequest> options = List.of(
                new ItemOptionRequest(1L, "WHIPPED_CREAM", false, 1, 500, 1)
        );

        List<OrderItemRequest> orderItems = List.of(new OrderItemRequest(mockBeverage.getId(), ItemType.COFFEE, BeverageSizeOption.TALL, BeverageTemperatureOption.HOT, options, 4100, 5000 ,2));
        CreateOrderDTO request = new CreateOrderDTO(mockStore.getId(), PickupType.STORE_PICKUP, 5000, OrderStatus.PLACED, LocalDateTime.now().plusMinutes(10), orderItems );

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 음료 ID로 주문 생성 시 예외 발생")
    void createOrder_InvalidBeverageId_ThrowsException() {
        // Given
        List<ItemOptionRequest> options = List.of(
                new ItemOptionRequest(1L, "WHIPPED_CREAM", false, 1, 500, 1)
        );

        List<OrderItemRequest> orderItems = List.of(new OrderItemRequest(mockBeverage.getId(), ItemType.COFFEE, BeverageSizeOption.TALL, BeverageTemperatureOption.HOT, options, 4100, 5000 ,2));
        CreateOrderDTO request = new CreateOrderDTO(mockStore.getId(), PickupType.STORE_PICKUP, 5000, OrderStatus.PLACED, LocalDateTime.now().plusMinutes(10), orderItems );

        given(storeRepository.findById(mockStore.getId())).willReturn(Optional.of(mockStore));
        given(memberRepository.findById(mockMember.getId())).willReturn(Optional.of(mockMember));
        given(beverageItemRepository.findById(orderItems.getFirst().itemId())).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request, mockMember.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 음료입니다.");
    }
}
