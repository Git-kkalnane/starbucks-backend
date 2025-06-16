package git_kkalnane.backend.starbucks.item.domain;

import git_kkalnane.backend.starbucks.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dessert_items")
@NoArgsConstructor
public class DessertItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name_ko", nullable = false, unique = true, length = 50)
    private String itemNameKo;

    @Column(name = "item_name_en", nullable = false, unique = true, length = 50)
    private String itemNameEn;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "image_url", length = 254)
    private String image_url;

    @Enumerated(EnumType.STRING)
    private ItemStatus status = ItemStatus.AVAILABLE;

}
