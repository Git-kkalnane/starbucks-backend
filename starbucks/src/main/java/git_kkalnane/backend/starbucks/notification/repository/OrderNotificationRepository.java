package git_kkalnane.backend.starbucks.notification.repository;

import git_kkalnane.backend.starbucks.notification.domain.OrderNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderNotificationRepository extends JpaRepository<OrderNotification,Long> {
}
