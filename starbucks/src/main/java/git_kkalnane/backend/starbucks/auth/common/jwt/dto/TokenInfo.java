package git_kkalnane.backend.starbucks.auth.common.jwt.dto;

import java.util.Date;
import lombok.Builder;
import lombok.Getter;

/**
 * JWT 토큰과 만료시간을 담고 있는 DTO 클래스
 */
@Builder
@Getter
public class TokenInfo {

    private final String token;
    private final Date expiration;
}
