package git_kkalnane.backend.starbucks.auth.service;

import git_kkalnane.backend.starbucks.auth.domain.AuthInfo;
import git_kkalnane.backend.starbucks.auth.repository.AuthInfoRepository;
import git_kkalnane.backend.starbucks.auth.service.jwt.JwtToken;
import git_kkalnane.backend.starbucks.auth.service.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthInfoRepository authInfoRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthInfo verify(String accessToken) {
        String memberId = jwtTokenProvider.getMemberId(accessToken);
        return authInfoRepository.getByMemberId(Long.parseLong(memberId));
    }

    public JwtToken createToken(Long memberId) {
        return jwtTokenProvider.createToken(String.valueOf(memberId));
    }

}
