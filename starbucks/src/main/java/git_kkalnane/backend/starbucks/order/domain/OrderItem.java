package git_kkalnane.backend.starbucks.order.domain;

import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageTemperatureOption;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Entity
@Table(name = "order_items")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name_at_order", nullable = false)
    private String itemName;

    @Builder.Default
    @Column(name = "order_item_quantity",nullable = false)
    private int orderItemQuantity = 1;

    @Column(name = "unit_price_at_order", nullable = false)
    private int unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_item_id")
    private BeverageItem beverageItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_item_id")
    private DessertItem dessertItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "temperature_option_at_order")
    private BeverageTemperatureOption temperatureOption;

    public void setOrder(Order order) {
        this.order = order;
    }

}
