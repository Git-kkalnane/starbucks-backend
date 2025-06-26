package git_kkalnane.backend.starbucks.order.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 주문번호 생성을 위한 Entity파일을 작성하였습니다.
 *
 */

@Entity
@Table(name = "order_daily_counters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDailyCounter {

    @EmbeddedId
    private OrderDailyCounterId id;

    @Column(name = "count", nullable = false)
    private int count;

    public void increment() {
        this.count++;
    }

    public OrderDailyCounter(OrderDailyCounterId id, int count) {
        this.id = id;
        this.count = count;
    }
}
