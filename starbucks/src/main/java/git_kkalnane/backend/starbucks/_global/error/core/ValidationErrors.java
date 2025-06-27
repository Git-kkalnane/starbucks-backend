package git_kkalnane.backend.starbucks._global.error.core;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class ValidationErrors {

    Map<String, String> errors;

    public ValidationErrors() {
        this.errors = new HashMap<>();
    }

    public void addError(String field, String message) {
        errors.put(field, message);
    }
}
