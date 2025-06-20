package git_kkalnane.backend.starbucks.member.controller;


import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.member.common.success.MemberSuccessCode;
import git_kkalnane.backend.starbucks.member.dto.request.SignUpRequest;
import git_kkalnane.backend.starbucks.member.dto.response.SignUpResponse;
import git_kkalnane.backend.starbucks.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * HTTP Request Body에 전송된 정보를 이용해 회원가입 요청을 처리하는 컨트롤러 메서드이다.
     *
     * @param request SingUpRequest 객체
     * @return SignUpResponse 객체를 담고 있는 ResponseEntity 객체
     */
    @PostMapping("/signup")
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
                    description = "이미 존재하는 이메일, 이름 길이 초과, 사용자 성명 규칙 위배"
            )
    })
    @Parameters({
            @Parameter(name = "name", description = "회원 이름", example = "홍길동"),
            @Parameter(name = "nickname", description = "닉네임", example = "나는야홍길동"),
            @Parameter(name = "email", description = "회원 이메일", example = "user0123@gmail.com"),
            @Parameter(name = "password", description = "비밀번호", example = "password0123")
    })
    public ResponseEntity<SuccessResponse<SignUpResponse>> signup(@RequestBody SignUpRequest request) {

        memberService.createMember(request);

        return ResponseEntity.ok(
                (SuccessResponse.of(MemberSuccessCode.SIGN_UP_COMPLETED)));
    }
}
