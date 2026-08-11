/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt
 * to change this license
 */
package Boundery;

import Control.WalkInRegistrationController;
import Control.GuestController;
import Control.RoomController;
import Control.BookingController;
import Entity.Guest;
import ADT.ListInterface;
import Utility.ControllerResult;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

// Author: [Your Name]
public class WalkInRegistrationUI {

    private final WalkInRegistrationController controller;
    private final Scanner scanner;

    /**
     * Receives the existing controllers from BookingUI so all screens
     * share the same in-memory data during a run.
     */
    public WalkInRegistrationUI(
            GuestController guestController,
            RoomController roomController,
            BookingController bookingController) {

        this.controller =
                new WalkInRegistrationController(
                        guestController,
                        roomController,
                        bookingController
                );

        this.scanner = new Scanner(System.in);
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

                case "5":
                    running = false;
                    System.out.println("\nReturning to Main Menu...\n");
                    break;

                default:
                    System.out.println("\nInvalid option. Please enter 1 to 5.\n");
            }
        }
    }

    private void printMenu() {

        System.out.print(
                "\n"
                + "------------------------------------------------------------------------------\n"
                + "                     WALK-IN REGISTRATION & STANDARD BOOKING                  \n"
                + "------------------------------------------------------------------------------\n"
                + "                   1. Register Walk-In Guest                                  \n"
                + "                   2. Process Next Guest in Queue                              \n"
                + "                   3. View Waiting Queue                                       \n"
                + "                   4. Cancel Walk-In Registration                              \n"
                + "                   5. Back to Main Menu                                        \n"
                + "------------------------------------------------------------------------------\n"
                + "Enter 1 - 5 to select an option: "
        );
    }

    // ───────────────────── 1. Register Walk-In Guest ─────────────────────

    private void handleRegisterWalkIn() {

        System.out.print(
                "\n"
                + "------------------------------------------------------------------------------\n"
                + "                         REGISTER WALK-IN GUEST                               \n"
                + "------------------------------------------------------------------------------\n"
        );

        String guestId = readValidId("Guest ID                 : ");
        String name = readValidName("Guest Name                : ");
        String contact = readValidContact("Guest Contact             : ");

        Guest guest = new Guest(guestId, name, contact);

        ControllerResult result = controller.registerWalkIn(guest);

        printResult(result);
        pressEnterToContinue();
    }

    // ───────────────────── 2. Process Next Guest in Queue ─────────────────────

    private void handleProcessNextGuest() {

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

        Guest nextGuest = controller.peekNextGuest();
        if (nextGuest == null) {
            System.out.println("\nUnable to retrieve the next guest.");
            pressEnterToContinue();
            return;
        }

        System.out.println("\nNext guest in queue: " + nextGuest.getName());

        LocalDate checkInDate = readValidDate("Enter Check-In Date (yyyy-mm-dd)  : ");
        LocalDate checkOutDate = readValidDate("Enter Check-Out Date (yyyy-mm-dd) : ");

        ControllerResult result =
                controller.processNextGuest(checkInDate, checkOutDate);

        printResult(result);
        pressEnterToContinue();
    }

    // ───────────────────── 3. View Waiting Queue ─────────────────────

    private void handleViewQueue() {

        System.out.print(
                "\n"
                + "------------------------------------------------------------------------------\n"
                + "                       CURRENT WALK-IN QUEUE                                  \n"
                + "------------------------------------------------------------------------------\n"
        );

        ListInterface<Guest> queueSnapshot = controller.viewQueue();

        if (queueSnapshot.size() == 0) {
            System.out.println("\nWalk-in queue is currently empty.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.printf("%-5s %-25s %-10s %-15s%n", "No.", "Name", "ID", "Contact");
        System.out.println("--------------------------------------------------------------------------");

        for (int i = 1; i <= queueSnapshot.size(); i++) {
            Guest guest = queueSnapshot.getEntry(i);
            System.out.printf("%-5d %-25s %-10s %-15s%n",
                    i, guest.getName(), guest.getGuestId(), guest.getContact());
        }

        pressEnterToContinue();
    }

    // ───────────────────── 4. Cancel Walk-In Registration ─────────────────────

    private void handleCancelWalkIn() {

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

        String guestId = readValidId("Guest ID to cancel        : ");

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