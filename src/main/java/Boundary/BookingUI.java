/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt
 * to change this license
 */
package Boundary;

import Control.GuestController;
import Control.RoomController;
import Control.BookingController;
import Control.WalkInRegistrationController;
import Entity.Guest;
import Entity.Booking;

import java.util.Scanner;

// Author: [Your Name]
public class BookingUI {

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
                    handleWaitingListReport();
                    break;

                case "3":
                    handleRegistrationReport();
                    break;

                case "4":
                    running = false;

                    System.out.println(
                            "\nReturning to Main Menu...\n"
                    );
                    break;

                default:
                    System.out.println(
                            "\nInvalid option. "
                            + "Please enter 1 to 4.\n"
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
                + "                   1. Walk-In Registration                                     \n"
                + "                   2. Waiting List Report                                      \n"
                + "                   3. Registration Report                                      \n"
                + "                   4. Back to Main Menu                                        \n"
                + "------------------------------------------------------------------------------\n"
                + "Enter 1 - 4 to select an option: "
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
                        bookingController
                );

        walkInRegistrationUI.run();
    }

    /**
     * Displays guests who are still waiting.
     */
    private void handleWaitingListReport() {

        WalkInRegistrationController walkInController =
                new WalkInRegistrationController(
                        guestController,
                        roomController,
                        bookingController
                );

        Guest[] results =
                walkInController.getWaitingListReport();

        System.out.println(
                "\n=========== WAITING LIST REPORT ==========="
        );

        System.out.println(
                "Total waiting : "
                + results.length
                + " guest(s)"
        );

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

            int position = 1;

            for (Guest guest : results) {

                System.out.println(
                        position
                        + ". "
                        + guest.getName()
                        + " (ID: "
                        + guest.getGuestId()
                        + ", Contact: "
                        + guest.getContact()
                        + ")"
                );

                position++;
            }
        }

        System.out.println(
                "=============================================\n"
        );
    }

    /**
     * Displays processed walk-in bookings.
     */
    private void handleRegistrationReport() {

        Booking[] results =
                bookingController
                        .getWalkInRegistrationReport();

        System.out.println(
                "\n=========== REGISTRATION REPORT ==========="
        );

        System.out.println(
                "Total processed walk-ins : "
                + results.length
                + " booking(s)"
        );

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

            for (Booking booking : results) {

                System.out.println(
                        booking.getConfirmationNo()
                        + " | "
                        + booking.getGuest().getName()
                        + " | Room "
                        + booking.getRoom().getRoomNo()
                        + " | "
                        + booking.getCheckInDate()
                        + " to "
                        + booking.getCheckOutDate()
                        + " | "
                        + booking.getBookingStatus()
                );
            }
        }

        System.out.println(
                "=============================================\n"
        );
    }
}