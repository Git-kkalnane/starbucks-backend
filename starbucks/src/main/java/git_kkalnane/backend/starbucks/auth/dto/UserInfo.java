package git_kkalnane.backend.starbucks.auth.dto;

public record UserInfo(String email, String nickname) {

    public static UserInfo of(String email, String nickname) {
        return new UserInfo(email, nickname);
    }
}
