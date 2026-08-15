/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt
 * to change this license
 */
package Boundery;

import Control.GuestController;
import Control.RoomController;
import Control.BookingController;
import Control.WalkInRegistrationController;
import Entity.Booking;
import Utility.ControllerResult;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * @author Chin Yik Heng
 */
public class BookingUI {

    private static final int FIELD_LABEL_WIDTH = 36;

    private final GuestController guestController;
    private final RoomController roomController;
    private final BookingController bookingController;
    private final Scanner scanner;

    public BookingUI() {

        this.guestController =
                new GuestController();

        this.roomController =
                new RoomController();

        this.bookingController =
                new BookingController(
                        guestController,
                        roomController
                );

        this.scanner =
                new Scanner(System.in);
    }

    // ───────────────────── Banners (Diet Cola font, patorjk.com/software/taag) ─────────────────────

    private static final String[] BANNER_BOOKING = {"   .-.                              .-.           .----.     .-.             .-.", "  (_) )-.      .--.    .-.--.    .-(_) )  .'-       /   `      /  |   .--.`-'   ", "     / __)    /    )`-' /    )`-'     /  /         /          /\\  |  /  (_;     ", "    /    `.  /    /    /    /       _/_.'         /          /  \\ | /           ", "   /'      )(    /    (    /     .  /   \\        /      .-' /    \\|(     --;-   ", "(_/  `----'  `-.'      `-.'     (_.'     `-'.---------'(__.'      `.`.___.'     "};

    private static final String[] BANNER_MAIN = combineVerticalBanner(
            firstBannerLines(WalkInRegistrationUI.BANNER_MAIN, 6),
            new String[]{"                                      &"},
            BANNER_BOOKING);

    private static final String[] BANNER_WAITING_LIST = {"                  /\\        .----.  .--------'  .----.     .-.             .-.        .-.     .----.        .-..--------' ", "..-.     .-.  _  / |          /   `(_)   /        /   `      /  |   .--.`-'          / (_)      /   ` .--.-'  (_)   /     ", "   )   (     (  /  |  .      /          /        /          /\\  |  /  (_;           /          /     (  (_)        /      ", "  /     \\     `/.__|_.'     /          /        /          /  \\ | /                /          /       `-.         /       ", " (   .   ).:' /    |       /        .-/._      /      .-' /    \\|(     --;-     .-/.    .-.  /      _    )     .-/._      ", "  `-' `-'(__.'     `-'.---------'  (_/  `-.---------'(__.'      `.`.___.'      (_/ `-._..---------'(_.--'     (_/  `-     "};

    private static final String[] BANNER_REG_REPORT = {"   .-.                .-       .-.    .-.                .-.-.                  .-.        .--------' ", "  (_) )-.     .---;`-'  .--.`-'      (_) )-.     .---;`-' (_) )-.    .--.    .-(_) )-.    (_)   /     ", "     /   \\   (   (_)   /  (_;           /   \\   (   (_)      /   \\  /    )`-'     /   \\        /      ", "    /     )   )--     /                /     )   )--        /     )/    /        /     )      /       ", " .-/  `--'   (      /(     --;-     .-/  `--'   (      / .-/  `--'(    /      .-/  `--'    .-/._      ", "(_/     `-._)`\\___.'  `.___.'      (_/     `-._)`\\___.' (_/        `-.'      (_/     `-._)(_/  `-     "};

    // Reuse the project's embedded Diet Cola artwork for the two submenu pages.
    private static final String[] BANNER_STANDARD_BOOKING = BANNER_BOOKING;
    private static final String[] BANNER_ADD = {
        "         /\\     .-.        .-.       ",
        "     _  / |    (_) )-.    (_) )-.    ",
        "    (  /  |  .    /   \\      /   \\   ",
        "     `/.__|_.'   /     \\    /     \\  ",
        " .:' /    |   .-/.      ).-/.      ) ",
        "(__.'     `-'(_/  `----'(_/  `----'  "
    };

    private static final String[] BANNER_MODIFY = {
        "      .-.                 .-.           .----.  .-._.---'    ",
        "        /|/|   .--.    .-(_) )-.          /   `(_) /.-.   .- ",
        "       /   |  /    )`-'     /   \\        /        /--./  (   ",
        "      /    | /    /        /     \\      /        /   (    )  ",
        " .-' /     |(    /      .-/.      )    /      .-/  .  `..'   ",
        "(__.'      `.`-.'      (_/  `----'.---------'(_/  (__.-'     "
    };

    private static final String[] BANNER_CANCEL = {
        "  .-._   .-._. /\\        .-.     .-._   .-._.   .-   .-.   ",
        "..' (_)`-'  _  / |          /  |..' (_)`-'.---;`-'    / (_)  ",
        "|         (  /  |  .      /\\  ||       (   (_)     /       ",
        "|    _     `/.__|_.'     /  \\ ||    _   )--       /        ",
        "`.    ).:' /    |   .-' /    \\|`.    ) (      /.-/.    .-. ",
        "  `--'(__.'     `-'(__.'      `. `--'  `\\___.'(_/ `-._.    "
    };
    private static final String[] BANNER_ADD_BOOKING =
            combineVerticalBanner(BANNER_ADD, new String[]{""}, BANNER_BOOKING);
    private static final String[] BANNER_MODIFY_BOOKING =
            combineVerticalBanner(BANNER_MODIFY, new String[]{""}, BANNER_BOOKING);
    private static final String[] BANNER_CANCEL_BOOKING =
            combineVerticalBanner(BANNER_CANCEL, new String[]{""}, BANNER_BOOKING);
    private static final String[] BANNER_VIEW_WORD = {
        "          .----.         .-          ",
        "..-.     .-./   `.---;`-'..-.     .-.",
        "   )   /   /    (   (_)     )   (    ",
        "  /   /   /      )--       /     \\   ",
        " (  .'   /      (      /  (   .   )  ",
        "  \\/.---------' `\\___.'    `-' `-'   "
    };
    private static final String[] BANNER_VIEW_REPORT =
            combineBannerParts(BANNER_VIEW_WORD, BANNER_REG_REPORT, 36);
    private static final String[] BANNER_BOOKING_REPORT =
            combineBannerParts(BANNER_BOOKING, BANNER_REG_REPORT, 36);
    private static final String[] BANNER_AVAILABLE_WORD = {
        "         /\\                /\\        .----.      .-.          /\\     .-.            .-.           .- ",
        "     _  / | ..-.     .-._  / |          /   `    / (_)     _  / |    (_) )-.        / (_)  .---;`-'   ",
        "    (  /  |  . )   /  (  /  |  .      /        /         (  /  |  .    / __)      /      (   (_)     ",
        "     `/.__|_.'/   /    `/.__|_.'     /        /           `/.__|_.'   /    `.    /        )--        ",
        " .:' /    |  (  .' .:' /    |       /      .-/.    .-..:' /    |     /'      ).-/.    .-.(      /    ",
        "(__.'     `-' \\/  (__.'     `-'.---------'(_/ `-._.  (__.'     `-'(_/  `----'(_/ `-._.   `\\___.'     "
    };
    private static final String[] BANNER_ROOM_WORD = {
        "   .-.                           .-.     ",
        "  (_) )-.      .--.    .-.--.    .-/|/|  ",
        "     /   \\    /    )`-' /    )`-' /   |  ",
        "    /     )  /    /    /    /    /    |  ",
        " .-/  `--'  (    /    (    /.-' /     |  ",
        "(_/     `-._)`-.'      `-.'(__.'      `. "
    };
    private static final String[] BANNER_OVERVIEW_WORD = {
        "                                .-.-.               .----.         .-          ",
        "   .--.    .-..-.     .-..---;`-' (_) )-.  ..-.     .-. /   `.---;`-'..-.     .-. ",
        "  /    )`-'     )   /  (   (_)      /   \\    )   /   /    (   (_)     )   (    ",
        " /    /        /   /    )--        /     )  /   /   /      )--       /     \\   ",
        "(    /        (  .'    (      / .-/  `--'  (  .'   /      (      /  (   .   )  ",
        " `-.'          \\/      `\\___.' (_/     `-._)\\/.---------' `\\___.'    `-' `-'   "
    };
    private static final String[] BANNER_AVAILABLE_ROOM_OVERVIEW =
            combineVerticalBanner(BANNER_AVAILABLE_WORD, BANNER_ROOM_WORD, BANNER_OVERVIEW_WORD);

    private static String[] combineVerticalBanner(String[] first, String[] middle, String[] last) {
        String[] combined = new String[first.length + middle.length + last.length];
        int index = 0;
        for (String line : first) combined[index++] = line;
        for (String line : middle) combined[index++] = line;
        for (String line : last) combined[index++] = line;
        return combined;
    }

    private static String[] firstBannerLines(String[] banner, int count) {
        String[] selected = new String[count];
        for (int i = 0; i < count; i++) selected[i] = banner[i];
        return selected;
    }

    private static String[] combineBannerParts(String[] left, String[] source, int sourceStart) {
        String[] combined = new String[left.length];
        for (int i = 0; i < left.length; i++) {
            combined[i] = left[i] + "   " + source[i].substring(sourceStart);
        }
        return combined;
    }

    private void printBanner(String[] banner) {
        if (banner == null || banner.length == 0) {
            return;
        }
        System.out.println();
        for (String line : banner) {
            System.out.println(line);
        }
    }

    /**
     * "Clears" the console. NetBeans' Output panel has no real clear API,
     * so this pushes enough blank lines through that old content scrolls
     * out of view - same approach used across the rest of the system.
     */
    private void clearScreen() {
        for (int i = 0; i < 60; i++) {
            System.out.println();
        }
    }

    /**
     * Waits for the user to press ENTER before returning to the menu,
     * so report output doesn't flash by immediately.
     */
    private void pressEnterToContinue() {
        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }

    private String fieldPrompt(String label) {
        return String.format("%-" + FIELD_LABEL_WIDTH + "s: ", label);
    }

    // ───────────────────── Main Menu ─────────────────────

    public void run() {

        boolean running = true;

        while (running) {

            printMenu();

            String choice =
                    scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    handleWalkInRegistration();
                    break;

                case "2":
                    handleStandardBooking();
                    break;

                case "3":
                    handleViewAvailableRooms();
                    break;

                case "4":
                    handleViewReport();
                    break;

                case "0":
                    running = false;

                    System.out.println(
                            "\nReturning to Main Menu...\n"
                    );
                    break;

                default:
                    System.out.println(
                            "\nInvalid option. "
                            + "Please enter 0 to 4.\n"
                    );
            }
        }
    }

    private void printMenu() {

        clearScreen();
        printBanner(BANNER_MAIN);

        System.out.print(
                "\n"
                + "------------------------------------------------------------------------------\n"
                + "                     WALK-IN REGISTRATION & STANDARD BOOKING                  \n"
                + "------------------------------------------------------------------------------\n"
                + "                   1. Walk-In Registration                                     \n"
                + "                   2. Standard Booking                                         \n"
                + "                   3. Available Room Overview                                  \n"
                + "                   4. View Report                                              \n"
                + "                   0. Return to Main Menu                                      \n"
                + "------------------------------------------------------------------------------\n"
                + "Enter your choice: "
        );
    }

    /**
     * Opens the walk-in registration menu.
     */
    private void handleWalkInRegistration() {

        WalkInRegistrationUI walkInRegistrationUI =
                new WalkInRegistrationUI(
                        guestController,
                        roomController,
                        bookingController,
                        scanner
                );

        walkInRegistrationUI.run();
    }

    private void handleStandardBooking() {
        boolean running = true;
        while (running) {
            clearScreen();
            printBanner(BANNER_STANDARD_BOOKING);
            System.out.print("\n------------------------------------------------------------------------------\n"
                    + "                              STANDARD BOOKING                                \n"
                    + "------------------------------------------------------------------------------\n"
                    + "                   1. Add Booking                                             \n"
                    + "                   2. Modify Booking                                          \n"
                    + "                   3. Cancel Booking                                          \n"
                    + "                   0. Back                                                     \n"
                    + "------------------------------------------------------------------------------\n"
                    + "Enter your choice: ");
            switch (scanner.nextLine().trim()) {
                case "1": addStandardBooking(); break;
                case "2": modifyStandardBooking(); break;
                case "3": cancelStandardBooking(); break;
                case "0": running = false; break;
                default:
                    System.out.println("Invalid option. Please enter 0 to 3.");
                    pressEnterToContinue();
            }
        }
    }

    private void addStandardBooking() {
        clearScreen();
        printBanner(BANNER_ADD_BOOKING);
        System.out.print("\n------------------------------------------------------------------------------\n"
                + "                                 ADD BOOKING                                  \n"
                + "------------------------------------------------------------------------------\n");
        String guestId = guestController.generateNextGuestId();
        System.out.println(fieldPrompt("Generated Guest ID") + guestId);
        String guestName = readValidGuestName(fieldPrompt("Guest Name"));
        String guestContact = readValidGuestContact(fieldPrompt("Guest Contact"));
        String roomNo = readRoomNo();
        LocalDate checkIn;
        LocalDate checkOut;
        while (true) {
            checkIn = readCheckInDate(fieldPrompt("Check-in date (yyyy-mm-dd)"));
            checkOut = readCheckOutDate(
                    fieldPrompt("Check-out date (yyyy-mm-dd)"), checkIn);
            String dateError = bookingController.validateStandardBookingDates(
                    roomNo, checkIn, checkOut);
            if (dateError == null) break;
            System.out.println("\n" + dateError);
            System.out.println("Please enter a different check-in and check-out date.\n");
        }
        printResult(bookingController.addStandardBookingForNewGuest(
                guestId, guestName, guestContact, roomNo, checkIn, checkOut));
        pressEnterToContinue();
    }

    private void modifyStandardBooking() {
        clearScreen();
        printBanner(BANNER_MODIFY_BOOKING);
        System.out.print("\n------------------------------------------------------------------------------\n"
                + "                           MODIFY STANDARD BOOKING                            \n"
                + "------------------------------------------------------------------------------\n");
        String confirmationNo = readStandardConfirmationNo();
        Booking existing = bookingController.findByKey(confirmationNo);
        if (existing == null || existing.getBookingType() != Entity.BookingType.STANDARD) {
            System.out.println("Booking not found or is not a standard booking.");
            pressEnterToContinue();
            return;
        }
        System.out.println(fieldPrompt("Current guest ID") + existing.getGuestId());
        System.out.println(fieldPrompt("Current room") + existing.getRoomNo());
        System.out.println(fieldPrompt("Current dates") + existing.getCheckInDate()
                + " to " + existing.getCheckOutDate());
        System.out.println("Press ENTER to keep each current value.");
        String guestId = readOptionalGuestId(existing.getGuestId());
        String roomNo = readOptionalRoomNo(existing.getRoomNo());
        LocalDate checkIn = readOptionalCheckInDate(existing.getCheckInDate());
        LocalDate checkOut = readOptionalCheckOutDate(existing.getCheckOutDate(), checkIn);
        printResult(bookingController.modifyStandardBooking(
                confirmationNo, guestId, roomNo, checkIn, checkOut));
        pressEnterToContinue();
    }

    private void cancelStandardBooking() {
        clearScreen();
        printBanner(BANNER_CANCEL_BOOKING);
        System.out.print("\n------------------------------------------------------------------------------\n"
                + "                           CANCEL STANDARD BOOKING                            \n"
                + "------------------------------------------------------------------------------\n");
        String confirmationNo = readStandardConfirmationNo();
        System.out.print(fieldPrompt("Confirm cancellation (Y/N)"));
        if (scanner.nextLine().trim().equalsIgnoreCase("Y"))
            printResult(bookingController.cancelStandardBooking(confirmationNo));
        else System.out.println("Cancellation aborted.");
        pressEnterToContinue();
    }

    private String readGuestId() {
        while (true) {
            System.out.print(fieldPrompt("Existing Guest ID"));
            String guestId = scanner.nextLine().trim();
            if (bookingController.guestExists(guestId)) return guestId;
            System.out.println("Guest not found.");
        }
    }

    private void handleViewAvailableRooms() {
        clearScreen();
        printBanner(BANNER_AVAILABLE_ROOM_OVERVIEW);
        System.out.print("\n------------------------------------------------------------------------------\n"
                + "                          AVAILABLE ROOM OVERVIEW                             \n"
                + "------------------------------------------------------------------------------\n");

        String[] rows = bookingController.getAvailableRoomRows();

        System.out.println();
        if (rows.length == 0) {
            System.out.println("No rooms currently have Available status.");
        } else {
            System.out.printf("%-10s %-12s %-18s%n",
                    "Room No.", "Room Type", "Current Status");
            System.out.println("------------------------------------------");
            for (String row : rows) System.out.println(row);
            System.out.println("\n" + fieldPrompt("Total available rooms") + rows.length);
        }
        pressEnterToContinue();
    }

    private String readOptionalGuestId(String currentGuestId) {
        while (true) {
            System.out.print(fieldPrompt("New Guest ID"));
            String guestId = scanner.nextLine().trim();
            if (guestId.isEmpty()) return currentGuestId;
            if (bookingController.guestExists(guestId)) return guestId;
            System.out.println("Guest not found.");
        }
    }

    private String readOptionalRoomNo(String currentRoomNo) {
        while (true) {
            System.out.print(fieldPrompt("New Room No"));
            String roomNo = scanner.nextLine().trim();
            if (roomNo.isEmpty()) return currentRoomNo;
            if (bookingController.roomExists(roomNo)) return roomNo;
            System.out.println("Room not found.");
        }
    }

    private LocalDate readOptionalCheckInDate(LocalDate currentDate) {
        while (true) {
            LocalDate date = readOptionalDateValue(
                    fieldPrompt("New check-in (yyyy-mm-dd)"), currentDate);
            if (!date.isBefore(LocalDate.now())) return date;
            System.out.println("Check-in date cannot be in the past.");
        }
    }

    private LocalDate readOptionalCheckOutDate(LocalDate currentDate, LocalDate checkIn) {
        while (true) {
            LocalDate date = readOptionalDateValue(
                    fieldPrompt("New check-out (yyyy-mm-dd)"), currentDate);
            if (date.isAfter(checkIn)) return date;
            System.out.println("Check-out date must be after check-in date.");
        }
    }

    private LocalDate readOptionalDateValue(String prompt, LocalDate currentDate) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) return currentDate;
            try { return LocalDate.parse(value); }
            catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use yyyy-mm-dd or press ENTER to keep it.");
            }
        }
    }

    private String readRequired(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println(fieldName + " cannot be empty.");
        }
    }

    private String readValidGuestName(String prompt) {
        while (true) {
            System.out.print(prompt);
            String name = scanner.nextLine().trim();
            if (name.matches("[A-Za-z ]{2,50}")) return name;
            System.out.println(
                    "Invalid guest name. Use letters and spaces only (2-50 characters).");
        }
    }

    private String readValidGuestContact(String prompt) {
        while (true) {
            System.out.print(prompt);
            String contact = scanner.nextLine().trim();
            if (contact.matches("[0-9\\-]{7,15}")) return contact;
            System.out.println(
                    "Invalid guest contact. Use digits and hyphens only (7-15 characters).");
        }
    }

    private String readRoomNo() {
        while (true) {
            System.out.print(fieldPrompt("Room No"));
            String roomNo = scanner.nextLine().trim();
            if (bookingController.roomExists(roomNo)) return roomNo;
            System.out.println("Room not found.");
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return LocalDate.parse(scanner.nextLine().trim()); }
            catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use yyyy-mm-dd.");
            }
        }
    }

    private LocalDate readCheckInDate(String prompt) {
        while (true) {
            LocalDate checkIn = readDate(prompt);
            if (!checkIn.isBefore(LocalDate.now())) return checkIn;
            System.out.println("Check-in date cannot be in the past.");
        }
    }

    private LocalDate readCheckOutDate(String prompt, LocalDate checkIn) {
        while (true) {
            LocalDate checkOut = readDate(prompt);
            if (checkOut.isAfter(checkIn)) return checkOut;
            System.out.println("Check-out date must be after check-in date.");
        }
    }

    private LocalDate readOptionalDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) return null;
            try { return LocalDate.parse(value); }
            catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use yyyy-mm-dd or leave blank.");
            }
        }
    }

    private String readStandardConfirmationNo() {
        while (true) {
            System.out.print(fieldPrompt("Confirmation No (8 digits)"));
            String value = scanner.nextLine().trim();
            if (bookingController.isValidStandardConfirmationNo(value)) return value;
            System.out.println("Invalid format. Example: 00000001.");
        }
    }

    private String readReportStatus() {
        while (true) {
            System.out.print(fieldPrompt("Booking status"));
            String status = scanner.nextLine().trim();
            if (status.isEmpty() || bookingController.validateReportFilters(status, null, null) == null)
                return status;
            System.out.println("Use Confirmed, CheckedIn, CheckedOut, Cancelled, or leave blank.");
        }
    }

    private LocalDate[] readReportDateRange() {
        while (true) {
            LocalDate from = readOptionalDate(fieldPrompt("Check-in from (yyyy-mm-dd)"));
            LocalDate to = readOptionalDate(fieldPrompt("Check-in to (yyyy-mm-dd)"));
            String error = bookingController.validateReportFilters("", from, to);
            if (error == null) return new LocalDate[]{from, to};
            System.out.println(error);
        }
    }

    private void printResult(ControllerResult result) {
        System.out.println(result.isOk() ? "\nOperation successful." : "\nOperation failed.");
        if (result.getMessage() != null) System.out.println(result.getMessage());
    }

    private void handleViewReport() {
        boolean running = true;
        while (running) {
            clearScreen();
            printBanner(BANNER_VIEW_REPORT);
            System.out.print("\n------------------------------------------------------------------------------\n"
                    + "                                VIEW REPORT                                   \n"
                    + "------------------------------------------------------------------------------\n"
                    + "                   1. Waiting List Report                                     \n"
                    + "                   2. Registration Report                                     \n"
                    + "                   3. Standard Booking Report                                 \n"
                    + "                   0. Back                                                     \n"
                    + "------------------------------------------------------------------------------\n"
                    + "Enter 1 - 4 to select an option: ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    handleWaitingListReport();
                    break;
                case "2":
                    handleRegistrationReport();
                    break;
                case "3":
                    handleStandardBookingReport();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter 1 to 4.");
                    pressEnterToContinue();
            }
        }
    }

    /**
     * Displays guests who are still waiting.
     */
    private void handleWaitingListReport() {

        clearScreen();
        printBanner(BANNER_WAITING_LIST);

        WalkInRegistrationController walkInController =
                new WalkInRegistrationController(
                        guestController,
                        roomController,
                        bookingController
                );

        System.out.println("Leave filters blank to include all waiting guests.");
        System.out.print(fieldPrompt("Guest name or ID"));
        String guestKeyword = scanner.nextLine().trim();
        System.out.print(fieldPrompt("Contact number contains"));
        String contactKeyword = scanner.nextLine().trim();

        String[] results = walkInController.getWaitingListReportRows(
                guestKeyword, contactKeyword);

        System.out.println(
                "\n=========== WAITING LIST REPORT ==========="
        );

        System.out.println(fieldPrompt("Total waiting")
                + results.length + " guest(s)");

        System.out.println(
                "(sorted alphabetically by name)"
        );

        System.out.println(
                "---------------------------------------------"
        );

        if (results.length == 0) {

            System.out.println(
                    "No guests currently waiting."
            );

        } else {

            for (String row : results) System.out.println(row);
        }

        System.out.println(
                "=============================================\n"
        );

        pressEnterToContinue();
    }

    /**
     * Displays processed walk-in bookings.
     */
    private void handleRegistrationReport() {

        clearScreen();
        printBanner(BANNER_REG_REPORT);

        System.out.println("Leave filters blank to include all records.");
        String status = readReportStatus();
        LocalDate[] dateRange = readReportDateRange();
        LocalDate fromDate = dateRange[0];
        LocalDate toDate = dateRange[1];
        System.out.print(fieldPrompt("Guest name or ID"));
        String guestKeyword = scanner.nextLine().trim();

        String[] results = bookingController.getWalkInRegistrationReportRows(
                status, fromDate, toDate, guestKeyword);

        System.out.println(
                "\n=========== REGISTRATION REPORT ==========="
        );

        System.out.println(fieldPrompt("Total processed walk-ins")
                + results.length + " booking(s)");

        System.out.println(
                "(sorted by confirmation number)"
        );

        System.out.println(
                "---------------------------------------------"
        );

        if (results.length == 0) {

            System.out.println(
                    "No walk-in registrations "
                    + "have been processed yet."
            );

        } else {

            for (String row : results) System.out.println(row);
        }

        System.out.println(
                "=============================================\n"
        );

        pressEnterToContinue();
    }

    private void handleStandardBookingReport() {
        clearScreen();
        printBanner(BANNER_BOOKING_REPORT);

        System.out.println("Leave filters blank to include all standard bookings.");
        String status = readReportStatus();
        LocalDate[] dateRange = readReportDateRange();
        LocalDate fromDate = dateRange[0];
        LocalDate toDate = dateRange[1];
        System.out.print(fieldPrompt("Guest name or ID"));
        String guestKeyword = scanner.nextLine().trim();

        String[] results = bookingController.getStandardBookingReportRows(
                status, fromDate, toDate, guestKeyword);

        System.out.println("\n================ STANDARD BOOKING REPORT ================");
        System.out.println(fieldPrompt("Total standard bookings")
                + results.length + " booking(s)");
        System.out.println("(filtered by status/date/guest; sorted by confirmation number)");
        System.out.println("---------------------------------------------------------");

        if (results.length == 0) {
            System.out.println("No standard bookings match the selected filters.");
        } else {
            for (String row : results) System.out.println(row);
        }

        System.out.println("=========================================================\n");
        pressEnterToContinue();
    }
}
