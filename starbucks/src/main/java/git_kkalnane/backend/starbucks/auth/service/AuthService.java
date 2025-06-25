package git_kkalnane.backend.starbucks.auth.service;

import git_kkalnane.backend.starbucks._global.utils.Encryptor;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.common.jwt.JwtToken;
import git_kkalnane.backend.starbucks.auth.common.jwt.JwtTokenProvider;
import git_kkalnane.backend.starbucks.auth.dto.LoginDto;
import git_kkalnane.backend.starbucks.auth.dto.UserInfo;
import git_kkalnane.backend.starbucks.auth.dto.request.LoginRequest;
import git_kkalnane.backend.starbucks.auth.repository.AccessTokenRepository;
import git_kkalnane.backend.starbucks.auth.repository.RefreshTokenRepository;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final Encryptor encryptor;

    /**
     * LoginRequest를 바탕으로 토큰을 발행한 뒤 토큰과 사용자 정보를 반환하는 메서드
     *
     * @param request SignUpRequest 객체
     * @return 멤버의 이름을 담은 SignUpResponse 객체
     */
    public LoginDto login(LoginRequest request) {

        // 로그인 요청에 포함된 이메일을 가진 멤버가 존재하는지 조회
        // 이 과정에서 이메일이 존재하지 않을 경우 예외가 발생한다.
        Member member = memberRepository.findMemberByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.EMAIL_INVALID_EXCEPTION));

        // 로그인 요청에 포함된 비밀번호가 DB에 저장된 비밀번호와 일치하는지 검증
        if (!encryptor.isMatch(request.password(), member.getPassword())) {
            throw new AuthException(AuthErrorCode.PASSWORD_INVALID_EXCEPTION);
        }

        JwtToken token = jwtTokenProvider.createJwtToken(member.getId());
        UserInfo userInfo = UserInfo.of(member.getEmail(), member.getNickname());

        return LoginDto.of(token, userInfo);
    }

//    public AccessToken verifyToken(String accessToken) {
//        String memberId = jwtTokenProvider.getMemberId(accessToken);
//
//        return accessTokenRepository.findByMemberId(Long.parseLong(memberId));
//    }
}
