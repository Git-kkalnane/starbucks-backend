package git_kkalnane.backend.starbucks.member.dto.request;

import git_kkalnane.backend.starbucks.member.validation.annotation.ValidNickname;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 닉네임 변경 요청 DTO
 *
 * @param nickname 변경할 닉네임
 */
@Schema(title = "닉네임 변경 요청 DTO")
public record UpdateNicknameRequest(

        @ValidNickname
        @Schema(description = "변경할 닉네임", example = "가나다라마바")
        String nickname) {
}
