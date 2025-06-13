package git_kkalnane.backend.starbucks.item.domain;

import git_kkalnane.backend.starbucks.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "item_categories")
public class ItemCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name_ko", nullable = false, unique = true)
    private String categoryNameKo;

    @Column(name = "name_en", nullable = false, unique = true)
    private String categoryNameEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private ItemCategory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private List<ItemCategory> children;

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE)
    private List<Item> items = new ArrayList<>();
}
