package git_kkalnane.backend.starbucks.order.service;

import git_kkalnane.backend.starbucks.item.domain.ItemType;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import git_kkalnane.backend.starbucks.item.repository.BeverageItemRepository;
import git_kkalnane.backend.starbucks.item.repository.DessertItemRepository;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.domain.OrderDailyCounter;
import git_kkalnane.backend.starbucks.order.domain.OrderItem;
import git_kkalnane.backend.starbucks.order.dto.request.CreateRequest;
import git_kkalnane.backend.starbucks.order.dto.request.OrderItemRequest;
import git_kkalnane.backend.starbucks.order.dto.response.CreateResponse;
import git_kkalnane.backend.starbucks.order.repository.OrderDailyCounterRepository;
import git_kkalnane.backend.starbucks.order.repository.OrderItemRepository;
import git_kkalnane.backend.starbucks.order.repository.OrderRepository;
import git_kkalnane.backend.starbucks.store.domain.Store;
import git_kkalnane.backend.starbucks.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {


    private final OrderDailyCounterRepository orderDailyCounterRepository;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final BeverageItemRepository beverageItemRepository;
    private final DessertItemRepository dessertItemRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 주문생성 로직
     * Store, Member가 실제 존재하는지 검증
     * OrderItem으로 주문정보를 받고, 정보를 통해 Order생성
     * @param request
     * @param memberId
     * @return
     */
    @Transactional
    public CreateResponse createOrder(CreateRequest request, Long memberId) {

        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));


        List<OrderItem> orderItems = request.orderItems().stream()
                .map(this::validateAndCreateOrderItems)
                .collect(Collectors.toList());

        String orderNumber = generateOrderNumber();

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

        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);



        return new CreateResponse(
                200,
                "주문이 성공적으로 생성되었습니다.",
                order.getId()
        );
    }

    /**
     * 음료, 디저트 존재하는지 검증로직
     * OrderItemRequest로 넘어오는 ItemType을 비교하여 실제하는 메뉴인지 확인한다.
     * @param request
     * @return
     */

    private OrderItem validateAndCreateOrderItems(OrderItemRequest request) {
        if (request.itemType() == ItemType.DRINK) {
            BeverageItem item = beverageItemRepository.findById(request.itemId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 음료입니다."));

            return OrderItem.builder()
                    .itemName(item.getBeverageItemNameKo())
                    .unitPrice(request.itemPrice())
                    .orderItemQuantity(1)
                    .beverageItem(item)
                    .build();

        } else if (request.itemType() == ItemType.DESSERT) {
            DessertItem item = dessertItemRepository.findById(request.itemId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 디저트입니다."));

            return OrderItem.builder()
                    .itemName(item.getDessertItemNameKo())
                    .unitPrice(request.itemPrice())
                    .orderItemQuantity(1)
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
    public String generateOrderNumber() {
        LocalDate today = LocalDate.now();

        OrderDailyCounter counter = orderDailyCounterRepository.findById(today)
                .orElseGet(() -> new OrderDailyCounter(today, 0));

        counter.increment();
        orderDailyCounterRepository.save(counter);

        return "A - " + counter.getCount();
    }

}
