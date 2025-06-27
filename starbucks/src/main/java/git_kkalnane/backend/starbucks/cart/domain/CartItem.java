package git_kkalnane.backend.starbucks.cart.domain;

import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItem;
import git_kkalnane.backend.starbucks.item.domain.beverage.CartItemOption;
import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "cart_items")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "cart_item_quantity", nullable = false)
    private int cartItemQuantity = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_item_id")
    private BeverageItem beverageItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_item_id")
    private DessertItem dessertItem;

    @Builder.Default
    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemOption> cartItemOption = new ArrayList<>();

    public void setCartItemOption(List<CartItemOption> options) {
        this.cartItemOption.clear();
        for (CartItemOption option : options) {
            this.cartItemOption.add(option);
            option.setCartItem(this);
        }
    }
    public int totalPrice() {
        int price = 0;

        if(this.beverageItem != null) {
            price = this.beverageItem.getPrice();
        } else if(this.dessertItem != null) {
            price = this.dessertItem.getPrice();
        }

        int optionPrice = this.cartItemOption.stream()
                .mapToInt(opt -> opt.getItemOption().getAdditionalPrice())
                .sum();

        return (price + optionPrice) * this.cartItemQuantity;
    }

    public void changeQuantity(int changeQuantity) {
        this.cartItemQuantity = changeQuantity;
    }


}
