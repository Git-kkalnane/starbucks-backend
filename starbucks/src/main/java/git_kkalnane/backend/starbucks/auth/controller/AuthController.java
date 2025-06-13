package git_kkalnane.backend.starbucks.auth.controller;


import git_kkalnane.backend.starbucks.auth.common.jwt.JwtToken;
import git_kkalnane.backend.starbucks.auth.common.success.AuthSuccessCode;
import git_kkalnane.backend.starbucks.auth.service.AuthService;
import git_kkalnane.backend.starbucks.global.success.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authInfoService;

    @PostMapping("/create-token")
    @Operation(
            summary = "JWT 토큰 생성",
            description = "회원 가입 완료 후 JWT 토큰을 발급합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 생성 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류"
            )
    })
    public ResponseEntity<SuccessResponse<JwtToken>> createToken() {
        JwtToken token = authInfoService.createToken(1L);
        return ResponseEntity.ok((SuccessResponse.of(AuthSuccessCode.SIGN_UP_COMPLETED,token)));
    }
}
