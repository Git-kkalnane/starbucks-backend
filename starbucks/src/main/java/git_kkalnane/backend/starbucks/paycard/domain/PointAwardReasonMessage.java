package git_kkalnane.backend.starbucks.paycard.domain;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointAwardReasonMessage {
    PAYMENT("결제 포인트 적립"),

    ;

    private final String description;
}
