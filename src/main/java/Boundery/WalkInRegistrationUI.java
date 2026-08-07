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
     * Receives the existing controllers from BookingUI.
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

    public void run() {

        boolean running = true;

        while (running) {

            printMenu();

            String choice =
                    scanner.nextLine().trim();

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

                    System.out.println(
                            "\nReturning to Main Menu...\n"
                    );
                    break;

                default:
                    System.out.println(
                            "\nInvalid option. "
                            + "Please enter 1 to 5.\n"
                    );
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

    private void handleRegisterWalkIn() {

        System.out.print("Enter Guest ID: ");
        String guestId =
                scanner.nextLine().trim();

        System.out.print("Enter Guest Name: ");
        String name =
                scanner.nextLine().trim();

        System.out.print("Enter Guest Contact: ");
        String contact =
                scanner.nextLine().trim();

        Guest guest =
                new Guest(
                        guestId,
                        name,
                        contact
                );

        ControllerResult result =
                controller.registerWalkIn(guest);

        printResult(result);
    }

    private void handleProcessNextGuest() {

        if (controller.isQueueEmpty()) {

            System.out.println(
                    "\nWalk-in queue is empty. "
                    + "No guest to process.\n"
            );

            return;
        }

        Guest nextGuest =
                controller.peekNextGuest();

        if (nextGuest == null) {

            System.out.println(
                    "\nUnable to retrieve the next guest.\n"
            );

            return;
        }

        System.out.println(
                "\nNext guest in queue: "
                + nextGuest.getName()
        );

        LocalDate checkInDate =
                readDate(
                        "Enter Check-In Date "
                        + "(yyyy-mm-dd): "
                );

        if (checkInDate == null) {
            return;
        }

        LocalDate checkOutDate =
                readDate(
                        "Enter Check-Out Date "
                        + "(yyyy-mm-dd): "
                );

        if (checkOutDate == null) {
            return;
        }

        ControllerResult result =
                controller.processNextGuest(
                        checkInDate,
                        checkOutDate
                );

        printResult(result);
    }

    private void handleViewQueue() {

        ListInterface<Guest> queueSnapshot =
                controller.viewQueue();

        if (queueSnapshot.size() == 0) {

            System.out.println(
                    "\nWalk-in queue is currently empty.\n"
            );

            return;
        }

        System.out.println(
                "\n--- Current Walk-In Queue "
                + "(front to back) ---"
        );

        for (int i = 1;
                i <= queueSnapshot.size();
                i++) {

            Guest guest =
                    queueSnapshot.getEntry(i);

            System.out.println(
                    i + ". "
                    + guest.getName()
                    + " (ID: "
                    + guest.getGuestId()
                    + ", Contact: "
                    + guest.getContact()
                    + ")"
            );
        }

        System.out.println();
    }

    private void handleCancelWalkIn() {

        System.out.print(
                "Enter Guest ID to cancel: "
        );

        String guestId =
                scanner.nextLine().trim();

        ControllerResult result =
                controller.cancelWalkIn(
                        guestId
                );

        printResult(result);
    }

    private LocalDate readDate(String prompt) {

        System.out.print(prompt);

        String input =
                scanner.nextLine().trim();

        try {

            return LocalDate.parse(input);

        } catch (DateTimeParseException e) {

            System.out.println(
                    "Invalid date format. "
                    + "Please use yyyy-mm-dd.\n"
            );

            return null;
        }
    }

    private void printResult(
            ControllerResult result) {

        if (result.isOk()) {

            System.out.println(
                    "\n"
                    + result.getMessage()
                    + "\n"
            );

        } else {

            System.out.println(
                    "\nError: "
                    + result.getMessage()
                    + "\n"
            );
        }
    }
}