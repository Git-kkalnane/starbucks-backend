package git_kkalnane.backend.starbucks.auth.controller;


import git_kkalnane.backend.starbucks.auth.common.success.AuthSuccessCode;
import git_kkalnane.backend.starbucks.auth.service.AuthService;
import git_kkalnane.backend.starbucks.auth.common.jwt.JwtToken;
import git_kkalnane.backend.starbucks.global.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authInfoService;

    @PostMapping("/create-token")
    public ResponseEntity<SuccessResponse> createToken() {
        JwtToken token = authInfoService.createToken(1L);
        return ResponseEntity.ok((SuccessResponse.of(AuthSuccessCode.SIGN_UP_COMPLETED,token)));
    }

    @GetMapping("/token-authorization")
    public ResponseEntity<SuccessResponse> tokenTest(@RequestAttribute Long memberId) {
        return ResponseEntity.ok(SuccessResponse.of(AuthSuccessCode.AUTHORIZED, memberId));
    }
}
