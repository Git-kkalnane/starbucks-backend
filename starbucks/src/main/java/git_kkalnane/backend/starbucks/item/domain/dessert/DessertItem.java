package git_kkalnane.backend.starbucks.item.domain.dessert;

import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
import git_kkalnane.backend.starbucks.item.domain.ItemStatus;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Entity
@Table(name = "dessert_items")

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DessertItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dessert_item_name_ko", nullable = false, unique = true, length = 50)
    private String dessertItemNameKo;

    @Column(name = "dessert_item_name_en", nullable = false, unique = true, length = 50)
    private String dessertItemNameEn;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "image_url", length = 254)
    private String imageUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ItemStatus status = ItemStatus.AVAILABLE;

}
