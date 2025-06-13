package git_kkalnane.backend.starbucks.order.domain;

import git_kkalnane.backend.starbucks.global.entity.BaseTimeEntity;
import git_kkalnane.backend.starbucks.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Long id;

    @Column(name = "order_number", nullable = false)
    private Long orderNumber;

    @Column(name = "order_total_price", nullable = false)
    private Long orderTotalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus = OrderStatus.PLACED;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_pickup_type", nullable = false)
    private PickupType pickupType;

    @Column(name = "order_request_memo", columnDefinition = "TEXT")
    private String orderRequestMemo;

    @Column(name = "order_expected_pickup_time", nullable = false)
    private Long orderExpectedPickupTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<OrderItem> OrderItems = new ArrayList<>();



}
