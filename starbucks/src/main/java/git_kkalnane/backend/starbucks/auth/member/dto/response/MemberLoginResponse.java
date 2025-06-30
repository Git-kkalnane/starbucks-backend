package git_kkalnane.backend.starbucks.auth.member.dto.response;

public record MemberLoginResponse(String name, String nickname, String email) {

    public static MemberLoginResponse of(String name, String nickname, String email) {
        return new MemberLoginResponse(name, nickname, email);
    }
}
