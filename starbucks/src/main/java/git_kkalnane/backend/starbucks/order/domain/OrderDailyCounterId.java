package git_kkalnane.backend.starbucks.order.domain;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class OrderDailyCounterId implements Serializable {
    private LocalDate date;
    private Long storeId;
}
