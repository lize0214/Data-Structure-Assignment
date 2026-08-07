/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Boundery;

import java.util.Scanner;

public class MainMenuUI {

    private final Scanner scanner;

    public MainMenuUI() {
        scanner = new Scanner(System.in);
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
                    openFrontDeskMenu();
                    break;

                case "3":
                    openVIPAllocationMenu();
                    break;

                case "4":
                    openLoyaltyMenu();
                    break;

                case "5":
                    openHousekeepingMenu();
                    break;

                case "6":
                    openReportMenu();
                    break;

                case "7":
                    running = false;
                    System.out.println(
                            "\nThank you for using the Hotel Management System."
                    );
                    break;

                default:
                    System.out.println(
                            "\nInvalid option. Please enter a number from 1 to 7.\n"
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
                "                         1. Booking & Registration                            \n" +
                "                         2. Front Desk                                        \n" +
                "                         3. VIP Room Allocation                               \n" +
                "                         4. Loyalty Management                                \n" +
                "                         5. Housekeeping                                      \n" +
                "                         6. Reports                                           \n" +
                "                         7. Exit System                                       \n" +
                "------------------------------------------------------------------------------\n" +
                "Enter 1 - 7 to select an option: "
        );
    }

    private void openBookingMenu() {
        BookingUI bookingUI = new BookingUI();
        bookingUI.run();
    }

    private void openFrontDeskMenu() {
        FrontDeskUI frontDeskUI = new FrontDeskUI(scanner);
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