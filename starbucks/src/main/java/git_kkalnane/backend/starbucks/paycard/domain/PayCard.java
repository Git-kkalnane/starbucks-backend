package git_kkalnane.backend.starbucks.paycard.domain;

import git_kkalnane.backend.starbucks._global.entity.BaseTimeEntity;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.paycard.common.exception.PayCardErrorCode;
import git_kkalnane.backend.starbucks.paycard.common.exception.PayCardException;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "paycards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PayCard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "card_amount", nullable = false)
    private int cardAmount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public void decreaseCardAmount(int amount) {
        if(amount < 0){
            throw new PayCardException(PayCardErrorCode.NOT_ENOUGH_PAY_CARD_AMOUNT);
        }

        if (cardAmount < amount) {
            throw new PayCardException(PayCardErrorCode.NOT_ENOUGH_PAY_CARD_AMOUNT);
        }

        this.cardAmount -= amount;
    }
}
