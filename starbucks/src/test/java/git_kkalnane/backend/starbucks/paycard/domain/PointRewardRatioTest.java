package git_kkalnane.backend.starbucks.paycard.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PointRewardRatio 도메인 테스트")
class PointRewardRatioTest {

    @Test
    @DisplayName("일반_결제_적립_비율_정확한_계산")
    void getAppliedPointReward_WithCommonRatio_CalculatesCorrectly() {
        // Given
        int paymentAmount = 10000;
        PointRewardRatio ratio = PointRewardRatio.COMMON;

        // When
        int result = ratio.getAppliedPointReward(paymentAmount);

        // Then
        assertThat(result).isEqualTo(500); // 10000 * 0.05 = 500
    }

    @Test
    @DisplayName("일반_결제_적립_비율_0원_결제시_0포인트")
    void getAppliedPointReward_WithZeroAmount_ReturnsZero() {
        // Given
        int paymentAmount = 0;
        PointRewardRatio ratio = PointRewardRatio.COMMON;

        // When
        int result = ratio.getAppliedPointReward(paymentAmount);

        // Then
        assertThat(result).isEqualTo(0);
    }


    @Test
    @DisplayName("일반_결제_적립_비율_큰_금액_계산")
    void getAppliedPointReward_WithLargeAmount_CalculatesCorrectly() {
        // Given
        int paymentAmount = 1000000; // 100만원
        PointRewardRatio ratio = PointRewardRatio.COMMON;

        // When
        int result = ratio.getAppliedPointReward(paymentAmount);

        // Then
        assertThat(result).isEqualTo(50000); // 1000000 * 0.05 = 50000
    }

    @Test
    @DisplayName("일반_결제_적립_비율_값_확인")
    void getCommonRatio_ReturnsCorrectValue() {
        // Given & When
        PointRewardRatio ratio = PointRewardRatio.COMMON;

        // Then
        assertThat(ratio.getRatio()).isEqualTo(0.05);
    }

    @Test
    @DisplayName("일반_결제_적립_비율_1원_결제시_0포인트")
    void getAppliedPointReward_WithOneWon_ReturnsZero() {
        // Given
        int paymentAmount = 1;
        PointRewardRatio ratio = PointRewardRatio.COMMON;

        // When
        int result = ratio.getAppliedPointReward(paymentAmount);

        // Then
        assertThat(result).isEqualTo(0); // 1 * 0.05 = 0.05 -> 0 (정수 변환)
    }

    @Test
    @DisplayName("일반_결제_적립_비율_20원_결제시_1포인트")
    void getAppliedPointReward_WithTwentyWon_ReturnsOnePoint() {
        // Given
        int paymentAmount = 20;
        PointRewardRatio ratio = PointRewardRatio.COMMON;

        // When
        int result = ratio.getAppliedPointReward(paymentAmount);

        // Then
        assertThat(result).isEqualTo(1); // 20 * 0.05 = 1
    }

    @Test
    @DisplayName("일반_결제_적립_비율_음수_결제시_음수_포인트")
    void getAppliedPointReward_WithNegativeAmount_ReturnsNegativePoints() {
        // Given
        int paymentAmount = -1000;
        PointRewardRatio ratio = PointRewardRatio.COMMON;

        // When
        int result = ratio.getAppliedPointReward(paymentAmount);

        // Then
        assertThat(result).isEqualTo(-50); // -1000 * 0.05 = -50
    }

    @Test
    @DisplayName("일반_결제_적립_비율_여러_금액_계산_정확성")
    void getAppliedPointReward_WithMultipleAmounts_CalculatesCorrectly() {
        // Given
        PointRewardRatio ratio = PointRewardRatio.COMMON;

        // When & Then
        assertThat(ratio.getAppliedPointReward(1000)).isEqualTo(50);   // 1000 * 0.05 = 50
        assertThat(ratio.getAppliedPointReward(5000)).isEqualTo(250);  // 5000 * 0.05 = 250
        assertThat(ratio.getAppliedPointReward(10000)).isEqualTo(500); // 10000 * 0.05 = 500
        assertThat(ratio.getAppliedPointReward(50000)).isEqualTo(2500); // 50000 * 0.05 = 2500
    }
} 