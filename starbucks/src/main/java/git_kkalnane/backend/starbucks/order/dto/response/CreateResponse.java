package git_kkalnane.backend.starbucks.order.dto.response;

public record CreateResponse(
        int httpStatus,
        String message,
        Long orderId
) {

}
