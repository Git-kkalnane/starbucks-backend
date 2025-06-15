package git_kkalnane.backend.starbucks.member.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponse {
    private final Long id;
    private final String name;
    private final String email;

    public static MemberResponse of(Long id, String name, String email) {
        return new MemberResponse(id, name, email);
    }
}
