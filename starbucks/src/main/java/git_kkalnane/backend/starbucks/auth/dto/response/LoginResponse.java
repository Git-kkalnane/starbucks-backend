package git_kkalnane.backend.starbucks.auth.dto.response;

public record LoginResponse(String name, String nickname, String email) {

    public static LoginResponse of(String name, String nickname, String email) {
        return new LoginResponse(name, nickname, email);
    }
}
