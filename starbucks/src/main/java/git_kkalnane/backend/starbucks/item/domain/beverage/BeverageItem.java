package git_kkalnane.backend.starbucks.item.domain.beverage;

import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
import git_kkalnane.backend.starbucks.item.domain.ItemStatus;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageShotOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageSizeOption;
import git_kkalnane.backend.starbucks.item.domain.beverage.enums.BeverageTemperatureOption;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "beverage_items")
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "hot_image_url", length = 254)
    private String hotImageUrl;

    @Column(name = "ice_image_url", length = 254)
    private String iceImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType category;

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
