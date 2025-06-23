package git_kkalnane.backend.starbucks.notification.repository;

import git_kkalnane.backend.starbucks.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
}
