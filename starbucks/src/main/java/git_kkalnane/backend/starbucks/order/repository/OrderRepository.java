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

    // 고객의 과거 주문 내역 조회용
    Page<Order> findByMemberIdAndOrderStatusIn(Long memberId, Collection<OrderStatus> statuses, Pageable pageable);
    // 고객의 현재 주문 내역 조회용
    List<Order> findByMemberIdAndOrderStatusInOrderByCreatedAtAsc(Long memberId, List<OrderStatus> statuses);
    // 매장 주문 목록 조회용
    List<Order> findByStoreIdAndOrderStatusInOrderByCreatedAtAsc(Long storeId, List<OrderStatus> statuses);

}
