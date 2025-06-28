package git_kkalnane.backend.starbucks.auth.common.resolver;

import git_kkalnane.backend.starbucks.auth.common.annotation.CurrentMemberId;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.jwt.JwtTokenProvider;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentMemberIdArgumentResolverTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MethodParameter parameter;

    @Mock
    private ModelAndViewContainer mavContainer;

    @InjectMocks
    private CurrentMemberIdArgumentResolver resolver;

    private NativeWebRequest webRequest;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        webRequest = new ServletWebRequest(request);
    }

    @Test
    @DisplayName("supportsParameter - @CurrentMemberId가 있고 타입이 Long인 경우 true 반환")
    void supportsParameter_WhenAnnotationPresentAndTypeIsLong_ReturnsTrue() {
        // given
        when(parameter.hasParameterAnnotation(CurrentMemberId.class)).thenReturn(true);
        when(parameter.getParameterType()).thenReturn((Class) Long.class);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("supportsParameter - @CurrentMemberId가 없으면 false 반환")
    void supportsParameter_WhenAnnotationNotPresent_ReturnsFalse() {
        // given
        when(parameter.hasParameterAnnotation(CurrentMemberId.class)).thenReturn(false);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("supportsParameter - 타입이 Long이 아니면 false 반환")
    void supportsParameter_WhenTypeIsNotLong_ReturnsFalse() {
        // given
        when(parameter.hasParameterAnnotation(CurrentMemberId.class)).thenReturn(true);
        when(parameter.getParameterType()).thenReturn((Class) String.class);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("resolveArgument - 유효한 토큰인 경우 memberId 반환")
    void resolveArgument_WithValidToken_ReturnsMemberId() {
        // given
        String validToken = "valid.token.here";
        String expectedMemberId = "1";
        
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + validToken);
        when(jwtTokenProvider.getMemberId(validToken)).thenReturn(expectedMemberId);

        // when
        Long result = (Long) resolver.resolveArgument(
                parameter, mavContainer, webRequest, null);

        // then
        assertEquals(1L, result);
    }

    @Test
    @DisplayName("resolveArgument - Authorization 헤더가 없는 경우 예외 발생")
    void resolveArgument_WhenNoAuthHeader_ThrowsException() {
        // given - No Authorization header set
        
        // when & then
        AuthException exception = assertThrows(AuthException.class, () ->
            resolver.resolveArgument(parameter, mavContainer, webRequest, null)
        );
        
        assertEquals(AuthErrorCode.MISSING_PREFIX, exception.getErrorCode());
    }

    @Test
    @DisplayName("resolveArgument - Bearer 접두사가 없는 경우 예외 발생")
    void resolveArgument_WhenNoBearerPrefix_ThrowsException() {
        // given
        request.addHeader(HttpHeaders.AUTHORIZATION, "InvalidToken");

        // when & then
        AuthException exception = assertThrows(AuthException.class, () ->
            resolver.resolveArgument(parameter, mavContainer, webRequest, null)
        );
        
        assertEquals(AuthErrorCode.MISSING_PREFIX, exception.getErrorCode());
    }

    @Test
    @DisplayName("resolveArgument - 토큰이 유효하지 않은 경우 예외 발생")
    void resolveArgument_WhenTokenIsInvalid_ThrowsException() {
        // given
        String invalidToken = "invalid.token";
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken);
        
        when(jwtTokenProvider.getMemberId(invalidToken))
            .thenThrow(new AuthException(AuthErrorCode.INVALID_TOKEN));

        // when & then
        assertThrows(AuthException.class, () ->
            resolver.resolveArgument(parameter, mavContainer, webRequest, null)
        );
    }
}
