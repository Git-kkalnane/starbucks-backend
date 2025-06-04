package git_kkalnane.backend.starbucks.member.dto.response;


import lombok.Getter;

@Getter
public class SignUpResponse {
    String memberName;
    String email;

    private SignUpResponse(String memberName, String email) {
        this.memberName = memberName;
        this.email = email;
    }

    public static SignUpResponse of(String memberName, String email) {
        return new SignUpResponse(memberName, email);
    }
}
