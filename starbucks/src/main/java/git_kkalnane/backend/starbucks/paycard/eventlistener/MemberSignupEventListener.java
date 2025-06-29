package git_kkalnane.backend.starbucks.paycard.eventlistener;

import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.event.MemberSignedUpEvent;
import git_kkalnane.backend.starbucks.paycard.service.PayCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import git_kkalnane.backend.starbucks.paycard.common.exception.PayCardErrorCode;
import git_kkalnane.backend.starbucks.paycard.common.exception.PayCardException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSignupEventListener {

    private final PayCardService payCardService;

    /**
     * 회원가입 완료 후 PayCard를 생성하는 이벤트 리스너
     * 트랜잭션이 성공적으로 커밋된 후에 실행됩니다.
     *
     * @param event 회원가입 완료 이벤트
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMPLETION,
        classes = MemberSignedUpEvent.class
    )
    public void handleMemberSignedUpEvent(MemberSignedUpEvent event) {
        Member member = event.getMember();
        try {
            payCardService.createPayCard(member);
            log.info( "PayCard 생성 - 회원: {}", member.getEmail());
        } catch (PayCardException e) {
            if (e.getErrorCode() == PayCardErrorCode.PAY_CARD_ALREADY_EXISTS) {
                log.warn("PayCard 이미 존재 - 회원 : {}", member.getEmail());
            } else {
                log.error("PayCard 생성 실패 - 회원: {}. 오류: {}", member.getEmail(), e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("PayCard 생성 중 예상치 못한 오류가 발생했습니다. 회원: {}", member.getEmail(), e);
        }
    }
}
