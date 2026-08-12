package Control;

import Entity.Booking;
import Entity.Payment;
import Utility.ControllerResult;
import Utility.FileUtility;
import Utility.ValidationUtility;

import java.time.LocalDateTime;

public class PaymentController {

    private static final String DEFAULT_PAYMENTS_FILE = "data/payments.txt";

    private final BookingController bookingController;
    private final String paymentsFile;
    private int nextPaymentSeq;

    public PaymentController(
            BookingController bookingController
    ) {
        this(
                bookingController,
                DEFAULT_PAYMENTS_FILE
        );
    }

    public PaymentController(
            BookingController bookingController,
            String paymentsFile
    ) {
        this.bookingController = bookingController;
        this.paymentsFile = paymentsFile;
        this.nextPaymentSeq =
                countExistingPayments() + 1;
    }

    private int countExistingPayments() {
        if (!FileUtility.fileExists(paymentsFile)) {
            return 0;
        }

        return FileUtility.readLines(paymentsFile).length;
    }

    private String validateConfirmationNo(
            String confNo
    ) {
        ValidationUtility.ValidationAccumulator acc =
                new ValidationUtility.ValidationAccumulator();

        acc.check(
                ValidationUtility.validateRequired(
                        confNo,
                        "Confirmation number"
                )
        );

        if (confNo != null
                && !confNo.trim().isEmpty()
                && !confNo.trim().matches("\\d{8}")) {

            acc.check(
                    "Confirmation number must be exactly 8 digits"
            );
        }

        return acc.hasErrors()
                ? acc.getErrorMessage()
                : null;
    }

    public ControllerResult processPayment(
            String confNo,
            String method,
            String resultingBookingStatus
    ) {
        String confError =
                validateConfirmationNo(confNo);

        if (confError != null) {
            return ControllerResult.fail(confError);
        }

        String methodError =
                ValidationUtility.validatePaymentMethod(
                        method
                );

        if (methodError != null) {
            return ControllerResult.fail(methodError);
        }

        Booking booking =
                bookingController.findByKey(
                        confNo.trim()
                );

        if (booking == null) {
            return ControllerResult.fail(
                    "No booking found for confirmation number: "
                    + confNo
            );
        }
        
        if (isPaid(confNo)) {
            return ControllerResult.fail(
                    "Payment has already been completed for booking #" + confNo.trim()
            );
        }

        double amount =
                FrontDeskController.calculateTotal(
                        booking
                );

        String paymentId =
                "P"
                + String.format(
                        "%04d",
                        nextPaymentSeq++
                );

        Payment payment =
                new Payment(
                        paymentId,
                        confNo.trim(),
                        amount,
                        method,
                        LocalDateTime.now(),
                        "Success"
                );

        FileUtility.appendLine(
                paymentsFile,
                payment.toCsvLine()
        );

        StringBuilder message =
                new StringBuilder(
                        String.format(
                                "Payment %s recorded: "
                                + "RM%.2f via %s for booking #%s",
                                paymentId,
                                amount,
                                method,
                                confNo.trim()
                        )
                );

        if (resultingBookingStatus != null
                && !resultingBookingStatus.isBlank()) {

            ControllerResult statusResult =
                    bookingController.updateBookingStatus(
                            confNo.trim(),
                            resultingBookingStatus
                    );

            if (statusResult.isOk()) {
                message.append(
                        " | Booking status updated to "
                ).append(
                        resultingBookingStatus
                );
            } else {
                message.append(
                        " | Warning: booking status not updated ("
                ).append(
                        statusResult.getMessage()
                ).append(
                        ")"
                );
            }
        }

        return ControllerResult.success(
                message.toString()
        );
    }

    /*
     * Returns all payments stored in payments.txt.
     */
    public Payment[] getAllPayments() {

        if (!FileUtility.fileExists(paymentsFile)) {
            return new Payment[0];
        }

        String[] lines =
                FileUtility.readLines(paymentsFile);

        int count = 0;

        for (String line : lines) {
            if (line != null && !line.isBlank()) {
                count++;
            }
        }

        Payment[] payments =
                new Payment[count];

        int index = 0;

        for (String line : lines) {

            if (line != null && !line.isBlank()) {

                payments[index++] =
                        Payment.fromCsvLine(line);
            }
        }

        return payments;
    }

    /*
     * Calculates the total revenue from successful payments.
     */
    public double getTotalRevenue() {

        double total = 0;

        for (Payment payment : getAllPayments()) {

            if ("Success".equalsIgnoreCase(
                    payment.getStatus()
            )) {
                total += payment.getAmount();
            }
        }

        return total;
    }

    /*
     * Counts the total number of successfully processed payments.
     */
    public int getTotalPaymentsProcessed() {

        int count = 0;

        for (Payment payment : getAllPayments()) {

            if ("Success".equalsIgnoreCase(
                    payment.getStatus()
            )) {
                count++;
            }
        }

        return count;
    }
    
    public boolean isPaid(String confNo) {
        if (confNo == null || confNo.trim().isEmpty()) {
            return false;
        }

        for (Payment payment : getAllPayments()) {
            if (payment.getConfirmationNo().equalsIgnoreCase(confNo.trim())
                    && "Success".equalsIgnoreCase(payment.getStatus())) {
                return true;
            }
        }

        return false;
    }
}