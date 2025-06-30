package git_kkalnane.backend.starbucks.merchant.service;

import git_kkalnane.backend.starbucks._global.utils.Encryptor;
import git_kkalnane.backend.starbucks.merchant.common.exception.MerchantErrorCode;
import git_kkalnane.backend.starbucks.merchant.common.exception.MerchantException;
import git_kkalnane.backend.starbucks.merchant.domain.Merchant;
import git_kkalnane.backend.starbucks.merchant.dto.request.SignUpRequest;
import git_kkalnane.backend.starbucks.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final Encryptor encryptor;

    /**
     * SignUpRequest를 바탕으로 DB에 매장정보를 저장하는 메서드
     *
     * @param request {@link SignUpRequest}
     */
    @Transactional
    public void createMerchant(SignUpRequest request) {
        validateDuplicatedEmail(request.email());

        String encryptedPassword = encryptor.encrypt(request.password());

        Merchant merchant = Merchant.builder()
                .merchantName(request.name())
                .email(request.email())
                .passwordHash(encryptedPassword)
                .build();

        merchantRepository.save(merchant);
    }

    /**
     * 사용자가 입력한 이메일이 이미 존재하는지 검증하는 메서드
     *
     * @param email 회원가입 요청에 포함된 가입자의 이메일
     */
    private void validateDuplicatedEmail(String email) {
        if (merchantRepository.existsByEmail(email)) {
            throw new MerchantException(MerchantErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
}
