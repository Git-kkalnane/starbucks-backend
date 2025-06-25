package git_kkalnane.backend.starbucks.auth.dto.response;

import git_kkalnane.backend.starbucks.auth.dto.UserInfo;

public record LoginResponse(String accessToken, UserInfo user) {

    public static LoginResponse of(String accessToken, UserInfo user) {
        return new LoginResponse(accessToken, user);
    }
}
