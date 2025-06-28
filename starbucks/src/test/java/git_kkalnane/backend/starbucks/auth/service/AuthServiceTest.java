package git_kkalnane.backend.starbucks.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import git_kkalnane.backend.starbucks._global.utils.Encryptor;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.common.jwt.JwtTokenProvider;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.JwtToken;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.TokenInfo;
import git_kkalnane.backend.starbucks.auth.domain.AccessToken;
import git_kkalnane.backend.starbucks.auth.domain.RefreshToken;
import git_kkalnane.backend.starbucks.auth.dto.LoginDto;
import git_kkalnane.backend.starbucks.auth.dto.request.LoginRequest;
import git_kkalnane.backend.starbucks.auth.repository.RefreshTokenRepository;
import git_kkalnane.backend.starbucks.member.domain.Member;
import git_kkalnane.backend.starbucks.member.repository.MemberRepository;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private Encryptor encryptor;
    private AuthService authService;

    private LoginRequest validLoginRequest;
    private Member existingMember;
    private JwtToken jwtToken;

    @BeforeEach
    void setUp() {
        encryptor = new Encryptor();
        authService = new AuthService(memberRepository, refreshTokenRepository, jwtTokenProvider, encryptor);

        String rawPassword = "password123";
        String hashedPassword = encryptor.encrypt(rawPassword);

        validLoginRequest = new LoginRequest("test@example.com", rawPassword);

        existingMember = Member.builder().name("홍길동").nickname("길동이").email("test@example.com")
                .password(hashedPassword).build();

        TokenInfo accessTokenInfo = TokenInfo.builder().token("access-token-123").expiration(new Date()).build();
        TokenInfo refreshTokenInfo = TokenInfo.builder().token("refresh-token-456").expiration(new Date()).build();

        jwtToken = JwtToken.of(accessTokenInfo, refreshTokenInfo);
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Nested
        @DisplayName("정상 시나리오")
        class SuccessTest {
            void commonWhen(LoginRequest request, Member member, JwtToken token) {
                when(memberRepository.findMemberByEmail(request.email())).thenReturn(Optional.of(member));
                when(jwtTokenProvider.createJwtToken(member.getId())).thenReturn(token);
                when(refreshTokenRepository.findByMemberId(member.getId())).thenReturn(Optional.empty());
                when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(RefreshToken.builder()
                        .memberId(member.getId()).token("test").expiration(new Date()).build());
            }

            void commonAssertion(LoginDto result, JwtToken token, Member member) {
                assertThat(result).isNotNull();
                assertThat(result.accessToken()).isEqualTo(token.getAccessTokenInfo().getToken());
                assertThat(result.refreshToken()).isEqualTo(token.getRefreshTokenInfo().getToken());
                assertThat(result.name()).isEqualTo(member.getName());
                assertThat(result.email()).isEqualTo(member.getEmail());
                assertThat(result.nickname()).isEqualTo(member.getNickname());
            }

            void commonVerify(LoginRequest request, Member member) {
                verify(memberRepository, times(1)).findMemberByEmail(request.email());
                verify(jwtTokenProvider, times(1)).createJwtToken(member.getId());
                verify(refreshTokenRepository, times(1)).findByMemberId(member.getId());
            }

            @Test
            @DisplayName("정상적인 로그인 요청 시 성공적으로 로그인한다")
            void login_Success() {
                // given
                commonWhen(validLoginRequest, existingMember, jwtToken);

                // when
                LoginDto result = authService.login(validLoginRequest);

                // then
                commonAssertion(result, jwtToken, existingMember);

                // verify
                commonVerify(validLoginRequest, existingMember);
            }

            @Test
            @DisplayName("다른 회원으로 로그인 시 올바른 정보가 반환된다")
            void login_DifferentMember() {
                // given
                Member differentMember = Member.builder().name("김철수").nickname("철수이")
                        .email("chulsoo@example.com").password(encryptor.encrypt("chulsoo123")).build();

                LoginRequest differentRequest = new LoginRequest("chulsoo@example.com", "chulsoo123");

                TokenInfo differentAccessTokenInfo =
                        TokenInfo.builder().token("different-access-token").expiration(new Date()).build();
                TokenInfo differentRefreshTokenInfo =
                        TokenInfo.builder().token("different-refresh-token").expiration(new Date()).build();
                JwtToken differentToken = JwtToken.of(differentAccessTokenInfo, differentRefreshTokenInfo);

                commonWhen(differentRequest, differentMember, differentToken);

                // when
                LoginDto result = authService.login(differentRequest);

                // then
                commonAssertion(result, differentToken, differentMember);

                // verify
                commonVerify(differentRequest, differentMember);
            }

            @Test
            @DisplayName("토큰이 올바르게 생성된다")
            void login_JwtTokenCreation() {
                // given
                commonWhen(validLoginRequest, existingMember, jwtToken);

                // when
                LoginDto result = authService.login(validLoginRequest);

                // then
                assertThat(result.accessToken()).isEqualTo("access-token-123");
                assertThat(result.refreshToken()).isEqualTo("refresh-token-456");
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {
            void commonAssertThatThrownBy(LoginRequest request, AuthErrorCode errorCode) {
                assertThatThrownBy(() -> authService.login(request)).isInstanceOf(AuthException.class)
                        .satisfies(exception -> {
                            AuthException authException = (AuthException) exception;
                            assertThat(authException.getErrorCode()).isEqualTo(errorCode);
                        });
            }

            @Test
            @DisplayName("존재하지 않는 이메일로 로그인 시 EMAIL_INVALID_EXCEPTION이 발생한다")
            void login_EmailNotFound() {
                // given
                LoginRequest invalidEmailRequest =
                        new LoginRequest("nonexistent@example.com", "password123");
                when(memberRepository.findMemberByEmail(invalidEmailRequest.email()))
                        .thenReturn(Optional.empty());

                // when & then
                commonAssertThatThrownBy(invalidEmailRequest, AuthErrorCode.EMAIL_INVALID_EXCEPTION);

                // verify
                verify(memberRepository, times(1)).findMemberByEmail(invalidEmailRequest.email());
            }

            @Test
            @DisplayName("빈 이메일로 로그인 시 EMAIL_INVALID_EXCEPTION이 발생한다")
            void login_EmptyEmail() {
                // given
                LoginRequest emptyEmailRequest = new LoginRequest("", "password123");
                when(memberRepository.findMemberByEmail(emptyEmailRequest.email()))
                        .thenReturn(Optional.empty());

                // when & then
                commonAssertThatThrownBy(emptyEmailRequest, AuthErrorCode.EMAIL_INVALID_EXCEPTION);

                // verify
                verify(memberRepository, times(1)).findMemberByEmail(emptyEmailRequest.email());
            }

            @Test
            @DisplayName("null 이메일로 로그인 시 EMAIL_INVALID_EXCEPTION이 발생한다")
            void login_NullEmail() {
                // given
                LoginRequest nullEmailRequest = new LoginRequest(null, "password123");
                when(memberRepository.findMemberByEmail(nullEmailRequest.email()))
                        .thenReturn(Optional.empty());

                // when & then
                commonAssertThatThrownBy(nullEmailRequest, AuthErrorCode.EMAIL_INVALID_EXCEPTION);

                // verify
                verify(memberRepository, times(1)).findMemberByEmail(nullEmailRequest.email());
            }

            @Test
            @DisplayName("잘못된 비밀번호로 로그인 시 PASSWORD_INVALID_EXCEPTION이 발생한다")
            void login_WrongPassword() {
                // given
                LoginRequest wrongPasswordRequest = new LoginRequest("test@example.com", "wrongpassword");
                when(memberRepository.findMemberByEmail(wrongPasswordRequest.email()))
                        .thenReturn(Optional.of(existingMember));

                // when & then
                commonAssertThatThrownBy(wrongPasswordRequest, AuthErrorCode.PASSWORD_INVALID_EXCEPTION);

                // verify
                verify(memberRepository, times(1)).findMemberByEmail(wrongPasswordRequest.email());
            }

            @Test
            @DisplayName("빈 비밀번호로 로그인 시 PASSWORD_INVALID_EXCEPTION이 발생한다")
            void login_EmptyPassword() {
                // given
                LoginRequest emptyPasswordRequest = new LoginRequest("test@example.com", "");
                when(memberRepository.findMemberByEmail(emptyPasswordRequest.email()))
                        .thenReturn(Optional.of(existingMember));

                // when & then
                commonAssertThatThrownBy(emptyPasswordRequest, AuthErrorCode.PASSWORD_INVALID_EXCEPTION);

                // verify
                verify(memberRepository, times(1)).findMemberByEmail(emptyPasswordRequest.email());
            }

            @Test
            @DisplayName("null 비밀번호로 로그인 시 PASSWORD_INVALID_EXCEPTION이 발생한다")
            void login_NullPassword() {
                // given
                LoginRequest nullPasswordRequest = new LoginRequest("test@example.com", null);
                when(memberRepository.findMemberByEmail(nullPasswordRequest.email()))
                        .thenReturn(Optional.of(existingMember));

                // when & then
                commonAssertThatThrownBy(nullPasswordRequest, AuthErrorCode.PASSWORD_INVALID_EXCEPTION);

                // verify
                verify(memberRepository, times(1)).findMemberByEmail(nullPasswordRequest.email());
            }
        }
    }

    @Nested
    @DisplayName("토큰저장 테스트")
    class SaveTokenTest {

        @Nested
        @DisplayName("RefreshToken 저장 테스트")
        class RefreshTokenSaveTest {

            @Test
            @DisplayName("새로운 RefreshToken을 저장할 수 있다")
            void saveRefreshToken_NewToken() {
                // given
                Long memberId = 1L;
                TokenInfo tokenInfo = TokenInfo.builder().token("new-refresh-token")
                        .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                when(refreshTokenRepository.findByMemberId(memberId)).thenReturn(Optional.empty());
                when(refreshTokenRepository.save(any(RefreshToken.class)))
                        .thenReturn(RefreshToken.builder().memberId(memberId).token(tokenInfo.getToken())
                                .expiration(tokenInfo.getExpiration()).build());

                // when
                authService.saveRefreshTokenToRepository(memberId, tokenInfo);

                // then
                verify(refreshTokenRepository, times(1)).findByMemberId(memberId);
                verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
            }

            @Test
            @DisplayName("기존 RefreshToken을 업데이트할 수 있다")
            void saveRefreshToken_UpdateExistingToken() {
                // given
                Long memberId = 1L;
                TokenInfo newTokenInfo = TokenInfo.builder().token("updated-refresh-token")
                        .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                RefreshToken existingToken =
                        RefreshToken.builder().memberId(memberId).token("old-refresh-token")
                                .expiration(new Date(System.currentTimeMillis() - 1000)).build();

                when(refreshTokenRepository.findByMemberId(memberId))
                        .thenReturn(Optional.of(existingToken));

                // when
                authService.saveRefreshTokenToRepository(memberId, newTokenInfo);

                // then
                verify(refreshTokenRepository, times(1)).findByMemberId(memberId);
                verify(refreshTokenRepository, times(0)).save(any(RefreshToken.class));

                // 기존 토큰이 업데이트되었는지 확인
                assertThat(existingToken.getToken()).isEqualTo(newTokenInfo.getToken());
                assertThat(existingToken.getExpiration()).isEqualTo(newTokenInfo.getExpiration());
            }

            @Test
            @DisplayName("다른 memberId의 RefreshToken을 저장할 수 있다")
            void saveRefreshToken_DifferentMemberId() {
                // given
                Long differentMemberId = 999L;
                TokenInfo tokenInfo = TokenInfo.builder().token("different-refresh-token")
                        .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                when(refreshTokenRepository.findByMemberId(differentMemberId)).thenReturn(Optional.empty());
                when(refreshTokenRepository.save(any(RefreshToken.class)))
                        .thenReturn(RefreshToken.builder().memberId(differentMemberId)
                                .token(tokenInfo.getToken()).expiration(tokenInfo.getExpiration()).build());

                // when
                authService.saveRefreshTokenToRepository(differentMemberId, tokenInfo);

                // then
                verify(refreshTokenRepository, times(1)).findByMemberId(differentMemberId);
                verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
            }
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {

        @Nested
        @DisplayName("정상 시나리오")
        class SuccessTest {
            void commonAssertion(RefreshToken refreshToken) {
                // 토큰이 빈 문자열로 초기화되었는지 확인
                assertThat(refreshToken.getToken()).isEqualTo("");

                // 만료 시간이 현재 시간으로 설정되었는지 확인 (1초 이내)
                long currentTime = System.currentTimeMillis();
                assertThat(refreshToken.getExpiration().getTime()).isBetween(currentTime - 1000,
                        currentTime + 1000);
            }

            @Test
            @DisplayName("정상적인 로그아웃 시 토큰이 초기화된다")
            void logout_Success() {
                // given
                Long memberId = 1L;
                RefreshToken refreshToken = RefreshToken.builder().memberId(memberId).token("valid-refresh-token")
                                .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                when(refreshTokenRepository.findByMemberId(memberId)).thenReturn(Optional.of(refreshToken));

                // when
                authService.logout(memberId);

                // then
                commonAssertion(refreshToken);

                // verify
                verify(refreshTokenRepository, times(1)).findByMemberId(memberId);
            }

            @Test
            @DisplayName("다른 memberId로 로그아웃할 수 있다")
            void logout_DifferentMemberId() {
                // given
                Long differentMemberId = 999L;
                RefreshToken refreshToken =
                        RefreshToken.builder().memberId(differentMemberId).token("different-refresh-token")
                                .expiration(new Date(System.currentTimeMillis() + 86400000)).build();

                when(refreshTokenRepository.findByMemberId(differentMemberId))
                        .thenReturn(Optional.of(refreshToken));

                // when
                authService.logout(differentMemberId);

                // then
                commonAssertion(refreshToken);

                // verify
                verify(refreshTokenRepository, times(1)).findByMemberId(differentMemberId);
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {

            @Test
            @DisplayName("RefreshToken이 존재하지 않을 때 TOKEN_NOT_FOUND_IN_DB 예외가 발생한다")
            void logout_RefreshTokenNotFound() {
                // given
                Long memberId = 1L;
                AccessToken accessToken = AccessToken.builder().memberId(memberId)
                        .token("valid-access-token").expiration(new Date()).build();

                when(refreshTokenRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> authService.logout(memberId)).isInstanceOf(AuthException.class)
                        .satisfies(exception -> {
                            AuthException authException = (AuthException) exception;
                            assertThat(authException.getErrorCode())
                                    .isEqualTo(AuthErrorCode.TOKEN_NOT_FOUND_IN_DB);
                        });

                // verify
                verify(refreshTokenRepository, times(1)).findByMemberId(memberId);
            }
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class VerifyTokenTest {

        @Nested
        @DisplayName("정상 시나리오")
        class SuccessTest {

            @Test
            @DisplayName("유효한 토큰으로 요청 검증 시 성공한다")
            void verifyTokenIncludedInRequest_Success() {
                // given
                String validToken = "valid-access-token";
                Long memberId = 1L;
                AccessToken accessToken = AccessToken.builder().memberId(memberId).token(validToken)
                        .expiration(new Date(System.currentTimeMillis() + 3600000)).build();

                when(jwtTokenProvider.getMemberId(validToken)).thenReturn(memberId.toString());

                // when
                Long result = authService.verifyTokenIncludedInRequest(validToken);

                // then
                assertThat(result).isEqualTo(memberId);

                // verify
                verify(jwtTokenProvider, times(1)).getMemberId(validToken);
            }

            @Test
            @DisplayName("다른 memberId의 유효한 토큰으로 요청 검증 시 성공한다")
            void verifyTokenIncludedInRequest_DifferentMemberId() {
                // given
                String validToken = "different-access-token";
                Long differentMemberId = 999L;
                AccessToken accessToken = AccessToken.builder().memberId(differentMemberId)
                        .token(validToken).expiration(new Date(System.currentTimeMillis() + 3600000)).build();

                when(jwtTokenProvider.getMemberId(validToken)).thenReturn(differentMemberId.toString());

                // when
                Long result = authService.verifyTokenIncludedInRequest(validToken);

                // then
                assertThat(result).isEqualTo(differentMemberId);

                // verify
                verify(jwtTokenProvider, times(1)).getMemberId(validToken);
            }
        }

        @Nested
        @DisplayName("예외 시나리오")
        class ExceptionTest {
            @Test
            @DisplayName("JwtTokenProvider에서 예외가 발생할 때 예외가 전파된다")
            void verifyTokenIncludedInRequest_JwtTokenProviderException() {
                // given
                String invalidToken = "invalid-token";

                when(jwtTokenProvider.getMemberId(invalidToken))
                        .thenThrow(new RuntimeException("JWT parsing failed"));

                // when & then
                assertThatThrownBy(() -> authService.verifyTokenIncludedInRequest(invalidToken))
                        .isInstanceOf(RuntimeException.class).hasMessage("JWT parsing failed");

                verify(jwtTokenProvider, times(1)).getMemberId(invalidToken);
            }

            @Test
            @DisplayName("memberId가 숫자가 아닐 때 NumberFormatException이 발생한다")
            void verifyTokenIncludedInRequest_InvalidMemberId() {
                // given
                String validToken = "valid-access-token";
                String invalidMemberId = "invalid-member-id";

                when(jwtTokenProvider.getMemberId(validToken)).thenReturn(invalidMemberId);

                // when & then
                assertThatThrownBy(() -> authService.verifyTokenIncludedInRequest(validToken))
                        .isInstanceOf(NumberFormatException.class);

                verify(jwtTokenProvider, times(1)).getMemberId(validToken);
            }
        }
    }
}
