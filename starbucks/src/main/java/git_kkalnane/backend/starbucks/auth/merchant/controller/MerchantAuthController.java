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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/merchant/auth")
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
    public ResponseEntity<SuccessResponse<MerchantLoginResponse>> login(@RequestBody LoginRequest request) {
        MerchantLoginDto merchantLoginDto = merchantAuthService.login(request);

        ResponseCookie responseCookie = CookieGenerator.createRefreshTokenCookie(merchantLoginDto.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header(HttpHeaders.AUTHORIZATION, ACCESS_PREFIX_STRING + merchantLoginDto.accessToken())
                .body(SuccessResponse.of(AuthSuccessCode.LOGIN_COMPLETED, merchantLoginDto.toLoginResponse()));
    }
}
