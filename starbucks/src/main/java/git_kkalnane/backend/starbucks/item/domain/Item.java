package git_kkalnane.backend.starbucks.item.domain;

import git_kkalnane.backend.starbucks.cart.domain.CartItem;
import git_kkalnane.backend.starbucks.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name_ko", nullable = false, unique = true, length = 50)
    private String itemNameKo;

    @Column(name = "item_name_en", nullable = false, unique = true, length = 50)
    private String itemNameEn;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private double price;

    @Column(name = "image_url", length = 254)
    private String image_url;

    @Enumerated(EnumType.STRING)
    private ItemStatus status = ItemStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    private ItemType optionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category")
    private ItemCategory itemCategory;

    @OneToMany(mappedBy = "item_option", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ItemOption> itemOptions = new ArrayList<>();

    @OneToMany(mappedBy = "cart_item", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();


}
