package git_kkalnane.backend.starbucks.member.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class SignUpRequest {
    String memberName;
    String email;
    String planePassword;
}
