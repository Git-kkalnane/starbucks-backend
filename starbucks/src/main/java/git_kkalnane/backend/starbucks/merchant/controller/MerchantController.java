package git_kkalnane.backend.starbucks.merchant.controller;

import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.member.dto.response.SignUpResponse;
import git_kkalnane.backend.starbucks.merchant.common.success.MerchantSuccessCode;
import git_kkalnane.backend.starbucks.merchant.dto.request.SignUpRequest;
import git_kkalnane.backend.starbucks.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Merchant", description = "점주 관련 API")
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * HTTP Request Body에 전송된 정보를 이용해 회원가입 요청을 처리하는 컨트롤러 메서드이다.
     *
     * @param request SingUpRequest 객체
     * @return SignUpResponse 객체를 담고 있는 ResponseEntity 객체
     */
    @Operation(
            summary = "점주 회원가입",
            description = "점주의 정보를 받아 회원가입을 처리합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "점주 회원가입 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 데이터 유효성 오류 (형식, 길이, 규칙 위반 등)"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 이메일 또는 사업자 등록번호"
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<SignUpResponse>> signup(
            @Parameter(description = "점주 회원가입에 필요한 정보") @RequestBody @Valid SignUpRequest request) {

        merchantService.createMerchant(request);

        return ResponseEntity.ok(
                (SuccessResponse.of(MerchantSuccessCode.SIGN_UP_COMPLETED)));
    }
}
