package git_kkalnane.backend.starbucks.auth.common.jwt.utils;

import git_kkalnane.backend.starbucks._global.utils.GlobalLogger;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import org.springframework.util.StringUtils;

public class TokenParser {

    public static final String ACCESS_PREFIX_STRING = "Bearer ";

    /**
     * HTTP 요청의 Authorization 헤더에 포함된 AccessToken의 PREFIX(Bearer)를 제거하여 반환하는 메서드이다.
     *
     * @param bearerToken PREFIX(Bearer)가 포함된 AccessToken
     * @return PREFIX(Bearer)가 제거된 AccessToken
     */
    public static String removeBearerTokenPrefix(String bearerToken) {

        GlobalLogger.info("Extract: ", bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(ACCESS_PREFIX_STRING)) {
            return bearerToken.substring(ACCESS_PREFIX_STRING.length());
        }

        throw new AuthException(AuthErrorCode.MISSING_PREFIX);
    }
}
