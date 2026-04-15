package Config;

import org.springframework.security.access.AccessDeniedException;

public class PermissionDeniedException extends AccessDeniedException {

    private final String module;
    private final String action;

    public PermissionDeniedException(String module, String action) {
        super("Permission insuffisante");
        this.module = module;
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public String getAction() {
        return action;
    }
}


