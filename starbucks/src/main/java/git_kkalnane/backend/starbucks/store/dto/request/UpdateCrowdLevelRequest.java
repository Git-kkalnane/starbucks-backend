package git_kkalnane.backend.starbucks.store.dto.request;

import git_kkalnane.backend.starbucks.store.domain.CrowdLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCrowdLevelRequest {

    @NotNull(message = "혼잡도는 null일 수 없습니다.")
    private CrowdLevel newCrowdLevel;
}
