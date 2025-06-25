package git_kkalnane.backend.starbucks.auth.dto;

import git_kkalnane.backend.starbucks.auth.dto.response.LoginResponse;

public record LoginDto(String accessToken, String refreshToken, String name, String nickname, String email) {

    public static LoginDto of(String accessToken, String refreshToken, String name, String nickname, String email) {
        return new LoginDto(accessToken, refreshToken, name, nickname, email);
    }

    public LoginResponse toLoginResponse() {
        return new LoginResponse(accessToken, name, nickname, email);
    }
}
