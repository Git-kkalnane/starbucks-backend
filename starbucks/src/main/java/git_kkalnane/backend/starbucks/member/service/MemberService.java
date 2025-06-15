package git_kkalnane.backend.starbucks.member.service;

import git_kkalnane.backend.starbucks.member.common.exception.MemberErrorCode;
import git_kkalnane.backend.starbucks.member.common.exception.MemberException;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.dto.request.SignInRequest;
import git_kkalnane.backend.starbucks.member.dto.request.SignUpRequest;
import git_kkalnane.backend.starbucks.member.dto.request.UpdateMemberRequest;
import git_kkalnane.backend.starbucks.member.dto.response.MemberResponse;
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
    // config에서 PasswordEncoder를 빈으로 등록하고 주입 필요
    //    @Bean
    //    public PasswordEncoder passwordEncoder() {
    //        return new BCryptPasswordEncoder();
    //    }
    // private final PasswordEncoder passwordEncoder;

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

    /**
     * 로그인
     */
    public String login(SignInRequest request) {
        Member member = memberRepository.findMemberByEmail(request.getEmail())
            .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new MemberException(MemberErrorCode.INVALID_PASSWORD);
        }

        // JWT 토큰 생성 로직 merge 시 적용
        //return jwtTokenProvider.createToken(member.getEmail());
        return "";
    }

    /**
     * 회원 정보 조회
     */
    public MemberResponse getMyInfo(String email) {
        Member member = memberRepository.findMemberByEmail(email)
            .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        return MemberResponse.of(member.getId(), member.getName(), member.getEmail());
    }

    /**
     * 회원 정보 수정
     */
    @Transactional
    public void updateMember(String email, UpdateMemberRequest request) {
        Member member = memberRepository.findMemberByEmail(email)
            .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (request.getMemberName() != null && !request.getMemberName().isBlank()) {
            member.updateName(request.getMemberName());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            member.updatePassword(encodedPassword);
        }
    }

    /**
     * 회원 탈퇴
     * Hard delete 방식으로 회원 정보를 삭제합니다.
     */
    @Transactional
    public void deleteMember(String email) {
        Member member = memberRepository.findMemberByEmail(email)
            .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        memberRepository.delete(member);
    }
}
