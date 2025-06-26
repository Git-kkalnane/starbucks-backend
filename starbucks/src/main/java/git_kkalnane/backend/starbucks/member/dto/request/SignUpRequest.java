package git_kkalnane.backend.starbucks.member.dto.request;

/**
 * 회원가입 요청 시 프론트엔드 측에서 넘어오는 데이터를 담기 위한 DTO
 * @param name 가입자 이름
 * @param nickname 가입자 닉네임
 * @param email 가입자 이메일
 * @param password 가입자 비밀번호
 */
public record SignUpRequest(String name, String nickname, String email, String password) {
}
