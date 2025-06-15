package git_kkalnane.backend.starbucks.member.domain;

import git_kkalnane.backend.starbucks.global.entity.BaseTimeEntity;
import git_kkalnane.backend.starbucks.member.dto.request.SignUpRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(unique = true)
    private String email;

    @Column
    private String password; // TODO : 암호화 적용해야함, 적절하게 컬럼 추가

    private Member(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public static Member of(String name, String email, String password) {
        return new Member(name, email, password);
    }

    public static Member from(SignUpRequest request) {
        return new Member(
                request.getMemberName(),
                request.getEmail(),
                request.getPlanePassword()
        );
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
