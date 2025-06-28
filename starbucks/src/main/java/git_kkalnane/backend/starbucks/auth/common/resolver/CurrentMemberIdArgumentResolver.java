package git_kkalnane.backend.starbucks.auth.common.resolver;

import git_kkalnane.backend.starbucks.auth.common.annotation.CurrentMemberId;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.auth.common.jwt.JwtTokenProvider;
import git_kkalnane.backend.starbucks.auth.common.jwt.utils.TokenParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @CurrentMemberId 어노테이션이 붙은 파라미터에 현재 인증된 사용자의 ID를 주입하기 위한 리졸버
 */
@Component
@RequiredArgsConstructor
public class CurrentMemberIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMemberId.class) &&
               parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                 NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith(TokenParser.ACCESS_PREFIX_STRING)) {
            throw new AuthException(AuthErrorCode.MISSING_PREFIX);
        }

        String token = authorizationHeader.substring(TokenParser.ACCESS_PREFIX_STRING.length());
        String memberIdStr = jwtTokenProvider.getMemberId(token);
        
        try {
            return Long.parseLong(memberIdStr);
        } catch (NumberFormatException e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }
}
