package git_kkalnane.backend.starbucks.item.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "beverage_item_syrup")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BeverageItemSyrup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_item_syrup_id")
    private BeverageItemSyrup beverageItemSyrup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_item")
    private BeverageItem beverageItem;

}
