package git_kkalnane.backend.starbucks.item.domain.beverage;

import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
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
public class BeverageCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "beverageCategory", cascade = CascadeType.REMOVE)
    private List<BeverageItemCategory> beverageItemCategory = new ArrayList<>();
}
