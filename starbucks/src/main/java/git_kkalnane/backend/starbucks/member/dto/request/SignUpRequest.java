package git_kkalnane.backend.starbucks.member.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class SignUpRequest {

    private String name;
    private String nickname;
    private String email;
    private String password;
}
