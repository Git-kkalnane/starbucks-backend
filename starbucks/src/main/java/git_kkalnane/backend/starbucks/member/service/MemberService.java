package git_kkalnane.backend.starbucks.member.service;

import git_kkalnane.backend.starbucks.member.common.exception.MemberErrorCode;
import git_kkalnane.backend.starbucks.member.common.exception.MemberException;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.dto.request.SignUpRequest;
import git_kkalnane.backend.starbucks.member.dto.response.SignUpResponse;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    public SignUpResponse createMember(SignUpRequest request) {

        Member member = Member.of(
                request.getMemberName(),
                request.getEmail(),
                request.getPlanePassword()
        );
//      Member member = Member.from(request); // 위의 코드와 동일한 기능을 수행합니다.


        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new MemberException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
        }

        memberRepository.save(member);

        return SignUpResponse.of(
                member.getName(),
                member.getEmail()
        );
    }
}
