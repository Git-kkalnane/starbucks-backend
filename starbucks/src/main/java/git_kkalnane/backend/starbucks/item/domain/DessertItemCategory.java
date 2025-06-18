package git_kkalnane.backend.starbucks.item.domain;

import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dessert_item_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DessertItemCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id")
    private ItemCategory itemCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_category_id")
    private DessertCategory dessertCategory;


}
