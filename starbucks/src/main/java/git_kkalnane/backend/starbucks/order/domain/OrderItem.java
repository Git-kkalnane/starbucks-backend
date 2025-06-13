package git_kkalnane.backend.starbucks.order.domain;

import git_kkalnane.backend.starbucks.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name_at_order", nullable = false)
    private String productName;

    @Column(name = "order_item_quantity",nullable = false, precision = 10, scale = 2)
    private int orderItemQuantity = 1;

    @Column(name = "unit_price_at_order", nullable = false)
    private double unitPrice;

//    @Type(JsonType.class)
//    @Column(name = "select_option_json", columnDefinition = "json")
//    private SelectedOption selectedOption;

}
