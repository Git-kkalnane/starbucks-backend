package git_kkalnane.backend.starbucks.auth.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Entity
@Getter
@RequiredArgsConstructor
public class AuthInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authInfoId;

    private String refreshToken;

    private Long memberId;

    public AuthInfo(Long authInfoId, String refreshToken, Long memberId) {
        this.authInfoId = authInfoId;
        this.refreshToken = refreshToken;
        this.memberId = memberId;
    }
}
