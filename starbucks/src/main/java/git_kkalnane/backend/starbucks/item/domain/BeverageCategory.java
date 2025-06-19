package git_kkalnane.backend.starbucks.item.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "beverage_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BeverageCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "beverage_item_category", cascade = CascadeType.REMOVE)
    private List<BeverageItemCategory> beverageItemCategory = new ArrayList<>();
}
