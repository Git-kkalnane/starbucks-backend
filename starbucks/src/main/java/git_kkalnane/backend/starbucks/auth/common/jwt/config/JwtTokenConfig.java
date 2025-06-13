package git_kkalnane.backend.starbucks.auth.common.jwt.config;

import git_kkalnane.backend.starbucks.auth.common.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtTokenConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity-in-seconds}")
    private Long accessTokenExpired;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpired;

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(secretKey, refreshTokenExpired, accessTokenExpired);
    }
}
