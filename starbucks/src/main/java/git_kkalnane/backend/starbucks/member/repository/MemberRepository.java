package git_kkalnane.backend.starbucks.member.repository;

import git_kkalnane.backend.starbucks.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
    Optional<Member> findMemberByEmail(String email);
}
