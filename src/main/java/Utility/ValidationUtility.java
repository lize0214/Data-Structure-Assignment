package Utility;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Common validation utility — returns null on success, error message string on failure.
 */
public class ValidationUtility {

    // ───────────────────── Basic Validation ─────────────────────

    public static String validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " cannot be empty";
        }
        return null;
    }

    public static String validatePositive(double value, String fieldName) {
        if (value <= 0) {
            return fieldName + " must be greater than 0";
        }
        return null;
    }

    public static String validatePositive(int value, String fieldName) {
        if (value <= 0) {
            return fieldName + " must be greater than 0";
        }
        return null;
    }

    public static String validateNonNegative(int value, String fieldName) {
        if (value < 0) {
            return fieldName + " cannot be negative";
        }
        return null;
    }

    public static <T> String validateNotNull(T value, String fieldName) {
        if (value == null) {
            return fieldName + " cannot be null";
        }
        return null;
    }

    // ───────────────────── Enum Validation ─────────────────────

    private static final String[] ROOM_STATUSES = {"Available", "Occupied", "Dirty", "Maintenance"};
    private static final String[] BOOKING_STATUSES = {"Confirmed", "Checked-in", "Checked-out", "Cancelled"};
    private static final String[] MEMBER_TIERS = {"Silver", "Gold", "Elite", "Diamond", "Platinum"};

    public static String validateRoomStatus(String status) {
        for (String s : ROOM_STATUSES) {
            if (s.equalsIgnoreCase(status)) return null;
        }
        return "Invalid room status: " + status + " (options: " + String.join(", ", ROOM_STATUSES) + ")";
    }

    public static String validateBookingStatus(String status) {
        for (String s : BOOKING_STATUSES) {
            if (s.equalsIgnoreCase(status)) return null;
        }
        return "Invalid booking status: " + status + " (options: " + String.join(", ", BOOKING_STATUSES) + ")";
    }

    public static String validateMemberTier(String tier) {
        for (String t : MEMBER_TIERS) {
            if (t.equalsIgnoreCase(tier)) return null;
        }
        return "Invalid member tier: " + tier + " (options: " + String.join(", ", MEMBER_TIERS) + ")";
    }

    // ───────────────────── Date Validation ─────────────────────

    public static String validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return "Date cannot be null";
        }
        if (!checkOut.isAfter(checkIn)) {
            return "Check-out date must be after check-in date";
        }
        return null;
    }

    // ───────────────────── Batch Validation ─────────────────────

    /**
     * Accumulates validation errors. Call hasErrors() / getErrorMessage()
     * after all checks to decide the response.
     *
     * Usage:
     * <pre>{@code
     *   ValidationAccumulator acc = new ValidationAccumulator();
     *   acc.check(ValidationUtility.validateRequired(name, "Name"));
     *   acc.check(ValidationUtility.validatePositive(price, "Price"));
     *   if (acc.hasErrors()) return ControllerResult.fail(acc.getErrorMessage());
     * }</pre>
     */
    public static class ValidationAccumulator {
        private final List<String> errors = new ArrayList<>();

        public void check(String error) {
            if (error != null) {
                errors.add(error);
            }
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}
