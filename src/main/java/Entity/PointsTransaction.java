package Entity;

import java.time.LocalDate;

/**
 * PointsTransaction.java
 * Module-specific entity for the Loyalty & Rewards Service.
 * Records every points change for a member - earning, redeeming, undoing
 * a redemption, or a starting balance at registration - so a full
 * Transaction History can be shown, not just redemption events.
 */
public class PointsTransaction {

    private String transactionId;
    private String memberId;
    private int pointsChange;   // positive = earned/refunded, negative = spent
    private String type;        // EARN, REDEEM, UNDO, REGISTER
    private String note;        // short description, letters/numbers/spaces only
    private LocalDate transactionDate;

    public PointsTransaction() {
    }

    public PointsTransaction(String transactionId, String memberId, int pointsChange,
                              String type, String note, LocalDate transactionDate) {
        this.transactionId = transactionId;
        this.memberId = memberId;
        this.pointsChange = pointsChange;
        this.type = type;
        this.note = note;
        this.transactionDate = transactionDate;
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

    // Format: transactionId,memberId,pointsChange,type,note,transactionDate(ISO yyyy-MM-dd)
    // split(",", 6) keeps the note field intact even if it ever contains a comma.
    public static PointsTransaction fromCsvLine(String line) {
        String[] parts = line.split(",", 6);
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid PointsTransaction data format: " + line);
        }
        return new PointsTransaction(
                parts[0].trim(),
                parts[1].trim(),
                Integer.parseInt(parts[2].trim()),
                parts[3].trim(),
                parts[4].trim(),
                LocalDate.parse(parts[5].trim())
        );
    }

    public String toCsvLine() {
        return transactionId + "," + memberId + "," + pointsChange + "," + type + "," + note + "," + transactionDate;
    }

    /**
     * Display format matching the Transaction History screen:
     *   15/08/2026
     *   +250 Points
     *   Booking Payment
     */
    @Override
    public String toString() {
        String sign = (pointsChange >= 0) ? "+" : "";
        return transactionDate + "\n" + sign + pointsChange + " Points\n" + note;
    }
}