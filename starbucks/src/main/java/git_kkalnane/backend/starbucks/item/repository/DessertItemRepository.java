package git_kkalnane.backend.starbucks.item.repository;

import git_kkalnane.backend.starbucks.item.domain.dessert.DessertItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DessertItemRepository extends JpaRepository<DessertItem, Long> {
}
