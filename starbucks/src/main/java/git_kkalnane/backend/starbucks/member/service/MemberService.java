package git_kkalnane.backend.starbucks.member.service;

import git_kkalnane.backend.starbucks._global.utils.Encryptor;
import git_kkalnane.backend.starbucks.member.common.exception.MemberErrorCode;
import git_kkalnane.backend.starbucks.member.common.exception.MemberException;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.dto.request.SignUpRequest;
import git_kkalnane.backend.starbucks.member.dto.request.UpdateNicknameRequest;
import git_kkalnane.backend.starbucks.member.dto.request.UpdatePasswordRequest;
import git_kkalnane.backend.starbucks.member.dto.response.MemberDetailInfo;
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

        validateDuplicatedEmail(request.email());

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

    /**
     * 사용자가 입력한 이메일이 이미 존재하는지 검증하는 메서드
     *
     * @param email 회원가입 요청에 포함된 가입자의 이메일
     */
    private void validateDuplicatedEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new MemberException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    /**
     * 매개변수로 들어온 멤버 엔티티 식별자(ID)를 바탕으로 사용자 정보를 DB에서 찾아내어 반환하는 메서드
     *
     * @param memberId 사용자 엔티티 식별자 (ID)
     * @return {@link MemberDetailInfo} 객체
     */
    public MemberDetailInfo getMemberDetailInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        return MemberDetailInfo.of(member.getName(), member.getEmail(), member.getNickname());
    }

    /**
     * 매개변수로 들어온 멤버 엔티티 식별자(ID)를 바탕으로 멤버의 닉네임을 변경하는 메서드
     *
     * @param memberId 사용자 엔티티 식별자 (ID)
     * @param request
     */
    @Transactional
    public void updateNickname(Long memberId, UpdateNicknameRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.modifyNickname(request.nickname());
    }

}
