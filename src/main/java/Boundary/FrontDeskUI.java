/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Boundary;

import Control.BookingController;
import Control.FrontDeskController;
import Control.PaymentController;
import Control.RoomController;
import Utility.ControllerResult;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class FrontDeskUI {

    private final FrontDeskController controller;
    private final PaymentController paymentController;
    private final RoomController roomController;
    private final Scanner scanner;

    public FrontDeskUI(BookingController bookingController, Scanner scanner) {
        this.scanner = scanner;
        roomController = new RoomController();
        paymentController = new PaymentController(bookingController);
        controller = new FrontDeskController(bookingController, roomController, paymentController);
    }

    // ───────────────────── Banners (Diet Cola font, patorjk.com/software/taag) ─────────────────────
    // Main "Front Desk" banner - original artwork.
    private static final String[] BANNER_MAIN = {
        "            .-._.---'                        .-.                           ",
        "          (_) /                      /     (_) )-.                 /       ",
        "             /--.).--..-._..  .-.---/---      /   \\    .-.  .     /-.     ",
        "            /   /    (   )  )/   ) /         /     \\ ./.-'_/ \\   /   )   ",
        "         .-/   /      `-'  '/   ( /       .-/.      )(__.'/ ._)_/    \\    ",
        "        (_/              .-.     `-      (_/  `----'     /                 ",
        "                   .--.-'                 .-.                              ",
        "                  (  (_) .-.  ).--.)   .-.`-'.-.    .-.                    ",
        "                   `-. ./.-' /    (   /  /  (     ./.-'_                   ",
        "                 _    )(__.'/      \\_/  (__. `---'(__.'                   ",
        "                (_.--'                                                     "
    };

    // Each submenu below gets its own Diet Cola banner (patorjk.com/software/taag).
    private static final String[] BANNER_SEARCH = {
        "                              .-.                            ",
        "                           .--.-'                            /   ",
        "                          (  (_)  .-.  .-.    ).--..-.      /-.  ",
        "                            `-. ./.-'_(  |   /    (        /   | ",
        "                          _    )(__.'  `-'-'/      `---'_.'    | ",
        "                         (_.--'                                  ",
        "                       .-.                                            ",
        "                      (_) )-.               /      .-.                ",
        "                         / __)  .-._..-._. /-.     `-'.  .-.    .-.   ",
        "                        /    `.(   )(   ) /   )   /    )/   )  (   )  ",
        "                       /'      )`-'  `-'_/    \\_.(__. '/   (    `-/-'",
        "                    (_/  `----'                             `--._/    "
    };
    
    private static final String[] BANNER_BOOKINGENQUIRY = {
        "                .-.                                            \n" +
        "               (_) )-.               /      .-.                \n" +
        "                  / __)  .-._..-._. /-.     `-'.  .-.    .-.   \n" +
        "                 /    `.(   )(   ) /   )   /    )/   )  (   )  \n" +
        "                /'      )`-'  `-'_/    \\_.(__. '/   (    `-/-' \n" +
        "             (_/  `----'                             `--._/    \n" +
        "                       .-                                      \n" +
        "               .---;`-'                     .-.                \n" +
        "              (   (_) .  .-.  .-.  )  (     `-' ).--..    .-.  \n" +
        "               )--     )/   )(   )(    )   /   /      )  /     \n" +
        "              (      /'/   (  `-(  `--':_.(__./      (_.'      \n" +
        "              `\\___.'       `-   `-'               ..-._)       "
    };

    private static final String[] BANNER_CHECKROOM = {
        "                      .-._   .-._.                                                           \n" +
        "                    ..' (_)`-'  /                  /                                         \n" +
        "                    |          /-.   .-.  .-.     /-.                                        \n" +
        "                    |    _    /   |./.-'_(       /   )                                       \n" +
        "                    `.    )_.'    |(__.'  `---'_/    \\                                       \n" +
        "                      `--'                                                                   \n" +
        "      .-.                                       .-.                         \n" +
        "     (_) )-.                              .--.-'   /          /             \n" +
        "        /   \\  .-._..-._..  .-. .-.      (  (_)---/---.-. ---/---)  (   .   \n" +
        "       /     )(   )(   )  )/   )   )      `-.    /   (  |   /   (    ) / \\  \n" +
        "    .-/  `--'  `-'  `-'  '/   /   (     _    )  /     `-'-'/     `--':/ ._) \n" +
        "   (_/     `-._)                   `-' (_.--'                        /      "
    };

    private static final String[] BANNER_BILLING = {
        "                               .-.                 .    .                      \n" +
        "..-.     .-..-.                (_) )-.      .-.   /    /    .-.                \n" +
        "   )   /   `-' .-. `)    (       / __)      `-'  /    /     `-'.  .-.    .-.   \n" +
        "  /   /   /  ./.-'_/  .   )     /    `.    /    /    /     /    )/   )  (   )  \n" +
        " (  .' _.(__.(__.'(_.' `-'     /'      )_.(__._/_.-_/_.-_.(__. '/   (    `-/-' \n" +
        "  \\/                        (_/  `----'                              `--._/    \n" +
        "                  .-.                                  .                        \n" +
        "                 (_) )-.            /           .-.   /                        \n" +
        "                    /   \\    .-.---/---.-.      `-'  /   .                     \n" +
        "                   /     \\ ./.-'_ /   (  |     /    /   / \\                    \n" +
        "                .-/.      )(__.' /     `-'-'_.(__._/_.-/ ._)                    \n" +
        "               (_/  `----'                            /         "
    };

    private static final String[] BANNER_CHECKOUT = {
        "         .-._   .-._.                                                \n" +
        "       ..' (_)`-'  /                  /         .--.    .-      /    \n" +
        "       |          /-.   .-.  .-.     /-.       /    )`-')  (---/---  \n" +
        "       |    _    /   |./.-'_(       /   )`-=-./    /   (    ) /      \n" +
        "       `.    )_.'    |(__.'  `---'_/    \\    (    /     `--':/       \n" +
        "         `--'               .-.               `-.'                   \n" +
        "                     .--.`-'                  /                      \n" +
        "                    /  (_;  )  (   .-.  . ---/---                    \n" +
        "                   /       (    )./.-'_/ \\  /                        \n" +
        "                  (     --;-`--':(__.'/ ._)/                         \n" +
        "                   `.___.'           /                               "
    };
    
    private static final String[] BANNER_MOVEMENT = {
        "       .--------'      .                          .-.                    \n" +
        "      (_)   /         /                    .--.`-'                  /    \n" +
        "           /.-._..-../ .-.  .    .-..     /  (_;  )  (   .-.  . ---/---  \n" +
        "          /(   )(   / (  |   )  /  / \\   /       (    )./.-'_/ \\  /    \n" +
        "       .-/._`-'  `-'-..`-'-'(_.'  / ._) (     --;-`--':(__.'/ ._)/       \n" +
        "      (_/  `-            ..-._)  /       `.___.'           /             \n" +
        "                .-.                                                       \n" +
        "                  /|/|                                          /         \n" +
        "                 /   | .-._.)   .-..-..  .-. .-.   .-..  .-.---/---       \n" +
        "                /    |(   )(   / ./.-'_)/   )   )./.-'_)/   ) /           \n" +
        "           .-' /     | `-'  \\_/  (__.''/   /   ( (__.''/   ( /           \n" +
        "          (__.'      `.                         `-'         `- "
    };

    private static final String[] BANNER_REPORTS = {
        "                   .-.                                     ",
        "                  (_) )-.                           /      ",
        "                     /   \\   .-. .-.  .-._.).--.---/---.   ",
        "                    /     )./.-'_/  )(   )/       /   / \\  ",
        "                 .-/  `--' (__.'/`-'  `-'/       /   / ._) ",
        "                (_/     `-._)  /                    /      "
    };

    private static final String[] BANNER_BOOKINGS = {
        "                                        .-.                                             \n" +
        "        ..-.     .-..-.                (_) )-.               /      .-.                 \n" +
        "            )   /   `-' .-. `)    (       / __)  .-._..-._. /-.     `-'.  .-.    .-.    \n" +
        "           /   /   /  ./.-'_/  .   )     /    `.(   )(   ) /   )   /    )/   )  (   )   \n" +
        "          (  .' _.(__.(__.'(_.' `-'     /'      )`-'  `-'_/    \\_.(__. '/   (    `-/-'  \n" +
        "           \\/                        (_/  `----'                             `--._/     \n" +
        "                              .-.                                                 \n" +
        "                             / (_)     .-.       /                                \n" +
        "                            /          `-' . ---/---                              \n" +
        "                           /          /   / \\  /                                  \n" +
        "                        .-/.    .-._.(__./ ._)/                                   \n" +
        "                       (_/ `-._.        /                                         "
    };

    private void printBanner(String[] banner) {
        if (banner == null || banner.length == 0) {
            return;
        }
        System.out.println();
        for (String line : banner) {
            System.out.println(line);
        }
    }

    // ───────────────────── UX Helpers ─────────────────────

    /**
     * "Clears" the console. NetBeans' built-in Output panel is NOT a real
     * terminal - it ignores ANSI escape codes entirely, so pushing enough
     * blank lines through to scroll old content out of view is what
     * reliably works there.
     */
    private void clearScreen() {
        for (int i = 0; i < 60; i++) {
            System.out.println();
        }
    }

    private void pressEnterToContinue() {
        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }
    
    private boolean confirmAction(String message) {

        while (true) {

            System.out.print(
                    message + " (Y/N): "
            );

            String input =
                    scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Y")) {
                return true;
            }

            if (input.equalsIgnoreCase("N")) {
                return false;
            }

            System.out.println(
                    "Invalid input. Please enter Y or N.\n"
            );
        }
    }

    // ───────────────────── Main Menu ─────────────────────

    public void run() {
        boolean running = true;

        while (running) {
            clearScreen();
            printMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    runBookingEnquiryMenu();
                    break;

                case "2":
                    handleBilling();
                    pressEnterToContinue();
                    break;

                case "3":
                    handleCheckOut();
                    pressEnterToContinue();
                    break;

                case "4":
                    runGuestMovementMenu();
                    break;

                case "5":
                    runReportsMenu();
                    break;

                case "0": 
                    running = false;
                    System.out.println(
                            "Returning to Main Menu...\n"
                    );
                    clearScreen();
                    break;

                default:
                    System.out.println(
                            "\nInvalid option. Please enter a number from 0 to 5."
                    );
                    pressEnterToContinue();
                    break;
            }
        }
    }

    private void printMenu() {
        printBanner(BANNER_MAIN);
        System.out.print(
                "------------------------------------------------------------------------------\n" +
                "                          1. Booking Enquiry                                   \n" +
                "                          2. View Billing Details                              \n" +
                "                          3. Check-Out Guest                                  \n" +
                "                          4. Today's Guest Movement                           \n" +
                "                          5. Generate Reports                                 \n" +
                "                          0. Return to Main Menu                              \n" +
                "------------------------------------------------------------------------------\n" +
                "Enter your choice: "
        );
    }

    private String readConfNo() {
        while (true) {
            System.out.print("Enter 8-digit confirmation number: ");
            String confNo = scanner.nextLine().trim();

            if (!confNo.matches("\\d{8}")) {
                System.out.println(
                        "Invalid format. Confirmation number must be exactly 8 digits.\n"
                );
                continue;
            }

            ControllerResult result =
                    controller.searchByConfirmationNo(confNo);

            if (!result.isOk()) {
                System.out.println(
                        "Booking not found. Please try again.\n"
                );
                continue;
            }

            return confNo;
        }
    }

    /**
     * Search groups both lookup strategies under one screen: exact
     * confirmation-number search hits the HashTable index (O(1) average),
     * name search falls back to a plain linear scan since a partial,
     * case-insensitive match can't be hashed to a single bucket the way an
     * exact key can. Keeping both here makes the trade-off easy to point to.
     */
    
    private void runBookingEnquiryMenu() {

    boolean running = true;

        while (running) {

            clearScreen();
            printBanner(BANNER_BOOKINGENQUIRY);

            System.out.print(
                    "------------------------------------------------------------------------------\n" +
                    "                         1. Search Booking                                    \n" +
                    "                         2. View Booking List                                 \n" +
                    "                         3. Check Assigned Room Status                       \n" +
                    "                         0. Back                                             \n" +
                    "------------------------------------------------------------------------------\n" +
                    "Enter your choice: "
            );

            String choice = scanner.nextLine().trim();

            switch (choice) {

            case "1":
                runSearchMenu();
                break;

            case "2":
                handleViewBookingList();
                break;

            case "3":
                handleAvailability();
                pressEnterToContinue();
                break;

            case "0":
                running = false;
                break;

            default:
                System.out.println(
                        "\nInvalid option. Please enter a number from 0 to 3."
                );
                pressEnterToContinue();
                break;
        }
        }
    }
    
    private void runSearchMenu() {
        boolean inSearch = true;

        while (inSearch) {
            clearScreen();
            printBanner(BANNER_SEARCH);

            System.out.print(
                    "----------------------------------------------------------------------------------------------\n" +
                    "                                1. By Confirmation Number                            \n" +
                    "                                2. By Guest Name                                     \n" +
                    "                                0. Back                                              \n" +
                    "----------------------------------------------------------------------------------------------\n" +
                    "Enter your choice: "
            );

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    String confNo = readConfNo();
                    printResult(
                            controller.searchByConfirmationNo(confNo)
                    );
                    pressEnterToContinue();
                    break;

                case "2":
                    System.out.print(
                            "Enter guest name (or part of it): "
                    );

                    String name = scanner.nextLine().trim();

                    printResult(
                            controller.searchByGuestName(name)
                    );

                    pressEnterToContinue();
                    break;

                case "0":
                    inSearch = false;
                    break;

                default:
                    System.out.println(
                            "\nInvalid option. Please enter a number from 0 to 2."
                    );
                    pressEnterToContinue();
                    break;
            }
        }
    }

    private void handleAvailability() {
        clearScreen();
        printBanner(BANNER_CHECKROOM);
        System.out.println("------------------------------------------------------------------------------");
        String confNo = readConfNo();
        ControllerResult result = controller.checkAssignedRoomStatus(confNo);
        printResult(result);
    }

    private void handleBilling() {
        clearScreen();
        printBanner(BANNER_BILLING);
        System.out.print("------------------------------------------------------------------------------\n");
        String confNo = readConfNo();
        ControllerResult result = controller.getBillingDetails(confNo);
        printResult(result);
    }

    private void handleCheckOut() {

        clearScreen();
        printBanner(BANNER_CHECKOUT);

        System.out.println(
            "------------------------------------------------------------------------------"
        );

        String confNo = readConfNo();

        // Get billing details
        ControllerResult billing =
                controller.getBillingDetails(confNo);

        if (!billing.isOk()) {
            printResult(billing);
            return;
        }

        // Check whether already checked out
        if (controller.isBookingCheckedOut(confNo)) {

            System.out.println(
                "\nThis booking has already been checked out."
            );

            return;
        }

        // Only CheckedIn guests can proceed with checkout
        if (!controller.isBookingCheckedIn(confNo)) {

            System.out.println(
                "\nOnly guests with CheckedIn status can be checked out."
            );

            return;
        }

        // ───────────────── Check Checkout Date ─────────────────

        LocalDate today =
                LocalDate.now();

        LocalDate scheduledCheckOut =
                controller.getScheduledCheckOutDate(confNo);

        String timing =
                controller.getCheckOutTiming(confNo);

        System.out.println();

        System.out.println(
                "Scheduled Check-Out : " + scheduledCheckOut
        );

        System.out.println(
                "Current Date        : " + today
        );

        // Early checkout
        if ("EARLY".equals(timing)) {

            System.out.println(
                "\nThis is an early check-out."
            );

            if (!confirmAction(
                    "Proceed with early check-out?"
            )) {

                System.out.println(
                    "\nCheck-out cancelled."
                );

                return;
            }

        // Late checkout
        } else if ("LATE".equals(timing)) {

            long overdueDays =
                    java.time.temporal.ChronoUnit.DAYS.between(
                            scheduledCheckOut,
                            today
                    );

            System.out.println(
                "\nGuest is "
                + overdueDays
                + " day(s) past the scheduled check-out date."
            );

            if (!confirmAction(
                    "Proceed with late check-out?"
            )) {

                System.out.println(
                    "\nCheck-out cancelled."
                );

                return;
            }

        // Normal checkout
        } else {

            System.out.println(
                "\nCheck-out date confirmed."
            );
        }

        // Show billing after checkout-date validation
        System.out.println(
                billing.getMessage()
        );

        // ───────────────── Payment ─────────────────

        if (!paymentController.isPaid(confNo)) {

            System.out.println(
                "\n------------------------------------------------------------------------------"
            );

            System.out.println(
                "                              PAYMENT METHOD"
            );

            System.out.println(
                "------------------------------------------------------------------------------"
            );

            System.out.println(
                "                         1. Cash"
            );

            System.out.println(
                "                         2. Card"
            );

            System.out.println(
                "                         3. E-wallet"
            );

            System.out.println(
                "                         0. Cancel"
            );

            System.out.println(
                "------------------------------------------------------------------------------"
            );

            System.out.print(
                "Select payment method: "
            );

            String choice =
                    scanner.nextLine().trim();

            String method;

            switch (choice) {

                case "1":
                    method = "Cash";
                    break;

                case "2":
                    method = "Card";
                    break;

                case "3":
                    method = "E-wallet";
                    break;

                case "0":
                    System.out.println(
                        "\nPayment cancelled. Check-out not completed."
                    );
                    return;

                default:
                    System.out.println(
                        "\nInvalid payment method. Check-out not completed."
                    );
                    return;
            }

            System.out.println();

            System.out.println(
                    "Selected Payment Method : " + method
            );

            System.out.println();

            if (!confirmAction(
                    "Confirm payment and check-out?"
            )) {

                System.out.println(
                        "\nCheck-out cancelled."
                );

                return;
            }

            // Process payment only
            ControllerResult paymentResult =
                    paymentController.processPayment(
                        confNo,
                        method,
                        null
                    );

            if (!paymentResult.isOk()) {
                printResult(paymentResult);
                return;
            }

            printPaymentReceipt(
                    confNo,
                    method,
                    billing.getMessage(),
                    paymentResult.getMessage()
            );

        } else {

            System.out.println(
                "\nPayment has already been completed."
            );

            System.out.println();

            if (!confirmAction(
                    "Confirm guest check-out?"
            )) {

                System.out.println(
                        "\nCheck-out cancelled."
                );

                return;
            }
        }

        // ───────────────── Complete Checkout ─────────────────

        ControllerResult checkoutResult =
                controller.updateBookingStatus(
                    confNo,
                    "CheckedOut"
                );

        if (!checkoutResult.isOk()) {
            printResult(checkoutResult);
            return;
        }

        // Get room assigned to booking
        String roomNo =
                controller.getRoomNoForBooking(confNo);

        // Room becomes Dirty after checkout
        if (roomNo != null) {

            ControllerResult roomResult =
                    roomController.updateStatus(
                        roomNo,
                        "Dirty"
                    );

            if (!roomResult.isOk()) {

                System.out.println(
                    "\nWarning: Room status could not be updated."
                );

                System.out.println(
                    roomResult.getMessage()
                );
            }
        }

        controller.refreshIndex();

        // ───────────────── Success Output ─────────────────

        System.out.println();

        System.out.println(
            "------------------------------------------------------------------------------"
        );

        System.out.println(
            "                           CHECK-OUT SUCCESSFUL"
        );

        System.out.println(
            "------------------------------------------------------------------------------"
        );

        System.out.println(
            "Confirmation No. : " + confNo
        );

        if (roomNo != null) {

            System.out.println(
                "Room No.         : " + roomNo
            );

            System.out.println(
                "Room Status      : Dirty"
            );
        }

        System.out.println(
            "Booking Status   : CheckedOut"
        );

        System.out.println(
            "------------------------------------------------------------------------------"
        );
    }
    
    private void printPaymentReceipt(
        String confNo,
        String method,
        String billingDetails,
        String paymentMessage) {

        System.out.println();

        System.out.println(
                "=============================================================================="
        );

        System.out.println(
                "                               PAYMENT RECEIPT"
        );

        System.out.println(
                "=============================================================================="
        );

        System.out.println(
                "Confirmation No. : " + confNo
        );

        System.out.println(
                "Payment Method   : " + method
        );

        System.out.println(
                "------------------------------------------------------------------------------"
        );

        System.out.println(
                billingDetails
        );

        System.out.println(
                "------------------------------------------------------------------------------"
        );

        System.out.println(
                paymentMessage
        );

        System.out.println(
                "Payment Status   : Success"
        );

        System.out.println(
                "=============================================================================="
        );
    }

    private void handleViewBookingList() {
        boolean running = true;

        while (running) {
            clearScreen();
            printBanner(BANNER_BOOKINGS);

            System.out.print(
                    "----------------------------------------------------------------------------------------------\n" +
                    "                                Sort by:                                                   \n" +
                    "                                1. Check-in Date                                           \n" +
                    "                                2. Room Type                                               \n" +
                    "                                3. Status                                                  \n" +
                    "                                0. Back                                                    \n" +
                    "----------------------------------------------------------------------------------------------\n" +
                    "Enter your choice: "
            );

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    printResult(
                            controller.viewAllBookings("checkin")
                    );
                    pressEnterToContinue();
                    break;

                case "2":
                    printResult(
                            controller.viewAllBookings("roomtype")
                    );
                    pressEnterToContinue();
                    break;

                case "3":
                    printResult(
                            controller.viewAllBookings("status")
                    );
                    pressEnterToContinue();
                    break;

                case "0":
                    running = false;
                    break;

                default:
                    System.out.println(
                            "\nInvalid option. Please enter a number from 0 to 3."
                    );
                    pressEnterToContinue();
                    break;
            }
        }
    }
    
    private void runGuestMovementMenu() {

        boolean running = true;

        while (running) {

            clearScreen();
            printBanner(BANNER_MOVEMENT);

            System.out.print(
                    "------------------------------------------------------------------------------\n" +
                    "                         1. Today's Arrivals                                  \n" +
                    "                         2. Today's Departures                                \n" +
                    "                         0. Back                                              \n" +
                    "------------------------------------------------------------------------------\n" +
                    "Enter your choice: "
            );

            String choice =
                    scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    System.out.println(
                            "\n                           TODAY'S ARRIVALS"
                    );

                    System.out.println(
                            "------------------------------------------------------------------------------"
                    );

                    printResult(
                            controller.getTodaysArrivals()
                    );

                    pressEnterToContinue();
                    break;

                case "2":
                    System.out.println(
                            "\n                           TODAY'S DEPARTURES"
                    );

                    System.out.println(
                            "------------------------------------------------------------------------------"
                    );

                    printResult(
                            controller.getTodaysDepartures()
                    );

                    pressEnterToContinue();
                    break;

                case "0":
                    running = false;
                    break;

                default:
                    System.out.println(
                            "\nInvalid option. Please enter a number from 0 to 2."
                    );
                    pressEnterToContinue();
                    break;
            }
        }
    }

    // ───────────────────── Reports Submenu ─────────────────────

    private void runReportsMenu() {
        boolean inReports = true;
        while (inReports) {
            clearScreen();
            printBanner(BANNER_REPORTS);
            System.out.print(
                    "------------------------------------------------------------------------------\n" +
                    "                   1. Revenue Report                                           \n" +
                    "                   2. Outstanding Payments Report                              \n" +
                    "                   3. Room Status Report                                       \n" +
                    "                   4. Operational Report                                      \n" +
                    "                   0. Back                                                     \n" +
                    "------------------------------------------------------------------------------\n" +
                    "Enter your choice: "
            );
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleRevenueReport();
                    pressEnterToContinue();
                    break;
                case "2":
                    handleOutstandingPaymentsReport();
                    pressEnterToContinue();
                    break;
                case "3":
                    handleRoomStatusReport();
                    pressEnterToContinue();
                    break;
                case "4":
                    printResult(controller.quickStats());
                    pressEnterToContinue();
                    break;
                case "0":
                    inReports = false;
                    break;
                default:
                    System.out.println(
                            "\nInvalid option. Please enter a number from 0 to 4."
                    );
                    pressEnterToContinue();
                    break;
            }
        }
    }

    private void handleRevenueReport() {

        System.out.println("------------------------------------------------------------------------------");
        System.out.println("                              REVENUE REPORT");
        System.out.println("------------------------------------------------------------------------------");
     
        LocalDate start =
                readOptionalDate(
                        "Filter check-out date FROM (YYYY-MM-DD, blank to skip): "
                );

        LocalDate end =
                readOptionalDate(
                        "Filter check-out date TO   (YYYY-MM-DD, blank to skip): "
                );

        System.out.println();

        ControllerResult result =
            controller.revenueReport(start, end);

        printResult(result);
    }
    
    private void handleOutstandingPaymentsReport() {

        System.out.println("------------------------------------------------------------------------------");
        System.out.println("                     OUTSTANDING PAYMENTS REPORT");
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("Booking Status Options:");
        System.out.println("Confirmed | CheckedIn | CheckedOut | Cancelled");
        System.out.println("------------------------------------------------------------------------------");
        System.out.print("Filter by booking status (leave blank for all): ");

        String status = scanner.nextLine().trim();

        LocalDate start =
                readOptionalDate(
                        "Filter check-out date FROM (YYYY-MM-DD, blank to skip): "
                );

        LocalDate end =
                readOptionalDate(
                        "Filter check-out date TO   (YYYY-MM-DD, blank to skip): "
                );

        System.out.println();

        ControllerResult result =
                controller.outstandingPaymentsReport(
                        status.isBlank()
                                ? null
                                : status,
                        start,
                        end
                );

        printResult(result);
    }
        
        private void handleRoomStatusReport() {
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("                            ROOM STATUS REPORT");
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("Room Status Options:");
        System.out.println("Available | Occupied | Dirty | CleaningInProgress | Inspected | ");
        System.out.println("ReadyForCheckIn | Maintenance");
        System.out.println("------------------------------------------------------------------------------");
        System.out.print("Filter by room status (leave blank for all): ");

        String statusFilter = scanner.nextLine().trim();
        System.out.println();
        ControllerResult result = controller.roomStatusReport(statusFilter);

        printResult(result);
    }

    private LocalDate readOptionalDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD, or leave blank to skip.");
            }
        }
    }

    private void printResult(ControllerResult result) {
        if (result.isOk()) {
            System.out.println(result.getMessage());
        } else {
            System.out.println("Error: " + result.getMessage());
        }
    }
    
}