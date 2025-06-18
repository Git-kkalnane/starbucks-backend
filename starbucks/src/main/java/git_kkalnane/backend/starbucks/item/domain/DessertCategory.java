package git_kkalnane.backend.starbucks.item.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "dessert_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DessertCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "dessert_item_category")
    private List<DessertItemCategory> dessertItemCategory = new ArrayList<>();


}
