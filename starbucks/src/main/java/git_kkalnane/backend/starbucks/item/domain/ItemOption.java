package git_kkalnane.backend.starbucks.item.domain;

import git_kkalnane.backend.starbucks.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "item_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_name", nullable = false, length = 50)
    private String optionName;

    @Column(name = "is_required")
    private boolean isRequired = false;

    @Column(name = "display_order")
    private int displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_option_type")
    private ItemOptionType itemOptionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @OneToMany(mappedBy = "option_value", cascade = CascadeType.REMOVE)
    private List<ItemOptionValue> optionValues = new ArrayList<>();


}
