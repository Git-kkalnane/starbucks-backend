package git_kkalnane.backend.starbucks.auth.dto;

import git_kkalnane.backend.starbucks.auth.common.jwt.JwtToken;

public record LoginDto(JwtToken token, UserInfo userInfo) {

    public static LoginDto of(JwtToken token, UserInfo userInfo) {
        return new LoginDto(token, userInfo);
    }
}
