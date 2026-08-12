/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Boundery;

import java.util.Scanner;
import Control.BookingController;
import Control.GuestController;
import Control.RoomController;

public class MainMenuUI {

    private final Scanner scanner;
    private final BookingController bookingController;

    public MainMenuUI() {
        scanner = new Scanner(System.in);
        GuestController guestController = new GuestController();
        RoomController roomController = new RoomController();
        bookingController = new BookingController(guestController, roomController);
    }

    public void run() {
        boolean running = true;

        while (running) {
            displayMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    openBookingMenu();
                    break;

                case "2":
                    openWalkInRegistrationMenu();
                    break;

                case "3":
                    openFrontDeskMenu();
                    break;

                case "4":
                    openVIPAllocationMenu();
                    break;

                case "5":
                    openLoyaltyMenu();
                    break;

                case "6":
                    openHousekeepingMenu();
                    break;

                case "7":
                    openReportMenu();
                    break;

                case "8":
                    running = false;
                    System.out.println(
                            "\nThank you for using the Hotel Management System."
                    );
                    break;

                default:
                    System.out.println(
                            "\nInvalid option. Please enter a number from 1 to 8.\n"
                    );
            }
        }

        scanner.close();
    }

    private void displayMainMenu() {
        System.out.print(
                "\n" +
                "                    /\\                                                        \n" +
                "                   / |                                                         \n" +
                "               (  /  |  . ).--..-.  )  ( .  .-. .-.                          \n" +
                "                `/.__|_.'/   ./.-'_(    ) )/   )   )                         \n" +
                "            .:' /    |  /    (__.'  `--':'/   /   (                          \n" +
                "            (__.'     `-'   .-.                     `-'                      \n" +
                "                          (_) )-.                          /                 \n" +
                "                             /   \\   .-.  .  .-._.).--.---/---              \n" +
                "                            /     )./.-'_/ \\(   )/       /                  \n" +
                "                         .-/  `--' (__.'/ ._)`-'/       /                    \n" +
                "                        (_/     `-._)  /                                     \n" +
                "------------------------------------------------------------------------------\n" +
                "                         1. Booking                                           \n" +
                "                         2. Walk-In Registration                              \n" +
                "                         3. Front Desk Service                                \n" +
                "                         4. VIP Room Allocation                               \n" +
                "                         5. Loyalty Management                                \n" +
                "                         6. Housekeeping                                      \n" +
                "                         7. Reports                                           \n" +
                "                         8. Exit System                                       \n" +
                "------------------------------------------------------------------------------\n" +
                "Enter 1 - 8 to select an option: "
        );
    }

    private void openBookingMenu() {
        BookingUI bookingUI = new BookingUI();
        bookingUI.run();
    }

    private void openWalkInRegistrationMenu() {
        WalkInRegistrationUI walkInRegistrationUI =
                new WalkInRegistrationUI();

        walkInRegistrationUI.run();
    }

    private void openFrontDeskMenu() {
        FrontDeskUI frontDeskUI = new FrontDeskUI(bookingController, scanner);
        frontDeskUI.run();
    }

    private void openVIPAllocationMenu() {
        VIPAllocationUI vipAllocationUI = new VIPAllocationUI();
        vipAllocationUI.run();
    }

    private void openLoyaltyMenu() {
        LoyaltyUI loyaltyUI = new LoyaltyUI();
        loyaltyUI.run();
    }

    private void openHousekeepingMenu() {
        HousekeepingUI housekeepingUI = new HousekeepingUI();
        housekeepingUI.run();
    }

    private void openReportMenu() {
        ReportUI reportUI = new ReportUI();
        reportUI.run();
    }
}