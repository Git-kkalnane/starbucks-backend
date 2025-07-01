package git_kkalnane.backend.starbucks.auth.merchant.controller;

import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.auth.common.success.AuthSuccessCode;
import git_kkalnane.backend.starbucks.auth.merchant.dto.MerchantLoginDto;
import git_kkalnane.backend.starbucks.auth.common.dto.request.LoginRequest;
import git_kkalnane.backend.starbucks.auth.merchant.dto.response.MerchantLoginResponse;
import git_kkalnane.backend.starbucks.auth.merchant.service.MerchantAuthService;
import git_kkalnane.backend.starbucks.auth.utils.CookieGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/merchant/auth")
@Tag(name = "Auth (Store)", description = "점주(매장) 인증/인가 관련 API")
public class MerchantAuthController {

    public static final String ACCESS_PREFIX_STRING = "Bearer ";

    private final MerchantAuthService merchantAuthService;

    public MerchantAuthController(MerchantAuthService merchantAuthService) {
        this.merchantAuthService = merchantAuthService;
    }

    /**
     * HTTP Request Body에 전송된 정보를 이용해 로그인 요청을 처리하는 컨트롤러 메서드이다.
     *
     * @param request LoginRequest 객체
     * @return - accessToken과 사용자 정보를 담고있는 LoginResponse를 담고 있는 ResponseEntity 객체
     */
    @Operation(
            summary = "점주 로그인",
            description = "점주의 이메일과 비밀번호를 받아 로그인을 처리하고, 토큰을 발급합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 데이터 유효성 오류"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 이메일이거나 비밀번호가 틀렸을 경우"
            )
    })
    @Parameters({
            @Parameter(name = "email", description = "회원 이메일", example = "user0123@gmail.com"),
            @Parameter(name = "password", description = "비밀번호", example = "password0123")
    })
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<MerchantLoginResponse>> login(@RequestBody LoginRequest request) {
        MerchantLoginDto merchantLoginDto = merchantAuthService.login(request);

        ResponseCookie responseCookie = CookieGenerator.createRefreshTokenCookie(merchantLoginDto.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header(HttpHeaders.AUTHORIZATION, ACCESS_PREFIX_STRING + merchantLoginDto.accessToken())
                .body(SuccessResponse.of(AuthSuccessCode.LOGIN_COMPLETED, merchantLoginDto.toLoginResponse()));
    }
}
