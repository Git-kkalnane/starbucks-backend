package git_kkalnane.backend.starbucks.member.dto.request;

import git_kkalnane.backend.starbucks.member.validation.annotation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 비밀번호 변경 요청 DTO
 *
 * @param password 변경할 비밀번호
 */
@Schema(title = "비밀번호 변경 요청 DTO")
public record UpdatePasswordRequest(

        @ValidPassword
        @Schema(description = "변경할 비밀번호", example = "@Hong12345#")
        String password) {
}
