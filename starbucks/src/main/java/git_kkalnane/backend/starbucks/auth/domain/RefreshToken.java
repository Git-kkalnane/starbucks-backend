package git_kkalnane.backend.starbucks.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expiration", nullable = false)
    private Date expiration;

    @Builder
    public RefreshToken(Long memberId, String token, Date expiration) {
        this.memberId = memberId;
        this.token = token;
        this.expiration = expiration;
    }

    public void modifyToken(String token) {
        this.token = token;
    }

    public void modifyExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public boolean validateToken(String refreshToken) {
        return Objects.equals(this.token, refreshToken);
    }
}
