package git_kkalnane.backend.starbucks.merchant.dto.request;

import git_kkalnane.backend.starbucks._global.validation.annotation.ValidEmail;
import git_kkalnane.backend.starbucks._global.validation.annotation.ValidName;
import git_kkalnane.backend.starbucks._global.validation.annotation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 매장 회원가입 요청 시 프론트엔드 측에서 넘어오는 데이터를 담기 위한 DTO
 *
 * @param name     매장명
 * @param email    매장 이메일
 * @param password 매장 비밀번호
 */
@Schema(title = "회원가입 요청 DTO")
public record SignUpRequest(

        @ValidName
        @Schema(description = "매장명", example = "강남점")
        String name,

        @ValidEmail
        @Schema(description = "매장 이메일", example = "gangnam@starbucks.co.kr")
        String email,

        @ValidPassword
        @Schema(description = "매장 비밀번호", example = "@Gangnam12345!")
        String password) {
}
