package git_kkalnane.backend.starbucks.order.repository;

import git_kkalnane.backend.starbucks.order.domain.OrderDailyCounterId;
import git_kkalnane.backend.starbucks.order.domain.OrderDailyCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface OrderDailyCounterRepository extends JpaRepository<OrderDailyCounter, LocalDate> {
    Optional<OrderDailyCounter> findById(OrderDailyCounterId id);
}
