package git_kkalnane.backend.starbucks.member.dto.request;

import git_kkalnane.backend.starbucks.member.validation.annotation.ValidEmail;
import git_kkalnane.backend.starbucks.member.validation.annotation.ValidName;
import git_kkalnane.backend.starbucks.member.validation.annotation.ValidNickname;
import git_kkalnane.backend.starbucks.member.validation.annotation.ValidPassword;

/**
 * 회원가입 요청 시 프론트엔드 측에서 넘어오는 데이터를 담기 위한 DTO
 *
 * @param name     가입자 이름
 * @param nickname 가입자 닉네임
 * @param email    가입자 이메일
 * @param password 가입자 비밀번호
 */
public record SignUpRequest(

        @ValidName
        String name,

        @ValidNickname
        String nickname,

        @ValidEmail
        String email,

        @ValidPassword
        String password) {
}
