package git_kkalnane.backend.starbucks.order.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * 주문번호 생성을 위한 Entity파일을 작성하였습니다.
 *
 */

@Entity
@Table(name = "order_daily_counters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDailyCounter {

    @Id
    private LocalDate date;

    @Column(name = "count", nullable = false)
    private int count;

    public void increment() {
        this.count++;
    }

    public OrderDailyCounter(LocalDate date, int count) {
        this.date = date;
        this.count = count;
    }
}
