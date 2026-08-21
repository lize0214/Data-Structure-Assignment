/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Boundary;

import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import Control.BookingController;
import Control.GuestController;
import Control.RoomController;
import Entity.Booking;
import Entity.BookingType;

public class MainMenuUI {

    private final Scanner scanner;
    private final BookingController bookingController;

    private record BookingListCriteria(String keyword, BookingType bookingType,
            String status, String roomType, LocalDate fromDate, LocalDate toDate,
            String sortBy, boolean ascending) { }

    public MainMenuUI() {
        scanner = new Scanner(System.in);
        GuestController guestController = new GuestController();
        RoomController roomController = new RoomController();
        bookingController = new BookingController(guestController, roomController);
    }

    public void run() {
        boolean running = true;

        while (running) {

            clearScreen();
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
                    openBookingList();
                    break;

                case "6":
                    running = false;
                    System.out.println(
                            "\nThank you for using our Areum Resort."
                    );
                    break;

                default:
                    System.out.println(
                            "\nInvalid option. Please enter a number from 1 to 6."
                    );
                    pressEnterToContinue();
                    break;
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
                "                         2. Front Desk Service                                \n" +
                "                         3. VIP Room Allocation                               \n" +
                "                         4. Loyalty Management                                \n" +
                "                         5. View Booking List                                  \n" +
                "                         6. Exit System                                       \n" +
                "------------------------------------------------------------------------------\n" +
                "Enter 1 - 6 to select an option: "
        );
    }

    private void openBookingMenu() {
        BookingUI bookingUI = new BookingUI();
        bookingUI.run();
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

    private void openBookingList() {
        BookingListCriteria criteria = defaultBookingListCriteria();
        boolean returnHome = false;

        while (!returnHome) {
            clearScreen();
            displayBookingList(criteria);

            System.out.println("\nActions: 1=Advanced Search / Filter / Sort"
                    + " | 2=Reset / View All | 0=Return Home");
            switch (readChoice("Select action: ", 0, 2)) {
                case 1 -> criteria = readAdvancedBookingListCriteria();
                case 2 -> criteria = defaultBookingListCriteria();
                case 0 -> returnHome = true;
            }
        }
    }

    private BookingListCriteria defaultBookingListCriteria() {
        return new BookingListCriteria(null, null, null, null,
                null, null, "Check-in Date", false);
    }

    private BookingListCriteria readAdvancedBookingListCriteria() {
        clearScreen();
        System.out.println("-".repeat(126));
        System.out.println("                                          ADVANCED BOOKING LIST OPTIONS");
        System.out.println("-".repeat(126));
        System.out.println("Leave the keyword/date blank or select 0 to include all records.\n");

        System.out.print("Search Confirmation / Holder ID / Holder Name / Room No: ");
        String keyword = optionalInput();
        BookingType bookingType = selectBookingTypeFilter();
        String status = selectBookingStatusFilter();
        String roomType = selectRoomTypeFilter();

        LocalDate fromDate;
        LocalDate toDate;
        while (true) {
            fromDate = readOptionalDate("Check-in start date (yyyy-MM-dd, blank for earliest): ");
            toDate = readOptionalDate("Check-in end date   (yyyy-MM-dd, blank for latest)  : ");
            if (fromDate == null || toDate == null || !fromDate.isAfter(toDate)) break;
            System.out.println("Start date cannot be after end date. Please try again.");
        }

        String sortBy = selectBookingListSort();
        boolean ascending = readChoice(
                "Order: 1=Ascending, 2=Descending: ", 1, 2) == 1;

        return new BookingListCriteria(keyword, bookingType, status, roomType,
                fromDate, toDate, sortBy, ascending);
    }

    private void displayBookingList(BookingListCriteria criteria) {
        // Reload controllers so the Home Page always shows the latest file state.
        GuestController latestGuestController = new GuestController();
        RoomController latestRoomController = new RoomController();
        BookingController latestBookingController = new BookingController(
                latestGuestController, latestRoomController);
        Booking[] bookings = latestBookingController.getBookingList(
                criteria.keyword(), criteria.bookingType(), criteria.status(),
                criteria.roomType(), criteria.fromDate(), criteria.toDate(),
                criteria.sortBy(), criteria.ascending());

        System.out.println("-".repeat(126));
        System.out.println("                                                   BOOKING LIST");
        System.out.println("-".repeat(126));
        System.out.println("Filters: Keyword=" + displayFilter(criteria.keyword())
                + " | Type=" + displayBookingType(criteria.bookingType())
                + " | Status=" + displayFilter(criteria.status())
                + " | Room Type=" + displayFilter(criteria.roomType())
                + " | Check-in=" + (criteria.fromDate() == null
                        ? "Earliest" : criteria.fromDate())
                + " to " + (criteria.toDate() == null ? "Latest" : criteria.toDate()));
        System.out.println("Sorting: " + criteria.sortBy() + " / "
                + (criteria.ascending() ? "Ascending" : "Descending")
                + " (Selection Sort) | Filtering: Linear Search");
        System.out.println("-".repeat(126));
        System.out.printf("%-10s %-15s %-8s %-10s %-18s %-7s %-13s %-11s %-11s %-12s%n",
                "CONFIRM", "BOOKING TYPE", "HOLDER", "HOLDER ID", "HOLDER NAME",
                "ROOM", "ROOM TYPE", "CHECK-IN", "CHECK-OUT", "STATUS");
        System.out.println("-".repeat(126));

        if (bookings.length == 0) {
            System.out.println("No bookings match the selected filters.");
        } else {
            for (Booking booking : bookings) {
                String resolvedRoomType = booking.getRoom() == null
                        ? "Unknown" : booking.getRoom().getRoomType();
                System.out.printf("%-10s %-15.15s %-8s %-10s %-18.18s %-7s %-13.13s %-11s %-11s %-12s%n",
                        booking.getConfirmationNo(), displayBookingType(booking.getBookingType()),
                        booking.isMemberBooking() ? "Member" : "Guest",
                        booking.getHolderId(), booking.getHolderName(), booking.getRoomNo(),
                        resolvedRoomType, booking.getCheckInDate(), booking.getCheckOutDate(),
                        booking.getBookingStatus());
            }
        }

        printBookingListSummary(bookings);
    }

    private BookingType selectBookingTypeFilter() {
        System.out.println("Booking Type: 0=All, 1=Standard, 2=Walk-in, 3=VIP Allocation");
        return switch (readChoice("Select booking type: ", 0, 3)) {
            case 1 -> BookingType.STANDARD;
            case 2 -> BookingType.WALK_IN;
            case 3 -> BookingType.VIP_ALLOCATION;
            default -> null;
        };
    }

    private String selectBookingStatusFilter() {
        System.out.println("Status: 0=All, 1=Confirmed, 2=Checked In, 3=Checked Out, 4=Cancelled");
        return switch (readChoice("Select status: ", 0, 4)) {
            case 1 -> "Confirmed";
            case 2 -> "CheckedIn";
            case 3 -> "CheckedOut";
            case 4 -> "Cancelled";
            default -> null;
        };
    }

    private String selectRoomTypeFilter() {
        System.out.println("Room Type: 0=All, 1=Single, 2=Deluxe, 3=Suite, 4=Presidential");
        return switch (readChoice("Select room type: ", 0, 4)) {
            case 1 -> "Single";
            case 2 -> "Deluxe";
            case 3 -> "Suite";
            case 4 -> "Presidential";
            default -> null;
        };
    }

    private String selectBookingListSort() {
        System.out.println("Sort: 1=Check-in Date, 2=Confirmation No, 3=Holder Name,"
                + " 4=Booking Type, 5=Room No");
        return switch (readChoice("Select sorting: ", 1, 5)) {
            case 2 -> "Confirmation No";
            case 3 -> "Holder Name";
            case 4 -> "Booking Type";
            case 5 -> "Room No";
            default -> "Check-in Date";
        };
    }

    private void printBookingListSummary(Booking[] bookings) {
        int standard = 0;
        int walkIn = 0;
        int vip = 0;
        int confirmed = 0;
        int checkedIn = 0;
        int checkedOut = 0;
        int cancelled = 0;

        for (Booking booking : bookings) {
            switch (booking.getBookingType()) {
                case STANDARD -> standard++;
                case WALK_IN -> walkIn++;
                case VIP_ALLOCATION -> vip++;
            }
            switch (booking.getBookingStatus().toLowerCase()) {
                case "confirmed" -> confirmed++;
                case "checkedin" -> checkedIn++;
                case "checkedout" -> checkedOut++;
                case "cancelled" -> cancelled++;
            }
        }

        System.out.println("\nSummary");
        System.out.println("Total Bookings : " + bookings.length);
        System.out.println("By Type        : Standard=" + standard + " | Walk-in=" + walkIn
                + " | VIP Allocation=" + vip);
        System.out.println("By Status      : Confirmed=" + confirmed + " | Checked In=" + checkedIn
                + " | Checked Out=" + checkedOut + " | Cancelled=" + cancelled);
    }

    private int readChoice(String prompt, int minimum, int maximum) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= minimum && choice <= maximum) return choice;
            } catch (NumberFormatException ignored) {
                // Print the shared validation message below.
            }
            System.out.printf("Please enter a number from %d to %d.%n", minimum, maximum);
        }
    }

    private LocalDate readOptionalDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = optionalInput();
            if (input == null) return null;
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Please use yyyy-MM-dd.");
            }
        }
    }

    private String optionalInput() {
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? null : input;
    }

    private String displayFilter(String value) {
        return value == null || value.isBlank() ? "All" : value;
    }

    private String displayBookingType(BookingType bookingType) {
        if (bookingType == null) return "All";
        return switch (bookingType) {
            case STANDARD -> "Standard";
            case WALK_IN -> "Walk-in";
            case VIP_ALLOCATION -> "VIP Allocation";
        };
    }
    
    private void pressEnterToContinue() {
        System.out.print("\nPress ENTER to continue...");
        scanner.nextLine();
    }
    
    private void clearScreen() {
        for (int i = 0; i < 40; i++) {
            System.out.println();
        }
    }
}
