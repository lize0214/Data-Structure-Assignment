/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt
 * to change this license
 */
package Boundery;

import Control.WalkInRegistrationController;
import Control.GuestController;
import Control.RoomController;
import Control.BookingController;
import Utility.ControllerResult;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * @author Chin Yik Heng
 */
public class WalkInRegistrationUI {

    private static final int FIELD_LABEL_WIDTH = 36;

    private final WalkInRegistrationController controller;
    private final GuestController guestController;
    private final Scanner scanner;

    /**
     * Receives the existing controllers from BookingUI so all screens
     * share the same in-memory data during a run.
     */
    public WalkInRegistrationUI(
            GuestController guestController,
            RoomController roomController,
            BookingController bookingController,
            Scanner scanner) {

        this.guestController = guestController;
        this.controller =
                new WalkInRegistrationController(
                        guestController,
                        roomController,
                        bookingController
                );

        this.scanner = scanner;
    }

    // ───────────────────── Banner (Diet Cola font, patorjk.com/software/taag) ─────────────────────
    static final String[] BANNER_MAIN = {
        "                  /\\         .-.     .-.           .----.     .-.     ",
        "..-.     .-.  _  / |        / (_)   (_) )  .'-       /   `      /  |  ",
        "   )   (     (  /  |  .    /           /  /         /          /\\  |  ",
        "  /     \\     `/.__|_.'   /          _/_.'`-=-.    /          /  \\ |  ",
        " (   .   ).:' /    |   .-/.    .-..  /   \\        /      .-' /    \\|  ",
        "  `-' `-'(__.'     `-'(_/ `-._.  (_.'     `-'.---------'(__.'      `. ",
        "   .-.                .-       .-. .----.        .-..--------'.-.        ",
        "  (_) )-.     .---;`-'  .--.`-'      /   ` .--.-'  (_)   /   (_) )-.     ",
        "     /   \\   (   (_)   /  (_;       /     (  (_)        /       /   \\    ",
        "    /     )   )--     /            /       `-.         /       /     )   ",
        " .-/  `--'   (      /(     --;-   /      _    )     .-/._   .-/  `--'    ",
        "(_/     `-._)`\\___.'  `.___.'.---------'(_.--'     (_/  `- (_/     `-._) ",
        "         /\\   .--------'  .----.         .-.     ",
        "     _  / |  (_)   /        /   `.--.    .-/  |  ",
        "    (  /  |  .    /        /    /    )`-' /\\  |  ",
        "     `/.__|_.'   /        /    /    /    /  \\ |  ",
        " .:' /    |   .-/._      /    (    /.-' /    \\|  ",
        "(__.'     `-'(_/  `-.---------'`-.'(__.'      `. "
    };

    private static final String[] BANNER_REGISTER = {
        " .-.                .-       .-. .----.        .-..--------'     .- .-.       ",
        "(_) )-.     .---;`-'  .--.`-'      /   ` .--.-'  (_)   / .---;`-' (_) )-.    ",
        "   /   \\   (   (_)   /  (_;       /     (  (_)        / (   (_)      /   \\   ",
        "  /     )   )--     /            /       `-.         /   )--         /     )  ",
        "-/  `--'   (      /(     --;-   /      _    )     .-/._ (      / .-/  `--'   ",
        "/     `-._)`\\___.'  `.___.'.---------'(_.--'     (_/  `-`\\___.' (_/     `-._)"
    };

    private static final String[] BANNER_PROCESS = {
        "   .-.      .-.                     .-._   .-._.    .-     .-.   .-. ",
        "  (_) )-.  (_) )-.      .--.    .-..' (_)`-'.---;`-' .--.-'.--.-'    ",
        "     /   \\    /   \\    /    )`-'  |        (   (_)  (  (_)(  (_)     ",
        "    /     )  /     )  /    /      |    _    )--      `-.   `-.       ",
        " .-/  `--'.-/  `--'  (    /       `.    )  (      /_    )_    )      ",
        "(_/      (_/     `-._)`-.'          `--'   `\\___.'(_.--'(_.--'       "
    };

    private static final String[] BANNER_VIEW_QUEUE = {
        "          .----.         .-          ",
        "..-.     .-./   `.---;`-'..-.     .-.",
        "   )   /   /    (   (_)     )   (    ",
        "  /   /   /      )--       /     \\   ",
        " (  .'   /      (      /  (   .   )  ",
        "  \\/.---------' `\\___.'    `-' `-'   ",
        "   .`-,                   .-               .- ",
        "  /    ) _     .-..---;`-'_     .-..---;`-'   ",
        " /    / '     (  (   (_) '     (  (   (_)     ",
        "(    /   /     )  )--     /     )  )--        ",
        " `--`-. (     /  (      /(     /  (      /    ",
        "       '-`._.'   `\\___.'  `._.'   `\\___.'     "
    };

    private static final String[] BANNER_CANCEL = {
        "  .-._   .-._. /\\        .-.     .-._   .-._.    .-   .-.   ",
        "..' (_)`-' _  / |          /  |..' (_)`-'.---;`-'    / (_)  ",
        "|         (  /  |  .      /\\  ||        (   (_)     /       ",
        "|    _     `/.__|_.'     /  \\ ||    _    )--       /        ",
        "`.    ).:' /    |   .-' /    \\|`.    )  (      /.-/.    .-. ",
        "  `--'(__.'     `-'(__.'      `. `--'   `\\___.'(_/ `-._.    "
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

    /**
     * "Clears" the console. NetBeans' Output panel has no real clear API,
     * so this pushes enough blank lines through that old content scrolls
     * out of view - same approach used across the rest of the system for
     * a consistent look.
     */
    private void clearScreen() {
        for (int i = 0; i < 60; i++) {
            System.out.println();
        }
    }

    private String fieldPrompt(String label) {
        return String.format("%-" + FIELD_LABEL_WIDTH + "s: ", label);
    }

    /**
     * "Loading" animation that adapts to where it's running - a real
     * terminal gets a spinner, NetBeans' Output panel (which can't
     * overwrite lines) gets appended dots instead. Runs for ~3 seconds.
     */
    private void showLoading(String message) {
        if (System.console() != null) {
            String[] frames = {"|", "/", "-", "\\"};
            try {
                for (int cycle = 0; cycle < 6; cycle++) {
                    for (String frame : frames) {
                        System.out.print("\r" + message + " " + frame);
                        Thread.sleep(125);
                    }
                }
                System.out.print("\r" + " ".repeat(message.length() + 2) + "\r");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            System.out.print(message);
            try {
                for (int i = 0; i < 6; i++) {
                    Thread.sleep(500);
                    System.out.print(".");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println();
        }
    }

    // ───────────────────── Main Menu ─────────────────────

    public void run() {

        boolean running = true;

        while (running) {

            printMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    handleRegisterWalkIn();
                    break;

                case "2":
                    handleProcessNextGuest();
                    break;

                case "3":
                    handleViewQueue();
                    break;

                case "4":
                    handleCancelWalkIn();
                    break;

                case "0":
                    running = false;
                    System.out.println("\nReturning to Booking Menu...\n");
                    break;

                default:
                    System.out.println("\nInvalid option. Please enter 0 to 4.\n");
            }
        }
    }

    private void printMenu() {

        clearScreen();
        printBanner(BANNER_MAIN);

        System.out.print(
                "\n"
                + "------------------------------------------------------------------------------\n"
                + "                              WALK-IN REGISTRATION                            \n"
                + "------------------------------------------------------------------------------\n"
                + "                   1. Register Walk-In Guest                                  \n"
                + "                   2. Process Next Guest in Queue                              \n"
                + "                   3. View Waiting Queue                                       \n"
                + "                   4. Cancel Walk-In Registration                              \n"
                + "                   0. Back                                                     \n"
                + "------------------------------------------------------------------------------\n"
                + "Enter your choice: "
        );
    }

    // ───────────────────── 1. Register Walk-In Guest ─────────────────────

    private void handleRegisterWalkIn() {

        clearScreen();
        printBanner(BANNER_REGISTER);

        System.out.print(
                "\n"
                + "------------------------------------------------------------------------------\n"
                + "                         REGISTER WALK-IN GUEST                               \n"
                + "------------------------------------------------------------------------------\n"
        );

        String guestId = guestController.generateNextGuestId();
        System.out.println(fieldPrompt("Generated Guest ID") + guestId);
        String name = readValidName(fieldPrompt("Guest Name"));
        String contact = readValidContact(fieldPrompt("Guest Contact"));

        showLoading("Registering guest...");
        ControllerResult result = controller.registerWalkIn(guestId, name, contact);

        printResult(result);
        pressEnterToContinue();
    }

    // ───────────────────── 2. Process Next Guest in Queue ─────────────────────

    private void handleProcessNextGuest() {

        clearScreen();
        printBanner(BANNER_PROCESS);

        System.out.print(
                "\n"
                + "------------------------------------------------------------------------------\n"
                + "                       PROCESS NEXT GUEST IN QUEUE                            \n"
                + "------------------------------------------------------------------------------\n"
        );

        if (controller.isQueueEmpty()) {
            System.out.println("\nWalk-in queue is empty. No guest to process.");
            pressEnterToContinue();
            return;
        }

        String nextGuestName = controller.getNextGuestName();
        if (nextGuestName == null) {
            System.out.println("\nUnable to retrieve the next guest.");
            pressEnterToContinue();
            return;
        }

        System.out.println("\n" + fieldPrompt("Next guest in queue") + nextGuestName);

        LocalDate checkInDate = readValidCheckInDate(
                fieldPrompt("Enter Check-In Date (yyyy-mm-dd)"));
        LocalDate checkOutDate = readValidCheckOutDate(
                fieldPrompt("Enter Check-Out Date (yyyy-mm-dd)"), checkInDate);

        String preferredRoomType;
        String[] availableRooms;
        while (true) {
            preferredRoomType = readPreferredRoomType();
            if (preferredRoomType == null) {
                System.out.println("\nRoom selection cancelled. " + nextGuestName
                        + " remains at the front of the queue.");
                pressEnterToContinue();
                return;
            }
            availableRooms = controller.getAvailableRoomRows(
                    preferredRoomType, checkInDate, checkOutDate);
            if (availableRooms.length > 0) break;
            System.out.println("\nNo " + preferredRoomType
                    + " rooms are available for those dates.");
            System.out.println("Please choose another room type.");
        }

        System.out.println("\nAvailable " + preferredRoomType + " rooms:");
        System.out.printf("%-10s %-12s %-11s %-18s%n",
                "Room No.", "Room Type", "Price", "Current Status");
        System.out.println("-------------------------------------------------------");
        for (String roomRow : availableRooms) System.out.println(roomRow);

        String selectedRoomNo;
        while (true) {
            System.out.print("\n" + fieldPrompt("Select Room No"));
            selectedRoomNo = scanner.nextLine().trim();
            if (controller.isRoomAvailable(selectedRoomNo, preferredRoomType,
                    checkInDate, checkOutDate)) break;
            System.out.println("Please select an available room from the list.");
        }

        showLoading("Processing guest...");
        ControllerResult result =
                controller.processNextGuest(checkInDate, checkOutDate,
                        preferredRoomType, selectedRoomNo);

        printResult(result);
        pressEnterToContinue();
    }

    // ───────────────────── 3. View Waiting Queue ─────────────────────

    private void handleViewQueue() {

        clearScreen();
        printBanner(BANNER_VIEW_QUEUE);

        System.out.print(
                "\n"
                + "------------------------------------------------------------------------------\n"
                + "                       CURRENT WALK-IN QUEUE                                  \n"
                + "------------------------------------------------------------------------------\n"
        );

        String[] queueRows = controller.getQueueDisplayRows();

        if (queueRows.length == 0) {
            System.out.println("\nWalk-in queue is currently empty.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.printf("%-5s %-25s %-10s %-15s%n", "No.", "Name", "ID", "Contact");
        System.out.println("--------------------------------------------------------------------------");

        for (String row : queueRows) System.out.println(row);

        pressEnterToContinue();
    }

    // ───────────────────── 4. Cancel Walk-In Registration ─────────────────────

    private void handleCancelWalkIn() {

        clearScreen();
        printBanner(BANNER_CANCEL);

        System.out.print(
                "\n"
                + "------------------------------------------------------------------------------\n"
                + "                      CANCEL WALK-IN REGISTRATION                             \n"
                + "------------------------------------------------------------------------------\n"
        );

        if (controller.isQueueEmpty()) {
            System.out.println("\nWalk-in queue is empty. Nothing to cancel.");
            pressEnterToContinue();
            return;
        }

        String guestId = readValidId(fieldPrompt("Guest ID to cancel"));

        if (!confirmAction("\nConfirm cancelling this walk-in registration?")) {
            System.out.println("Cancellation aborted.");
            pressEnterToContinue();
            return;
        }

        ControllerResult result = controller.cancelWalkIn(guestId);

        printResult(result);
        pressEnterToContinue();
    }

    // ───────────────────── Input Helpers ─────────────────────

    /**
     * Reads an ID-style field, re-prompting until it's letters/numbers only
     * (no spaces or symbols, max 10 characters) - keeps IDs consistent
     * with the CSV storage format used across the system.
     */
    private String readValidId(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("[A-Za-z0-9]{1,10}")) {
                return input;
            }
            System.out.println("Invalid format - use letters and numbers only, no spaces or symbols (max 10 characters).");
        }
    }

    /**
     * Reads a name-style field, re-prompting until it contains only
     * letters and spaces (2-50 characters).
     */
    private String readValidName(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("[A-Za-z ]{2,50}")) {
                return input;
            }
            System.out.println("Invalid format - letters and spaces only (2-50 characters).");
        }
    }

    /**
     * Reads a contact/phone-style field, re-prompting until it contains
     * only digits and hyphens (7-15 characters).
     */
    private String readValidContact(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("[0-9\\-]{7,15}")) {
                return input;
            }
            System.out.println("Invalid format - digits and hyphens only (7-15 characters).");
        }
    }

    /**
     * Reads a date field, re-prompting until it parses as a valid
     * yyyy-mm-dd date.
     */
    private LocalDate readValidDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format - please use yyyy-mm-dd.");
            }
        }
    }

    private LocalDate readValidCheckInDate(String prompt) {
        while (true) {
            LocalDate checkInDate = readValidDate(prompt);
            if (checkInDate.equals(LocalDate.now())) return checkInDate;
            System.out.println("A walk-in check-in date must be today.");
        }
    }

    private LocalDate readValidCheckOutDate(String prompt, LocalDate checkInDate) {
        while (true) {
            LocalDate checkOutDate = readValidDate(prompt);
            if (checkOutDate.isAfter(checkInDate)) return checkOutDate;
            System.out.println("Check-out date must be after check-in date.");
        }
    }

    private String readPreferredRoomType() {
        while (true) {
            System.out.println("\nPreferred Room Type:");
            System.out.println("1. Single");
            System.out.println("2. Deluxe");
            System.out.println("3. Suite");
            System.out.println("0. Back");
            System.out.print("Enter your choice: ");
            switch (scanner.nextLine().trim()) {
                case "1": return "Single";
                case "2": return "Deluxe";
                case "3": return "Suite";
                case "0": return null;
                default: System.out.println("Invalid option. Please enter 0 to 3.");
            }
        }
    }

    /**
     * Asks a yes/no question before a destructive action (cancelling a
     * registration). Anything other than an explicit "Y" is treated as
     * "no", so accidental Enter-presses don't confirm by default.
     */
    private boolean confirmAction(String message) {
        System.out.print(message + " (Y/N): ");
        String input = scanner.nextLine().trim();
        return input.equalsIgnoreCase("Y");
    }

    private void printResult(ControllerResult result) {

        System.out.println();

        if (result.isOk()) {
            System.out.println("----------------------------------------");
            System.out.println("Operation completed successfully.");
            if (result.getMessage() != null) {
                System.out.println(result.getMessage());
            }
            System.out.println("----------------------------------------");
        } else {
            System.out.println("----------------------------------------");
            System.out.println("Operation failed.");
            System.out.println(result.getMessage());
            System.out.println("----------------------------------------");
        }
    }

    private void pressEnterToContinue() {
        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }
}
