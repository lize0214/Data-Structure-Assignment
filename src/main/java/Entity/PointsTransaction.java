package Entity;
import java.time.LocalDate;

/**
 * PointsTransaction.java
 * Module-specific entity for the Loyalty & Rewards Service.
 * Records every points change for a member - earning, redeeming, undoing
 * a redemption, an expiry deduction, or a starting balance at registration -
 * so a full Transaction History can be shown, not just redemption events.
 *
 * Carries two expiry-related fields used by LoyaltyController's expiry
 * engine:
 *   - expiryDate       : when this transaction's points expire (only set
 *                        on EARN transactions; null for everything else).
 *   - expiryProcessed  : whether this transaction has already had its
 *                        expired points deducted from the member's
 *                        balance by processExpiredPoints(). Once true, it
 *                        is permanently skipped on every future run, so
 *                        the same points can never be deducted twice.
 *
 * Backward compatible: older 6-column files (no expiry at all) and
 * 7-column files (expiry date but no processed flag) are still read
 * correctly - expiryDate defaults to null and expiryProcessed defaults
 * to false for rows that predate those columns.
 */

/*
 * Author: Tan Pei Xing
 */
public class PointsTransaction {
    private String transactionId;
    private String memberId;
    private int pointsChange;   // positive = earned/refunded, negative = spent/expired
    private String type;        // EARN, REDEEM, UNDO, REGISTER, EXPIRE
    private String note;        // short description, letters/numbers/spaces only
    private LocalDate transactionDate;
    private LocalDate expiryDate;      // null when this transaction does not carry expiring points
    private boolean expiryProcessed;   // true once this transaction's expiry has been applied

    public PointsTransaction() {
    }

    /**
     * Simplest constructor - no expiry. Used for REDEEM, UNDO, EXPIRE
     * (deductions never expire - there's nothing left to expire).
     */
    public PointsTransaction(String transactionId, String memberId, int pointsChange,
                              String type, String note, LocalDate transactionDate) {
        this(transactionId, memberId, pointsChange, type, note, transactionDate, null);
    }

    /**
     * Used for EARN transactions - carries an expiry date, not yet
     * processed (expiryProcessed defaults to false).
     */
    public PointsTransaction(String transactionId, String memberId, int pointsChange,
                              String type, String note, LocalDate transactionDate, LocalDate expiryDate) {
        this(transactionId, memberId, pointsChange, type, note, transactionDate, expiryDate, false);
    }

    /**
     * Full constructor - used internally by fromCsvLine() when restoring
     * a transaction whose expiry may already have been processed in a
     * previous run.
     */
    public PointsTransaction(String transactionId, String memberId, int pointsChange,
                              String type, String note, LocalDate transactionDate,
                              LocalDate expiryDate, boolean expiryProcessed) {
        this.transactionId = transactionId;
        this.memberId = memberId;
        this.pointsChange = pointsChange;
        this.type = type;
        this.note = note;
        this.transactionDate = transactionDate;
        this.expiryDate = expiryDate;
        this.expiryProcessed = expiryProcessed;
    }

    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public String getMemberId() {
        return memberId;
    }
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
    public int getPointsChange() {
        return pointsChange;
    }
    public void setPointsChange(int pointsChange) {
        this.pointsChange = pointsChange;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public LocalDate getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    public boolean isExpiryProcessed() {
        return expiryProcessed;
    }
    public void setExpiryProcessed(boolean expiryProcessed) {
        this.expiryProcessed = expiryProcessed;
    }

    /**
     * Format: transactionId,memberId,pointsChange,type,note,transactionDate,expiryDate,expiryProcessed
     * expiryDate is written as the literal text "NONE" when there isn't
     * one, so it stays a real column (not a blank/missing field).
     *
     * split(",", 8) on read naturally yields 6, 7, or 8 parts depending
     * on how many commas the line actually contains, so older rows
     * (written before expiryDate/expiryProcessed existed) parse without
     * any special-casing beyond checking parts.length.
     */
    public static PointsTransaction fromCsvLine(String line) {
        String[] parts = line.split(",", 8);

        if (parts.length == 8) {
            String expiryText = parts[6].trim();
            LocalDate expiry = (expiryText.isEmpty() || expiryText.equals("NONE")) ? null : LocalDate.parse(expiryText);
            boolean processed = Boolean.parseBoolean(parts[7].trim());
            return new PointsTransaction(
                    parts[0].trim(),
                    parts[1].trim(),
                    Integer.parseInt(parts[2].trim()),
                    parts[3].trim(),
                    parts[4].trim(),
                    LocalDate.parse(parts[5].trim()),
                    expiry,
                    processed
            );
        } else if (parts.length == 7) {
            String expiryText = parts[6].trim();
            LocalDate expiry = (expiryText.isEmpty() || expiryText.equals("NONE")) ? null : LocalDate.parse(expiryText);
            return new PointsTransaction(
                    parts[0].trim(),
                    parts[1].trim(),
                    Integer.parseInt(parts[2].trim()),
                    parts[3].trim(),
                    parts[4].trim(),
                    LocalDate.parse(parts[5].trim()),
                    expiry
            );
        } else if (parts.length == 6) {
            return new PointsTransaction(
                    parts[0].trim(),
                    parts[1].trim(),
                    Integer.parseInt(parts[2].trim()),
                    parts[3].trim(),
                    parts[4].trim(),
                    LocalDate.parse(parts[5].trim())
            );
        }
        throw new IllegalArgumentException("Invalid PointsTransaction data format: " + line);
    }

    public String toCsvLine() {
        String expiryPart = (expiryDate == null) ? "NONE" : expiryDate.toString();
        return transactionId + "," + memberId + "," + pointsChange + "," + type + "," + note + ","
                + transactionDate + "," + expiryPart + "," + expiryProcessed;
    }

    /**
     * Display format matching the Transaction History screen:
     *   15/08/2026
     *   +250 Points
     *   Booking Payment
     *   (expires 15/08/2027)     <- only shown when this transaction carries an expiry
     */
    @Override
    public String toString() {
        String sign = (pointsChange >= 0) ? "+" : "";
        String base = transactionDate + "\n" + sign + pointsChange + " Points\n" + note;
        if (expiryDate != null) {
            base += "\n(expires " + expiryDate + ")";
        }
        return base;
    }
}