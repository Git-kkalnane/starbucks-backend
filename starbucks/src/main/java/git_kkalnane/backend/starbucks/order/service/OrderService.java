package git_kkalnane.backend.starbucks.order.service;

import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import git_kkalnane.backend.starbucks.item.repository.BeverageItemRepository;
import git_kkalnane.backend.starbucks.item.repository.DessertItemRepository;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import git_kkalnane.backend.starbucks.order.common.exception.OrderErrorCode;
import git_kkalnane.backend.starbucks.order.common.exception.OrderException;
import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.domain.OrderDailyCounter;
import git_kkalnane.backend.starbucks.order.domain.OrderItem;
import git_kkalnane.backend.starbucks.order.domain.OrderStatus;
import git_kkalnane.backend.starbucks.order.dto.request.OrderItemRequest;
import git_kkalnane.backend.starbucks.order.dto.response.CurrentOrderResponse;
import git_kkalnane.backend.starbucks.order.dto.response.OrderDetailResponse;
import git_kkalnane.backend.starbucks.order.dto.response.OrderListResponse;
import git_kkalnane.backend.starbucks.order.dto.response.OrderSummaryResponse;
import git_kkalnane.backend.starbucks.order.domain.OrderDailyCounterId;
import git_kkalnane.backend.starbucks.order.dto.request.CreateOrderDTO;
import git_kkalnane.backend.starbucks.order.repository.OrderDailyCounterRepository;
import git_kkalnane.backend.starbucks.order.repository.OrderItemRepository;
import git_kkalnane.backend.starbucks.order.repository.OrderRepository;
import git_kkalnane.backend.starbucks.payment.service.PaymentService;
import git_kkalnane.backend.starbucks.store.domain.Store;
import git_kkalnane.backend.starbucks.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {


    private final OrderDailyCounterRepository orderDailyCounterRepository;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final BeverageItemRepository beverageItemRepository;
    private final DessertItemRepository dessertItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentService paymentService;

    /**
     * 주문생성 로직
     * Store, Member가 실제 존재하는지 검증
     * OrderItem으로 주문정보를 받고, 정보를 통해 Order생성
     * @param request
     * @param memberId
     * @return
     */
    @Transactional
    public Order createOrder(CreateOrderDTO request, Long memberId) {

        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));


        List<OrderItem> orderItems = request.orderItems().stream()
                .map(this::validateAndCreateOrderItems)
                .collect(Collectors.toList());

        String orderNumber = generateOrderNumber(request);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .store(store)
                .member(member)
                .orderExpectedPickupTime(LocalDateTime.now().plusMinutes(10))
                .orderStatus(request.orderStatus())
                .orderItems(new ArrayList<>())
                .orderTotalPrice(request.orderTotalPrice())
                .pickupType(request.pickupType())
                .build();

        for (OrderItem item : orderItems) {
            order.addOrderItem(item);
        }

        Order savedOrder = orderRepository.save(order);

        paymentService.processPayment(order);

        return savedOrder;
    }

    /**
     * 음료, 디저트 존재하는지 검증로직
     * OrderItemRequest로 넘어오는 ItemType을 비교하여 실제하는 메뉴인지 확인한다.
     * @param request
     * @return
     */

    private OrderItem validateAndCreateOrderItems(OrderItemRequest request) {
        int orderQuantity = request.quantity();

        if (request.itemType() == ItemType.COFFEE) {
            BeverageItem item = beverageItemRepository.findById(request.itemId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 음료입니다."));

            return OrderItem.builder()
                    .itemName(item.getBeverageItemNameKo())
                    .unitPrice(item.getPrice())
                    .orderItemQuantity(orderQuantity)
                    .beverageItem(item)
                    .build();

        } else if (request.itemType() == ItemType.DESSERT) {
            DessertItem item = dessertItemRepository.findById(request.itemId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 디저트입니다."));

            return OrderItem.builder()
                    .itemName(item.getDessertItemNameKo())
                    .unitPrice(item.getPrice())
                    .orderItemQuantity(orderQuantity)
                    .dessertItem(item)
                    .build();
        }

        throw new IllegalArgumentException("잘못된 메뉴 타입입니다.");
    }

    /**
     * 주문번호 생성 메소드
     * @return : 날짜를 비교하여, 날짜가 지나면 0으로 초기화하고 1부터 다시 주문번호가 시작된다.
     */
    @Transactional
    public String generateOrderNumber(CreateOrderDTO request) {
        Long storeId = request.storeId();
        LocalDate today = LocalDate.now();

        OrderDailyCounterId id = new OrderDailyCounterId(today, storeId);

        OrderDailyCounter counter = orderDailyCounterRepository.findById(id)
                .orElseGet(() -> new OrderDailyCounter(id, 0));

        counter.increment();

        orderDailyCounterRepository.save(counter);

        return "A-" + counter.getCount();
    }

    /**
     * 주문 상세 내용을 조회하는 로직.
     * 특정 주문 ID로 Order 엔티티를 조회하고, OrderDetailResponse DTO로 변환하여 반환합니다.
     *
     * @param orderId 조회할 주문의 고유 ID
     * @return 주문 상세 정보를 담은 OrderDetailResponse DTO
     * @throws OrderException 주어진 orderId로 주문을 찾을 수 없을 경우
     */

    public OrderDetailResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        return OrderDetailResponse.from(order);
    }

    /**
     * 과거 주문 내역 목록을 조회하는 로직.
     * 특정 회원의 주문 목록을 페이지네이션과 함께 조회합니다.
     *
     * @param memberId 조회할 회원의 고유 ID
     * @param pageable 페이징 및 정렬 정보
     * @return 페이지네이션된 주문 요약 정보 목록
     * @throws OrderException memberId로 회원을 찾을 수 없을 경우
     */
    public OrderListResponse getOrderHistory(Long memberId, Pageable pageable){
    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new OrderException(OrderErrorCode.MEMBER_NOT_FOUND));

        List<OrderStatus> pastOrderStatuses = List.of(OrderStatus.COMPLETED);
        Page<Order> orderPage = orderRepository.findByMemberIdAndOrderStatusIn(memberId, pastOrderStatuses, pageable);
        Page<OrderSummaryResponse> summaryPage = orderPage.map(OrderSummaryResponse::from);

        return OrderListResponse.from(summaryPage);
    }
    /**
     * 특정 회원의 현재 진행중인 모든 주문 목록(주문 접수, 준비중, 픽업 가능)을 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 현재 진행중인 주문의 상세 정보 DTO 리스트
     */
    public List<CurrentOrderResponse> getCurrentOrders(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.MEMBER_NOT_FOUND));

        List<OrderStatus> currentStatuses = List.of(
                OrderStatus.PLACED,
                OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP
        );

        List<Order> currentOrders = orderRepository.findByMemberIdAndOrderStatusInOrderByCreatedAtAsc(memberId, currentStatuses);

        return currentOrders.stream()
                .map(CurrentOrderResponse::from)
                .collect(Collectors.toList());
    }

}
