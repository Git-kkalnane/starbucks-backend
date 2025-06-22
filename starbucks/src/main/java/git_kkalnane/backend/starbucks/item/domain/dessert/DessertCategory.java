package git_kkalnane.backend.starbucks.item.domain.dessert;

import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
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
public class DessertCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "dessertCategory")
    private List<DessertItemCategory> dessertItemCategory = new ArrayList<>();


}
