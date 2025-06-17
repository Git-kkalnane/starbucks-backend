package git_kkalnane.backend.starbucks.item.domain;

import git_kkalnane.backend.starbucks.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "beverage_items")
@NoArgsConstructor
public class BervergeItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "beverage_item_name_ko", nullable = false, unique = true, length = 50)
    private String beverageItemNameKo;

    @Column(name = "beverage_item_name_en", nullable = false, unique = true, length = 50)
    private String beverageItemNameEn;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "image_url", length = 254)
    private String image_url;

    @Enumerated(EnumType.STRING)
    private ItemStatus status = ItemStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    private BevarageShotOption shotOption = BevarageShotOption.SHOT;

    @Enumerated(EnumType.STRING)
    private BeverageSizeOption sizeOption;

    @Enumerated(EnumType.STRING)
    private BeverageTemperatureOption temperatureOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_option_id")
    private ItemOption option;

}
