package git_kkalnane.backend.starbucks.global.config;

import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.domain.AuthInfo;
import git_kkalnane.backend.starbucks.auth.service.AuthService;
import git_kkalnane.backend.starbucks.global.utils.GlobalLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authInfoService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        GlobalLogger.info("[Intercept] 요청 경로 정보:",request.getMethod(),request.getRequestURI());

//      TODO: 기능 구현 시 아래 코드 활성화
//        String accessToken = extractToken(request);
//        AuthInfo authInfo = authInfoService.verify(accessToken);

        Long memberId =  1L; // authInfo.getMemberId();

        request.setAttribute("memberId", memberId);

        GlobalLogger.info("[Intercept] memberId: ",memberId);
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        throw new AuthException(AuthErrorCode.INVALID_TOKEN);
    }
}
