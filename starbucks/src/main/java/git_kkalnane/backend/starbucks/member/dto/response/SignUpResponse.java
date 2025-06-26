package git_kkalnane.backend.starbucks.member.dto.response;

/**
 * 회원가입 응답 시 프론트엔드 측으로 넘겨줄 데이터를 담기위한 DTO
 * @param name 가입자 이름
 */
public record SignUpResponse(String name) {
}
