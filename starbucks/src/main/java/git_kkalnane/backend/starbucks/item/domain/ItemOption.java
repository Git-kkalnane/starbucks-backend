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

    @Column(name = "syrup_name", nullable = false, length = 10)
    private String surupName;

    @Column(name = "is_required")
    private boolean isRequired = false;

    @Column(name = "display_order")
    private int displayOrder;

    @Column(name = "additonal_price", nullable = false)
    private int additonalPrice;

}
