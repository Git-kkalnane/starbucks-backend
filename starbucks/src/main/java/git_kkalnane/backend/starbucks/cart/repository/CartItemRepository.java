package git_kkalnane.backend.starbucks.cart.repository;

import git_kkalnane.backend.starbucks.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
