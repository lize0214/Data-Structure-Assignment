package Utility;

/**
 * Unified API response wrapper for all Controllers.
 */
public class ControllerResult {
    private final boolean ok;
    private final String message;

    private ControllerResult(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

    public static ControllerResult success() {
        return new ControllerResult(true, null);
    }

    public static ControllerResult success(String message) {
        return new ControllerResult(true, message);
    }

    public static ControllerResult fail(String message) {
        return new ControllerResult(false, message);
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }
}
