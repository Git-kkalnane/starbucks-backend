package git_kkalnane.backend.starbucks.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 멤버 상세 정보 응답 DTO
 *
 * @param name 사용자 이름
 * @param email 사용자 이메일
 * @param nickname 사용자 닉네임
 */
@Schema(title = "멤버 상세 정보 응답 DTO")
public record MemberDetailInfo(

        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "사용자 이메일", example = "example@gmail.com")
        String email,

        @Schema(description = "사용자 닉네임", example = "가나다라마바")
        String nickname) {

    public static MemberDetailInfo of(String name, String email, String nickname) {
        return new MemberDetailInfo(name, email, nickname);
    }
}
