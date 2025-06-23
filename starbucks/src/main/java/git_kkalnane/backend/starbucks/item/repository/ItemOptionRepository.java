package git_kkalnane.backend.starbucks.item.repository;

import git_kkalnane.backend.starbucks.item.domain.ItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemOptionRepository extends JpaRepository<ItemOption, Long> {
}
