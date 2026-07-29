/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Boundery;

import Control.FrontDeskController;
import Utility.ControllerResult;

import java.util.Scanner;

public class FrontDeskUI {

    private final FrontDeskController controller;
    private final Scanner scanner;

    public FrontDeskUI(Scanner scanner) {
        this.controller = new FrontDeskController();
        this.scanner = scanner;
    }

    public void run() {
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleSearch();
                    break;

                case "2":
                    handleAvailability();
                    break;

                case "3":
                    handleBilling();
                    break;

                case "4":
                    running = false;
                    System.out.println("\nReturning to Main Menu...\n");
                    break;

                default:
                    System.out.println(
                            "\nInvalid option. Please enter 1 to 4.\n"
                    );
            }
        }
    }

    private void printMenu() {
        System.out.print(
                "\n" +
                "            .-._.---'                        .-.                           \n" +
                "          (_) /                      /     (_) )-.                 /       \n" +
                "             /--.).--..-._..  .-.---/---      /   \\    .-.  .     /-.     \n" +
                "            /   /    (   )  )/   ) /         /     \\ ./.-'_/ \\   /   )   \n" +
                "         .-/   /      `-'  '/   ( /       .-/.      )(__.'/ ._)_/    \\    \n" +
                "        (_/                      `-      (_/  `----'     /                 \n" +
                "------------------------------------------------------------------------------\n" +
                "                   1. Search booking by confirmation number                   \n" +
                "                   2. Check room availability                                 \n" +
                "                   3. View billing details                                    \n" +
                "                   4. Back to Main Menu                                       \n" +
                "------------------------------------------------------------------------------\n" +
                "Enter 1 - 4 to select an option: "
        );
    }

    private String readConfNo() {
        System.out.print("Enter 8-digit confirmation number: ");
        return scanner.nextLine().trim();
    }

    private void handleSearch() {
        String confNo = readConfNo();

        ControllerResult result =
                controller.searchByConfirmationNo(confNo);

        printResult(result);
    }

    private void handleAvailability() {
        String confNo = readConfNo();

        ControllerResult result =
                controller.checkRoomAvailability(confNo);

        printResult(result);
    }

    private void handleBilling() {
        String confNo = readConfNo();

        ControllerResult result =
                controller.getBillingDetails(confNo);

        printResult(result);
    }

    private void printResult(ControllerResult result) {
        if (result.isOk()) {
            System.out.println(result.getMessage());
        } else {
            System.out.println("Error: " + result.getMessage());
        }

        System.out.println();
    }
}