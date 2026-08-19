package Entity;

import java.time.LocalDate;

/**
 * Represents a points transaction for a member.
 *
 * @author Tan Pei Xing
 */
public class PointsTransaction {

    private String transactionId;
    private String memberId;
    private int pointsChange;   // Positive = earned/refunded, negative = spent/expired.
    private String type;        // EARN, REDEEM, UNDO, REGISTER, EXPIRE.
    private String note;        // Transaction description.
    private LocalDate transactionDate;
    private LocalDate expiryDate;      // Expiry date for earned points.
    private boolean expiryProcessed;   // Tracks whether expiry has been processed.

    // Creates an empty transaction.
    public PointsTransaction() {
    }

    // Creates a transaction without an expiry date.
    public PointsTransaction(String transactionId, String memberId, int pointsChange,
                              String type, String note, LocalDate transactionDate) {
        this(transactionId, memberId, pointsChange, type, note, transactionDate, null);
    }

    // Creates a transaction with an expiry date.
    public PointsTransaction(String transactionId, String memberId, int pointsChange,
                              String type, String note, LocalDate transactionDate, LocalDate expiryDate) {
        this(transactionId, memberId, pointsChange, type, note, transactionDate, expiryDate, false);
    }

    // Creates a transaction with complete expiry information.
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

    // Returns the transaction ID.
    public String getTransactionId() {
        return transactionId;
    }

    // Sets the transaction ID.
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    // Returns the member ID.
    public String getMemberId() {
        return memberId;
    }

    // Sets the member ID.
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    // Returns the points change.
    public int getPointsChange() {
        return pointsChange;
    }

    // Sets the points change.
    public void setPointsChange(int pointsChange) {
        this.pointsChange = pointsChange;
    }

    // Returns the transaction type.
    public String getType() {
        return type;
    }

    // Sets the transaction type.
    public void setType(String type) {
        this.type = type;
    }

    // Returns the transaction note.
    public String getNote() {
        return note;
    }

    // Sets the transaction note.
    public void setNote(String note) {
        this.note = note;
    }

    // Returns the transaction date.
    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    // Sets the transaction date.
    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    // Returns the expiry date.
    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    // Sets the expiry date.
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    // Checks whether expiry has been processed.
    public boolean isExpiryProcessed() {
        return expiryProcessed;
    }

    // Sets the expiry processed status.
    public void setExpiryProcessed(boolean expiryProcessed) {
        this.expiryProcessed = expiryProcessed;
    }

    // Creates a transaction object from a CSV line.
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

    // Converts the transaction to CSV format.
    public String toCsvLine() {
        String expiryPart = (expiryDate == null) ? "NONE" : expiryDate.toString();
        return transactionId + "," + memberId + "," + pointsChange + "," + type + "," + note + ","
                + transactionDate + "," + expiryPart + "," + expiryProcessed;
    }

    // Returns the transaction details for display.
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