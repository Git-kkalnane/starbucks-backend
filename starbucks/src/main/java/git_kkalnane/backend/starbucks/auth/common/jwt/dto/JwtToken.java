package git_kkalnane.backend.starbucks.auth.common.jwt.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class JwtToken {

    private final TokenInfo accessTokenInfo;
    private final TokenInfo refreshTokenInfo;

    public static JwtToken of(TokenInfo accessTokenInfo, TokenInfo refreshTokenInfo) {
        return new JwtToken(accessTokenInfo, refreshTokenInfo);
    }
}
