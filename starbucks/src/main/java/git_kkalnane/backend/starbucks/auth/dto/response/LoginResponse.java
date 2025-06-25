package git_kkalnane.backend.starbucks.auth.dto.response;

public record LoginResponse(String accessToken, String name, String nickname, String email) {

    public static LoginResponse of(String accessToken, String name, String nickname, String email) {
        return new LoginResponse(accessToken, name, nickname, email);
    }
}
