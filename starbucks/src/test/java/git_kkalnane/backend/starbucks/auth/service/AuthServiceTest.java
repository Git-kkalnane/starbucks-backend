package git_kkalnane.backend.starbucks.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import git_kkalnane.backend.starbucks.auth.dto.UserInfo;
import git_kkalnane.backend.starbucks.auth.dto.request.LoginRequest;
import git_kkalnane.backend.starbucks.auth.repository.AccessTokenRepository;
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
  private AccessTokenRepository accessTokenRepository;

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
    authService = new AuthService(memberRepository, accessTokenRepository, refreshTokenRepository,
        jwtTokenProvider, encryptor);

    String rawPassword = "password123";
    String hashedPassword = encryptor.encrypt(rawPassword);

    validLoginRequest = new LoginRequest("test@example.com", rawPassword);

    existingMember = Member.builder().name("홍길동").nickname("길동이").email("test@example.com")
        .password(hashedPassword).build();

    TokenInfo accessTokenInfo =
        TokenInfo.builder().token("access-token-123").expiration(new Date()).build();
    TokenInfo refreshTokenInfo =
        TokenInfo.builder().token("refresh-token-456").expiration(new Date()).build();

    jwtToken = JwtToken.of(accessTokenInfo, refreshTokenInfo);
  }

  @Nested
  @DisplayName("정상 시나리오")
  class SuccessTest {

    @Test
    @DisplayName("정상적인 로그인 요청 시 성공적으로 로그인한다")
    void login_Success() {
      // given
      when(memberRepository.findMemberByEmail(validLoginRequest.email()))
          .thenReturn(Optional.of(existingMember));
      when(jwtTokenProvider.createJwtToken(existingMember.getId())).thenReturn(jwtToken);
      when(accessTokenRepository.findByMemberId(existingMember.getId()))
          .thenReturn(Optional.empty());
      when(refreshTokenRepository.findByMemberId(existingMember.getId()))
          .thenReturn(Optional.empty());
      when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(AccessToken.builder()
          .memberId(existingMember.getId()).token("test").expiration(new Date()).build());
      when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(RefreshToken.builder()
          .memberId(existingMember.getId()).token("test").expiration(new Date()).build());

      // when
      LoginDto result = authService.login(validLoginRequest);

      // then
      assertThat(result).isNotNull();
      assertThat(result.token()).isEqualTo(jwtToken);
      assertThat(result.userInfo().email()).isEqualTo(existingMember.getEmail());
      assertThat(result.userInfo().nickname()).isEqualTo(existingMember.getNickname());

      // verify
      verify(memberRepository, times(1)).findMemberByEmail(validLoginRequest.email());
      verify(jwtTokenProvider, times(1)).createJwtToken(existingMember.getId());
      verify(accessTokenRepository, times(1)).findByMemberId(existingMember.getId());
      verify(refreshTokenRepository, times(1)).findByMemberId(existingMember.getId());
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

      when(memberRepository.findMemberByEmail(differentRequest.email()))
          .thenReturn(Optional.of(differentMember));
      when(jwtTokenProvider.createJwtToken(differentMember.getId())).thenReturn(differentToken);
      when(accessTokenRepository.findByMemberId(differentMember.getId()))
          .thenReturn(Optional.empty());
      when(refreshTokenRepository.findByMemberId(differentMember.getId()))
          .thenReturn(Optional.empty());
      when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(AccessToken.builder()
          .memberId(differentMember.getId()).token("test").expiration(new Date()).build());
      when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(RefreshToken.builder()
          .memberId(differentMember.getId()).token("test").expiration(new Date()).build());

      // when
      LoginDto result = authService.login(differentRequest);

      // then
      assertThat(result).isNotNull();
      assertThat(result.token()).isEqualTo(differentToken);
      assertThat(result.userInfo().email()).isEqualTo(differentMember.getEmail());
      assertThat(result.userInfo().nickname()).isEqualTo(differentMember.getNickname());

      verify(memberRepository, times(1)).findMemberByEmail(differentRequest.email());
      verify(jwtTokenProvider, times(1)).createJwtToken(differentMember.getId());
    }

    @Test
    @DisplayName("UserInfo가 올바른 정보로 생성된다")
    void login_UserInfoCreation() {
      // given
      when(memberRepository.findMemberByEmail(validLoginRequest.email()))
          .thenReturn(Optional.of(existingMember));
      when(jwtTokenProvider.createJwtToken(existingMember.getId())).thenReturn(jwtToken);
      when(accessTokenRepository.findByMemberId(existingMember.getId()))
          .thenReturn(Optional.empty());
      when(refreshTokenRepository.findByMemberId(existingMember.getId()))
          .thenReturn(Optional.empty());
      when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(AccessToken.builder()
          .memberId(existingMember.getId()).token("test").expiration(new Date()).build());
      when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(RefreshToken.builder()
          .memberId(existingMember.getId()).token("test").expiration(new Date()).build());

      // when
      LoginDto result = authService.login(validLoginRequest);

      // then
      UserInfo userInfo = result.userInfo();
      assertThat(userInfo.email()).isEqualTo(existingMember.getEmail());
      assertThat(userInfo.nickname()).isEqualTo(existingMember.getNickname());
    }

    @Test
    @DisplayName("JwtToken이 올바르게 생성된다")
    void login_JwtTokenCreation() {
      // given
      when(memberRepository.findMemberByEmail(validLoginRequest.email()))
          .thenReturn(Optional.of(existingMember));
      when(jwtTokenProvider.createJwtToken(existingMember.getId())).thenReturn(jwtToken);
      when(accessTokenRepository.findByMemberId(existingMember.getId()))
          .thenReturn(Optional.empty());
      when(refreshTokenRepository.findByMemberId(existingMember.getId()))
          .thenReturn(Optional.empty());
      when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(AccessToken.builder()
          .memberId(existingMember.getId()).token("test").expiration(new Date()).build());
      when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(RefreshToken.builder()
          .memberId(existingMember.getId()).token("test").expiration(new Date()).build());

      // when
      LoginDto result = authService.login(validLoginRequest);

      // then
      JwtToken resultToken = result.token();
      assertThat(resultToken).isEqualTo(jwtToken);
      assertThat(resultToken.getAccessTokenInfo().getToken()).isEqualTo("access-token-123");
      assertThat(resultToken.getRefreshTokenInfo().getToken()).isEqualTo("refresh-token-456");
    }
  }

  @Nested
  @DisplayName("예외 시나리오")
  class ExceptionTest {

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 EMAIL_INVALID_EXCEPTION이 발생한다")
    void login_EmailNotFound() {
      // given
      LoginRequest invalidEmailRequest = new LoginRequest("nonexistent@example.com", "password123");
      when(memberRepository.findMemberByEmail(invalidEmailRequest.email()))
          .thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> authService.login(invalidEmailRequest))
          .isInstanceOf(AuthException.class).satisfies(exception -> {
            AuthException authException = (AuthException) exception;
            assertThat(authException.getErrorCode())
                .isEqualTo(AuthErrorCode.EMAIL_INVALID_EXCEPTION);
          });

      verify(memberRepository, times(1)).findMemberByEmail(invalidEmailRequest.email());
      verify(jwtTokenProvider, times(0)).createJwtToken(anyLong());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 PASSWORD_INVALID_EXCEPTION이 발생한다")
    void login_WrongPassword() {
      // given
      LoginRequest wrongPasswordRequest = new LoginRequest("test@example.com", "wrongpassword");
      when(memberRepository.findMemberByEmail(wrongPasswordRequest.email()))
          .thenReturn(Optional.of(existingMember));

      // when & then
      assertThatThrownBy(() -> authService.login(wrongPasswordRequest))
          .isInstanceOf(AuthException.class).satisfies(exception -> {
            AuthException authException = (AuthException) exception;
            assertThat(authException.getErrorCode())
                .isEqualTo(AuthErrorCode.PASSWORD_INVALID_EXCEPTION);
          });

      verify(memberRepository, times(1)).findMemberByEmail(wrongPasswordRequest.email());
      verify(jwtTokenProvider, times(0)).createJwtToken(anyLong());
    }

    @Test
    @DisplayName("빈 비밀번호로 로그인 시 PASSWORD_INVALID_EXCEPTION이 발생한다")
    void login_EmptyPassword() {
      // given
      LoginRequest emptyPasswordRequest = new LoginRequest("test@example.com", "");
      when(memberRepository.findMemberByEmail(emptyPasswordRequest.email()))
          .thenReturn(Optional.of(existingMember));

      // when & then
      assertThatThrownBy(() -> authService.login(emptyPasswordRequest))
          .isInstanceOf(AuthException.class).satisfies(exception -> {
            AuthException authException = (AuthException) exception;
            assertThat(authException.getErrorCode())
                .isEqualTo(AuthErrorCode.PASSWORD_INVALID_EXCEPTION);
          });

      verify(memberRepository, times(1)).findMemberByEmail(emptyPasswordRequest.email());
      verify(jwtTokenProvider, times(0)).createJwtToken(anyLong());
    }

    @Test
    @DisplayName("null 비밀번호로 로그인 시 PASSWORD_INVALID_EXCEPTION이 발생한다")
    void login_NullPassword() {
      // given
      LoginRequest nullPasswordRequest = new LoginRequest("test@example.com", null);
      when(memberRepository.findMemberByEmail(nullPasswordRequest.email()))
          .thenReturn(Optional.of(existingMember));

      // when & then
      assertThatThrownBy(() -> authService.login(nullPasswordRequest))
          .isInstanceOf(AuthException.class).satisfies(exception -> {
            AuthException authException = (AuthException) exception;
            assertThat(authException.getErrorCode())
                .isEqualTo(AuthErrorCode.PASSWORD_INVALID_EXCEPTION);
          });

      verify(memberRepository, times(1)).findMemberByEmail(nullPasswordRequest.email());
      verify(jwtTokenProvider, times(0)).createJwtToken(anyLong());
    }

    @Test
    @DisplayName("빈 이메일로 로그인 시 EMAIL_INVALID_EXCEPTION이 발생한다")
    void login_EmptyEmail() {
      // given
      LoginRequest emptyEmailRequest = new LoginRequest("", "password123");
      when(memberRepository.findMemberByEmail(emptyEmailRequest.email()))
          .thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> authService.login(emptyEmailRequest))
          .isInstanceOf(AuthException.class).satisfies(exception -> {
            AuthException authException = (AuthException) exception;
            assertThat(authException.getErrorCode())
                .isEqualTo(AuthErrorCode.EMAIL_INVALID_EXCEPTION);
          });

      verify(memberRepository, times(1)).findMemberByEmail(emptyEmailRequest.email());
      verify(jwtTokenProvider, times(0)).createJwtToken(anyLong());
    }

    @Test
    @DisplayName("null 이메일로 로그인 시 EMAIL_INVALID_EXCEPTION이 발생한다")
    void login_NullEmail() {
      // given
      LoginRequest nullEmailRequest = new LoginRequest(null, "password123");
      when(memberRepository.findMemberByEmail(nullEmailRequest.email()))
          .thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> authService.login(nullEmailRequest))
          .isInstanceOf(AuthException.class).satisfies(exception -> {
            AuthException authException = (AuthException) exception;
            assertThat(authException.getErrorCode())
                .isEqualTo(AuthErrorCode.EMAIL_INVALID_EXCEPTION);
          });

      verify(memberRepository, times(1)).findMemberByEmail(nullEmailRequest.email());
      verify(jwtTokenProvider, times(0)).createJwtToken(anyLong());
    }

    @Test
    @DisplayName("JwtTokenProvider 실패 시 예외가 발생한다")
    void login_JwtTokenProviderFailure() {
      // given
      when(memberRepository.findMemberByEmail(validLoginRequest.email()))
          .thenReturn(Optional.of(existingMember));
      when(jwtTokenProvider.createJwtToken(existingMember.getId()))
          .thenThrow(new RuntimeException("JWT creation failed"));

      // when & then
      assertThatThrownBy(() -> authService.login(validLoginRequest))
          .isInstanceOf(RuntimeException.class).hasMessage("JWT creation failed");

      verify(memberRepository, times(1)).findMemberByEmail(validLoginRequest.email());
      verify(jwtTokenProvider, times(1)).createJwtToken(existingMember.getId());
    }
  }
}
