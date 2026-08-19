package Utility;

import ADT.ArrayList;
import ADT.ListInterface;
import java.time.LocalDate;
/**
 * @author Chua Li Ze
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

    private static final String[] ROOM_STATUSES = {
        "Available", "Occupied", "Dirty", "CleaningInProgress", "Inspected", "ReadyForCheckIn", "Maintenance"
    };
    private static final String[] BOOKING_STATUSES = {"Confirmed", "CheckedIn", "CheckedOut", "Cancelled"};
    private static final String[] MEMBER_TIERS = {"Silver", "Gold", "Elite", "Diamond", "Platinum"};
    private static final String[] ROOM_TYPES = {"Single", "Deluxe", "Suite", "Presidential"};
    private static final String[] VIP_TIERS = {"Elite", "Diamond", "Platinum"};
    private static final String[] PAYMENT_METHODS = {"Cash", "Card", "E-wallet"};

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

    public static String validateRoomType(String roomType) {
        if (roomType == null || roomType.trim().isEmpty()) {
            return null; // optional field — empty is valid
        }
        for (String t : ROOM_TYPES) {
            if (t.equalsIgnoreCase(roomType.trim())) return null;
        }
        return "Invalid room type: " + roomType + " (options: " + String.join(", ", ROOM_TYPES) + ")";
    }

    /**
     * Validates that a tier is eligible for VIP priority allocation
     * (Elite, Diamond, or Platinum only — Silver and Gold are rejected).
     */
    public static String validateVIPAllocationTier(String tier) {
        if (tier == null || tier.trim().isEmpty()) {
            return "Tier cannot be empty";
        }
        for (String t : VIP_TIERS) {
            if (t.equalsIgnoreCase(tier.trim())) return null;
        }
        return "Tier '" + tier + "' is not eligible for VIP priority allocation."
                + " Only Elite, Diamond, and Platinum members qualify.";
    }
    
    public static String validatePaymentMethod(String method) {
        for (String m : PAYMENT_METHODS) {
            if (m.equalsIgnoreCase(method)) return null;
        }
        return "Invalid payment method: " + method + " (options: " + String.join(", ", PAYMENT_METHODS) + ")";
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

        private final ListInterface<String> errors =
                new ArrayList<>();

        public void check(String error) {
            if (error != null) {
                errors.add(error);
            }
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public String getErrorMessage() {

            StringBuilder sb = new StringBuilder();

            for (int i = 1; i <= errors.size(); i++) {

                if (i > 1) {
                    sb.append("; ");
                }

                sb.append(errors.getEntry(i));
            }

            return sb.toString();
        }
    }
}
