package git_kkalnane.backend.starbucks.member.controller;


import git_kkalnane.backend.starbucks._global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.member.common.success.MemberSuccessCode;
import git_kkalnane.backend.starbucks.member.dto.request.SignUpRequest;
import git_kkalnane.backend.starbucks.member.dto.request.UpdateNicknameRequest;
import git_kkalnane.backend.starbucks.member.dto.request.UpdatePasswordRequest;
import git_kkalnane.backend.starbucks.member.dto.response.MemberDetailInfo;
import git_kkalnane.backend.starbucks.member.dto.response.SignUpResponse;
import git_kkalnane.backend.starbucks.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "사용자(회원) 관련 API")
public class MemberController {

    private final MemberService memberService;

    /**
     * HTTP Request Body에 전송된 정보를 이용해 회원가입 요청을 처리하는 컨트롤러 메서드이다.
     *
     * @param request SingUpRequest 객체
     * @return SignUpResponse 객체를 담고 있는 ResponseEntity 객체
     */
    @Operation(
            summary = "회원가입",
            description = "사용자 정보를 받아 회원가입을 처리합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 데이터 유효성 오류 (이메일 중복, 이름 길이, 형식 등)"
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<SignUpResponse>> signup(
            @Parameter(description = "회원가입에 필요한 정보") @RequestBody @Valid SignUpRequest request) {

        return ResponseEntity.ok(
                (SuccessResponse.of(MemberSuccessCode.SIGN_UP_COMPLETED, memberService.createMember(request))));
    }

    /**
     * HTTP Request 속성에 있는 멤버 ID를 이용해 멤버 상세 정보를 조회하는 컨트롤러 메서드이다.
     *
     * @param memberId 사용자 엔티티 식별자 (ID)
     * @return {@link MemberDetailInfo} 객체
     */
    @Operation(
            summary = "멤버 상세 정보 조회",
            description = "현재 로그인한 사용자의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "멤버 상세 정보가 성공적으로 조회됨"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 멤버를 찾을 수 없음"
            )
    })
    @GetMapping("/info")
    public ResponseEntity<SuccessResponse<MemberDetailInfo>> getMemberDetailInfo(
            @RequestAttribute(name = "memberId") Long memberId) {

        return ResponseEntity.ok(SuccessResponse.of(MemberSuccessCode.GET_MEMBER_DETAIL_INFO_COMPLETE,
                memberService.getMemberDetailInfo(memberId)));
    }

    /**
     * HTTP Request 속성에 있는 멤버 ID를 이용해 멤버의 닉네임을 변경하는 컨트롤러 메서드이다.
     */
    @Operation(
            summary = "닉네임 변경",
            description = "현재 로그인한 사용자의 닉네임을 변경합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "멤버 닉네임이 성공적으로 갱신됨"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 데이터 유효성 오류 (예: 닉네임 형식 위반)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 멤버를 찾을 수 없음"
            )
    })
    @PostMapping("/update/nickname")
    public ResponseEntity<SuccessResponse<?>> updateNickname(
            @RequestAttribute(name = "memberId") Long memberId,
            @Parameter(description = "새로운 닉네임 정보") @RequestBody @Valid UpdateNicknameRequest request) {

        memberService.updateNickname(memberId, request);

        return ResponseEntity.ok(SuccessResponse.of(MemberSuccessCode.MEMBER_NICKNAME_UPDATE_COMPLETE));
    }

    /**
     * HTTP Request 속성에 있는 멤버 ID를 이용해 멤버의 비밀번호를 변경하는 컨트롤러 메서드이다.
     */
    @Operation(
            summary = "멤버 비밀번호 갱신",
            description = "현재 로그인한 사용자의 비밀번호를 변경합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "멤버 비밀번호가 성공적으로 갱신됨"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 데이터 유효성 오류 (예: 비밀번호 정책 위반)")
            ,
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자")
            ,
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 멤버를 찾을 수 없음"
            )
    })
    @PostMapping("/update/password")
    public ResponseEntity<SuccessResponse<?>> updatePassword(
            @RequestAttribute(name = "memberId") Long memberId,
            @Parameter(description = "새로운 비밀번호 정보") @RequestBody @Valid UpdatePasswordRequest request) {

        memberService.updatePassword(memberId, request);

        return ResponseEntity.ok(SuccessResponse.of(MemberSuccessCode.MEMBER_PASSWORD_UPDATE_COMPLETE));
    }

}
