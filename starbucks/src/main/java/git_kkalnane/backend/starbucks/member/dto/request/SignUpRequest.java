package git_kkalnane.backend.starbucks.member.dto.request;

import git_kkalnane.backend.starbucks._global.validation.annotation.ValidEmail;
import git_kkalnane.backend.starbucks._global.validation.annotation.ValidName;
import git_kkalnane.backend.starbucks._global.validation.annotation.ValidNickname;
import git_kkalnane.backend.starbucks._global.validation.annotation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 회원가입 요청 시 프론트엔드 측에서 넘어오는 데이터를 담기 위한 DTO
 *
 * @param name     가입자 이름
 * @param nickname 가입자 닉네임
 * @param email    가입자 이메일
 * @param password 가입자 비밀번호
 */
@Schema(title = "회원가입 요청 DTO")
public record SignUpRequest(

        @ValidName
        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @ValidNickname
        @Schema(description = "사용자 닉네임", example = "나는야홍길동")
        String nickname,

        @ValidEmail
        @Schema(description = "사용자 이메일", example = "hong0123@gmail.com")
        String email,

        @ValidPassword
        @Schema(description = "비밀번호", example = "@Hong12345#")
        String password) {
}
