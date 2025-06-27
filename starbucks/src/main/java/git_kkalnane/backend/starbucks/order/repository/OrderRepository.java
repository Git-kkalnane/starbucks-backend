package git_kkalnane.backend.starbucks.order.repository;

import git_kkalnane.backend.starbucks.order.domain.Order;
import git_kkalnane.backend.starbucks.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByMemberIdAndOrderStatusIn(Long memberId, Collection<OrderStatus> statuses, Pageable pageable);
    List<Order> findByMemberIdAndOrderStatusInOrderByCreatedAtAsc(Long memberId, List<OrderStatus> statuses);
}
