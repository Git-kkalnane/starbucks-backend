package git_kkalnane.backend.starbucks.auth.member.controller;

import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.auth.common.jwt.dto.TokenInfo;
import git_kkalnane.backend.starbucks.auth.common.success.AuthSuccessCode;
import git_kkalnane.backend.starbucks.auth.member.dto.MemberLoginDto;
import git_kkalnane.backend.starbucks.auth.common.dto.request.LoginRequest;
import git_kkalnane.backend.starbucks.auth.member.dto.response.MemberLoginResponse;
import git_kkalnane.backend.starbucks.auth.member.service.MemberAuthService;
import git_kkalnane.backend.starbucks.auth.utils.CookieGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class MemberAuthController {

    public static final String ACCESS_PREFIX_STRING = "Bearer ";

    private final MemberAuthService memberAuthService;

    /**
     * HTTP Request Body에 전송된 정보를 이용해 로그인 요청을 처리하는 컨트롤러 메서드이다.
     *
     * @param request LoginRequest 객체
     * @return - accessToken과 사용자 정보를 담고있는 LoginResponse를 담고 있는 ResponseEntity 객체
     */
    @Operation(
            summary = "로그인",
            description = "로그인 시 사용하는 API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "올바르지 않은 이메일 또는 패스워드"
            )
    })
    @Parameters({
            @Parameter(name = "email", description = "회원 이메일", example = "user0123@gmail.com"),
            @Parameter(name = "password", description = "비밀번호", example = "password0123")
    })
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<MemberLoginResponse>> login(@RequestBody LoginRequest request) {
        MemberLoginDto memberLoginDto = memberAuthService.login(request);

        ResponseCookie responseCookie = CookieGenerator.createRefreshTokenCookie(memberLoginDto.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header(HttpHeaders.AUTHORIZATION, ACCESS_PREFIX_STRING + memberLoginDto.accessToken())
                .body(SuccessResponse.of(AuthSuccessCode.LOGIN_COMPLETED, memberLoginDto.toLoginResponse()));
    }

    /**
     * HTTP Request Header에 전송된 accessToken을 이용해 로그아웃 요청을 처리하는 컨트롤러 메서드이다.
     *
     * @param memberId 멤버 엔티티의 식별자
     * @return 결과 메시지
     */
    @Operation(
            summary = "로그아웃",
            description = "로그아웃 시 사용하는 API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT 토큰과 관련된 오류"
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<String>> logout(@RequestAttribute(name = "id") Long memberId) {
        // 서비스 레이어 호출
        memberAuthService.logout(memberId);

        // 쿠키 무력화
        ResponseCookie responseCookie = CookieGenerator.destroyRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(SuccessResponse.of(AuthSuccessCode.LOGOUT_COMPLETED, "로그아웃 되었습니다."));
    }

    /**
     * HTTP Request Header에 전송된 refreshToken과 인터셉터에서 추가된 속성을 이용해 액세스 토큰 갱신을 처리하는 컨트롤러 메서드이다.
     *
     * @param refreshToken 리프레쉬 토큰
     * @param memberId     멤버 엔티티의 식별자
     * @return
     */
    @Operation(
            summary = "액세스 토큰 재발급",
            description = "액세스 토큰 재발급시 사용하는 API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 재발급 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT 토큰과 관련된 오류 (위변조된 토큰)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 멤버의 토큰이 DB에 존재하지 않음"
            )
    })
    @PostMapping("/reissue-access-token")
    public ResponseEntity<SuccessResponse<?>> reissueAccessToken(
            @RequestHeader(name = "Authorization") String refreshToken,
            @RequestAttribute(name = "id") Long memberId) {
        TokenInfo accessTokenInfo = memberAuthService.reissueAccessToken(refreshToken, memberId);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, ACCESS_PREFIX_STRING + accessTokenInfo.getToken())
                .body(SuccessResponse.of(AuthSuccessCode.TOKEN_REISSUE_COMPLETED));
    }
}
