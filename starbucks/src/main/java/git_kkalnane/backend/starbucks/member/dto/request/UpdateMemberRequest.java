package git_kkalnane.backend.starbucks.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateMemberRequest {
    private String memberName;
    private String password;
}
