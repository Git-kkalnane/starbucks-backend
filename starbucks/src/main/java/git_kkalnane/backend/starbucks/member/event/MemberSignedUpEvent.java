package git_kkalnane.backend.starbucks.member.event;

import git_kkalnane.backend.starbucks.member.domain.Member;
import org.springframework.context.ApplicationEvent;

public class MemberSignedUpEvent extends ApplicationEvent {
    private final Member member;

    public MemberSignedUpEvent(Object source, Member member) {
        super(source);
        this.member = member;
    }

    public Member getMember() {
        return member;
    }
}
