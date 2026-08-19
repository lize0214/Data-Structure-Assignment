package Entity;

import java.time.LocalDateTime;
/**
 *
 * @author Chong Zhi Yi
 */
// Shared by Front Desk (checkout billing) and Walk-in Registration (on-arrival payment)
public class Payment {
    private String paymentId;
    private String confirmationNo;
    private double amount;
    private String method;
    private LocalDateTime timestamp;
    private String status;

    public Payment() {
    }

    public Payment(String paymentId, String confirmationNo, double amount, String method,
                   LocalDateTime timestamp, String status) {
        this.paymentId = paymentId;
        this.confirmationNo = confirmationNo;
        this.amount = amount;
        this.method = method;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getConfirmationNo() { return confirmationNo; }
    public void setConfirmationNo(String confirmationNo) { this.confirmationNo = confirmationNo; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // format: paymentId,confirmationNo,amount,method,timestamp,status
    public static Payment fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid Payment data format: " + line);
        }
        return new Payment(
                parts[0].trim(),
                parts[1].trim(),
                Double.parseDouble(parts[2].trim()),
                parts[3].trim(),
                LocalDateTime.parse(parts[4].trim()),
                parts[5].trim()
        );
    }

    public String toCsvLine() {
        return paymentId + "," + confirmationNo + "," + amount + "," + method + "," + timestamp + "," + status;
    }

    @Override
    public String toString() {
        return "Payment{" + paymentId + ", Booking:" + confirmationNo + ", RM" + amount + ", " + method + ", " + status + "}";
    }
}
