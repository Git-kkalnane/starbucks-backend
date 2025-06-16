package git_kkalnane.backend.starbucks.auth.common.jwt;

import git_kkalnane.backend.starbucks.auth.common.exception.AuthErrorCode;
import git_kkalnane.backend.starbucks.auth.common.exception.AuthException;
import git_kkalnane.backend.starbucks.global.utils.GlobalLogger;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

@Getter
@ToString
public class JwtToken {
    private final String tokenType;
    private final String accessToken;
    private String refreshToken;

    public JwtToken(String accessToken, String refreshToken) {
        this.tokenType = "Bearer";
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static String extractToken(String bearerToken) {
        GlobalLogger.info("Extract: ",bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        throw new AuthException(AuthErrorCode.INVALID_TOKEN);
    }

    public static JwtToken of(String accessToken, String refreshToken){
        return new JwtToken(accessToken,refreshToken);
    }

    public void setRefreshToken(String refreshToken){
        this.refreshToken=refreshToken;
    }
    public boolean refreshTokenIsExists(){return this.refreshToken != null;}
}
