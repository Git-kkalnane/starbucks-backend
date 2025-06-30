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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<SignUpResponse>> signup(@RequestBody @Valid SignUpRequest request) {

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
            description = "멤버 상세 정보를 조회 시 사용하는 API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "멤버 상세 정보가 성공적으로 조회됨"
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
            summary = "멤버 닉네임 갱신",
            description = "멤버 닉네임을 갱신 시 사용하는 API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "멤버 닉네임이 성공적으로 갱신됨"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 멤버를 찾을 수 없음"
            )
    })
    @PostMapping("/update/nickname")
    public ResponseEntity<SuccessResponse<?>> updateNickname(
            @RequestAttribute(name = "memberId") Long memberId,
            @RequestBody @Valid UpdateNicknameRequest request) {

        memberService.updateNickname(memberId, request);

        return ResponseEntity.ok(SuccessResponse.of(MemberSuccessCode.MEMBER_NICKNAME_UPDATE_COMPLETE));
    }

    /**
     * HTTP Request 속성에 있는 멤버 ID를 이용해 멤버의 비밀번호를 변경하는 컨트롤러 메서드이다.
     */
    @Operation(
            summary = "멤버 비밀번호 갱신",
            description = "멤버 비밀번호를 갱신 시 사용하는 API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "멤버 비밀번호가 성공적으로 갱신됨"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 멤버를 찾을 수 없음"
            )
    })
    @PostMapping("/update/password")
    public ResponseEntity<SuccessResponse<?>> updatePassword(
            @RequestAttribute(name = "memberId") Long memberId,
            @RequestBody @Valid UpdatePasswordRequest request) {

        memberService.updatePassword(memberId, request);

        return ResponseEntity.ok(SuccessResponse.of(MemberSuccessCode.MEMBER_PASSWORD_UPDATE_COMPLETE));
    }

}
