package git_kkalnane.backend.starbucks.item.domain;

import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "beverage_items")
@NoArgsConstructor
public class BeverageItem extends BaseTimeEntity {

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
    private BeverageShotOption shotOption = BeverageShotOption.SHOT;

    @Enumerated(EnumType.STRING)
    private BeverageSizeOption sizeOption;

    @Enumerated(EnumType.STRING)
    private BeverageTemperatureOption temperatureOption;

    @OneToMany(mappedBy = "beverageItem", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BeverageItemSyrup> beverageItemSyrup = new ArrayList<>();

}
