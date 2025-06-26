package git_kkalnane.backend.starbucks.order.repository;

import git_kkalnane.backend.starbucks.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
