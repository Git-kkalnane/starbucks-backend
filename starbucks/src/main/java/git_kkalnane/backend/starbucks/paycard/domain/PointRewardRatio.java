package git_kkalnane.backend.starbucks.paycard.domain;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointRewardRatio {
    COMMON(0.05), // 일반 결제 적립 비율

    ;

    private final double ratio;

    public int getAppliedPointReward(int amount) {
        return amount * (int) (ratio * 100);
    }
}
