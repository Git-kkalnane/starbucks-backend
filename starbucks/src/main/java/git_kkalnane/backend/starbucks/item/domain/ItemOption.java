package git_kkalnane.backend.starbucks.item.domain;

import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
import git_kkalnane.backend.starbucks.item.domain.beverage.CartItemOption;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "item_options")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "syrup_name", nullable = false, length = 10)
    private String syrupName;

    @Builder.Default
    @Column(name = "is_required")
    private boolean isRequired = false;

    @Column(name = "display_order")
    private int displayOrder;

    @Column(name = "additional_price")
    private int additionalPrice;

    @Builder.Default
    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Builder.Default
    @OneToMany(mappedBy = "itemOption", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CartItemOption> cartItemOption = new ArrayList<>();

}
