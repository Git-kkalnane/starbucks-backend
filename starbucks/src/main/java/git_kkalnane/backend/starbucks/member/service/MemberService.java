package git_kkalnane.backend.starbucks.member.service;

import git_kkalnane.backend.starbucks._global.utils.Encryptor;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.dto.request.SignUpRequest;
import git_kkalnane.backend.starbucks.member.dto.response.SignUpResponse;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final Encryptor encryptor;

    /**
     * SignUpRequest를 바탕으로 DB에 회원정보를 저장하는 메서드
     *
     * @param request SignUpRequest 객체
     * @return 멤버의 이름을 담은 SignUpResponse 객체
     */
    @Transactional
    public SignUpResponse createMember(SignUpRequest request) {

        String encryptedPassword = encryptor.encrypt(request.password());

        Member member = Member.builder()
                .name(request.name())
                .nickname(request.nickname())
                .email(request.email())
                .password(encryptedPassword)
                .build();

        memberRepository.save(member);

        return new SignUpResponse(member.getName());
    }
}
