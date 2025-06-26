package git_kkalnane.backend.starbucks.order.dto.request;

public record ItemOptionRequest(
        Long id,
        String syrupName,
        boolean isRequired,
        int displayOrder,
        int additionalPrice,
        int quantity
) {}
