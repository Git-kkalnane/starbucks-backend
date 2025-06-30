package git_kkalnane.backend.starbucks.merchant.controller;

import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.member.dto.response.SignUpResponse;
import git_kkalnane.backend.starbucks.merchant.common.success.MerchantSuccessCode;
import git_kkalnane.backend.starbucks.merchant.dto.request.SignUpRequest;
import git_kkalnane.backend.starbucks.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;


    /**
     * HTTP Request Body에 전송된 정보를 이용해 회원가입 요청을 처리하는 컨트롤러 메서드이다.
     *
     * @param request SingUpRequest 객체
     * @return SignUpResponse 객체를 담고 있는 ResponseEntity 객체
     */
    @Operation(
            summary = "회원가입",
            description = "회원가입 시 사용하는 API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 존재하는 이메일, 이름 길이 초과, 매장명 규칙 위배, 비밀번호 규칙 위반"
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<SignUpResponse>> signup(@RequestBody @Valid SignUpRequest request) {

        merchantService.createMerchant(request);

        return ResponseEntity.ok(
                (SuccessResponse.of(MerchantSuccessCode.SIGN_UP_COMPLETED)));
    }
}
