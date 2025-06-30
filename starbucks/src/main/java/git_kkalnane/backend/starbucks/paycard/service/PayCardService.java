package git_kkalnane.backend.starbucks.paycard.service;


import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.paycard.domain.PayCard;
import git_kkalnane.backend.starbucks.paycard.repository.PayCardRepository;
import git_kkalnane.backend.starbucks.paycard.common.exception.PayCardErrorCode;
import git_kkalnane.backend.starbucks.paycard.common.exception.PayCardException;
import git_kkalnane.backend.starbucks.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class PayCardService {

  private final PayCardRepository payCardRepository;

  @Value("${pay-card.init-amount:50000}")
  private int initPayCardAmount;


  /**
   * 새로운 PayCard를 생성하고 초기 잔액을 설정합니다.
   * 한 회원당 하나의 PayCard만 가질 수 있습니다.
   *
   * @param member PayCard를 생성할 회원
   * @return 생성된 PayCard 객체
   * @throws PayCardException 이미 PayCard가 존재하는 경우 발생
   */
  @Transactional
  public PayCard createPayCard(Member member) {
    // 이미 PayCard가 있는지 확인
    if (payCardRepository.existsByMemberId(member.getId())) {
      throw new PayCardException(PayCardErrorCode.PAY_CARD_ALREADY_EXISTS);
    }
    
    // 16자리 카드 번호 생성
    String cardNumber = generateCardNumber();

    // 초기 잔액으로 PayCard 생성 및 저장
    PayCard payCard = PayCard.builder()
        .cardNumber(cardNumber)
        .cardAmount(initPayCardAmount)
        .member(member)
        .build();

    return payCardRepository.save(payCard);
  }

  /**
   * 결제 시 카드 잔액 차감 로직 결제 시 카드 잔액을 차감하는 로직을 수행합니다.
   *
   * @param payment 결제 정보
   * @return 차감된 PayCard 정보
   */
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public PayCard pay(Payment payment) {
    PayCard payCard = payCardRepository.getByMemberId(payment.getMember().getId());

    payCard.decreaseCardAmount(payment.getAmountPaidByPoint());

    return payCard;
  }


  /**
   * 16자리 랜덤 카드 번호를 생성합니다.
   *
   * @return 16자리 카드 번호 문자열 (XXXX XXXX XXXX XXXX 형식)
   */
  private String generateCardNumber() {
    // 랜덤 16자리 숫자 생성
    Random random = new Random();
    StringBuilder sb = new StringBuilder();

    // 4자리씩 4그룹으로 나누어 생성
    for (int i = 0; i < 4; i++) {
      if (i > 0) {
        sb.append(" ");  // 그룹 사이에 공백 추가
      }
      // 4자리 숫자 생성 (필요 시 앞에 0으로 채움)
      sb.append(String.format("%04d", random.nextInt(10000)));
    }

    return sb.toString();
  }
}
