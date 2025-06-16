package git_kkalnane.backend.starbucks.cart.domain;

import git_kkalnane.backend.starbucks.global.entity.BaseTimeEntity;
import git_kkalnane.backend.starbucks.item.domain.BervergeItem;
import git_kkalnane.backend.starbucks.item.domain.DessertItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cart_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cart_item_quantity", nullable = false)
    private int cartItemQuantity = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_item_id")
    private BervergeItem beverageItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_item_id")
    private DessertItem dessertItem;


}
