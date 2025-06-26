    package git_kkalnane.backend.starbucks.item.domain;

    import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
    import git_kkalnane.backend.starbucks.item.domain.beverage.BeverageItemCategory;
    import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItemCategory;
    import jakarta.persistence.*;
    import lombok.AccessLevel;
    import lombok.Getter;
    import lombok.NoArgsConstructor;

    import java.util.ArrayList;
    import java.util.List;

    @Getter
    @Entity
    @Table(name = "item_categories")
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public class ItemCategory extends BaseTimeEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "category_name_ko", nullable = false, unique = true)
        private String categoryNameKo;

        @Column(name = "category_name_en", nullable = false, unique = true)
        private String categoryNameEn;

        @OneToMany(mappedBy = "itemCategory", cascade = CascadeType.REMOVE)
        private List<BeverageItemCategory> beverageItemCategory = new ArrayList<>();

        @OneToMany(mappedBy = "itemCategory", cascade = CascadeType.REMOVE)
        private List<DessertItemCategory> dessertItemCategory = new ArrayList<>();

    }
