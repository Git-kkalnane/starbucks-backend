package git_kkalnane.backend.starbucks.member.controller;


import git_kkalnane.backend.starbucks.global.success.SuccessResponse;
import git_kkalnane.backend.starbucks.member.common.success.MemberSuccessCode;
import git_kkalnane.backend.starbucks.member.dto.request.SignUpRequest;
import git_kkalnane.backend.starbucks.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PatchMapping
    public ResponseEntity<SuccessResponse<?>> signUp(
//            @RequestAttribute Long memberId, API에서 멤버를 확인 시 해당 어노테이션 사용
            @RequestBody SignUpRequest request
            ){

        memberService.createMember(request);
        return ResponseEntity.ok(
                SuccessResponse.of(MemberSuccessCode.MEMBER_UPDATE_COMPLETE));
    }
}
