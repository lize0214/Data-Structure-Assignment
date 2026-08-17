package Boundary;

import Control.*;
import Entity.Room;
import Entity.VIPAllocationPerformanceReport;
import Entity.VIPAllocationRecord;
import Entity.VIPQueueDemandReport;
import Entity.VIPQueueEntry;
import Utility.ControllerResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console menu for the VIP Priority Room Allocation module.
 * <p>
 * Allows front-desk staff to manage the VIP priority queue:
 * add eligible high-tier members, allocate rooms by priority,
 * view the queue, and handle cancellations.
 * </p>
 */
public class VIPAllocationUI {

    private static final int PAGE_WIDTH = 78;
    private static final int REPORT_WIDTH = 118;
    private static final DateTimeFormatter REPORT_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Scanner scanner;
    private final VIPAllocationController vipController;

    /**
     * Constructs the UI with its own controller instances.
     * Each UI invocation creates fresh controller instances so data
     * is always loaded from the latest file state.
     */
    public VIPAllocationUI() {
        this.scanner = new Scanner(System.in);

        MemberController memberController = new MemberController();
        RoomController roomController = new RoomController();
        GuestController guestController = new GuestController();
        BookingController bookingController = new BookingController(guestController, roomController);

        this.vipController = new VIPAllocationController(
                memberController, roomController, guestController, bookingController);
    }

    /**
     * Main menu loop for the VIP allocation module.
     */
    public void run() {
        boolean exit = false;
        while (!exit) {
            clearScreen();
            printMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> handleAddVIPMember();
                case 2 -> handleAllocateRoom();
                case 3 -> handleViewQueue();
                case 4 -> handleRemoveVIPMember();
                case 5 -> handleViewAvailableRooms();
                case 6 -> handleViewReports();
                case 0 -> exit = true;
                default -> {
                    System.out.println("Invalid choice, please try again.");
                    enterToReturn();
                }
            }
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


    private void enterToReturn() {
        System.out.print("Enter 0 to return: ");
        while (!scanner.nextLine().trim().equals("0")) {
            System.out.print("Invalid input. Please enter 0 to return: ");
        }
    }

    // ───────────────────── Menu Display ─────────────────────

    private void printMenu() {
        System.out.println(
            "              .;;;;.  .-.         .-.                                     .        \r\n" +
            ".;.       .-.' .;'  `(_) )-.     (_) )-.         .-.            .-.   ...;...      \r\n" +
            " `;     .'    .;'      .:   \\      .:   \\  .;.::.`-' .-.  .;.::.`-'    .'.    .-.  \r\n" +
            "  ;;  .'     .;'      .:'    )    .:'    ) .;   ;'  ;   ';.   ;'    .;   `:  ;    \r\n" +
            " ;;  ;      .;'     .-:. `--'   .-:. `--'.;' _.;:._.`;;'.;' _.;:._..;      `.'     \r\n" +
            " `;.'   .;;;;;;;;;'(_/         (_/                                      -.;'       \r\n" +
            "                  .-.                                     /\\      .;    .;                         .                  \r\n" +
            "                 (_) )-.                               _ / |     .;'   .;'                    ...;....-.              \r\n" +
            "                  .:   \\  .-.   .-.  . ,';.,';.      (  /  |  . .;    .;  .-.   .-.   .-.       .'    `-' .-.  . ,';. \r\n" +
            "                  .::.   );   ';   ';;  ;;  ;;       `/.__|_.'::    ::  ;   ';    ;   :    .;     ;'  ;   ';;  ;; \r\n" +
            "                .-:. `:-' `;;'  `;;' ';  ;;  ';    .:' /    | _;;_.-_;;_.-`;;'  `;;;;'`:::'-'.;    _.;:._.`;;' ';  ;;  \r\n" +
            "               (_/     `:._.        _;        `-' (__.'     `-'                                                ;    `.\r\n" +
            "------------------------------------------------------------------------------\n" +
            "                   1. Add VIP Member to Priority Queue   \n" +
            "                   2. Allocate Room to Next VIP Member   \n" +
            "                   3. View VIP Priority Queue            \n" +
            "                   4. Remove VIP Member from Queue       \n" +
            "                   5. View Available Rooms               \n" +
            "                   6. View Reports                       \n" +
            "                   0. Return to Main Menu                \n" +
            "------------------------------------------------------------------------------\n" +
            "                             Queue size: " + vipController.getQueueSize() + "\n"
        );
    }

    // ───────────────────── Menu Handlers ─────────────────────

    private void handleAddVIPMember() {
        clearScreen();
        printPageHeader("ADD VIP MEMBER TO PRIORITY QUEUE", PAGE_WIDTH);

        String memberId = readString("Member ID: ");
        String preferredRoomType = readOptionalString("Preferred Room Type (Single/Deluxe/Suite/Presidential, or blank for any): ");

        ControllerResult result = vipController.enqueueVIPMember(memberId, preferredRoomType);
        printResult(result);
        enterToReturn();
    }

    private void handleAllocateRoom() {
        clearScreen();
        printPageHeader("ALLOCATE ROOM TO NEXT VIP MEMBER", PAGE_WIDTH);
        System.out.println("Allocating room to highest-priority VIP member...");

        ControllerResult result = vipController.allocateNextVIPRoom();
        printResult(result);
        enterToReturn();
    }

    private void handleViewQueue() {
        clearScreen();
        printPageHeader("VIP PRIORITY QUEUE", PAGE_WIDTH);

        List<VIPQueueEntry> queue = vipController.viewQueue();
        if (queue.isEmpty()) {
            System.out.println("The VIP queue is currently empty.");
            enterToReturn();
            return;
        }

        System.out.printf("%-4s %-12s %-12s %-20s %-20s%n",
                "#", "Member ID", "Tier", "Preferred Room", "Registration Time");
        System.out.println("-".repeat(75));

        int index = 1;
        for (VIPQueueEntry entry : queue) {
            String roomPref = entry.getPreferredRoomType() != null ? entry.getPreferredRoomType() : "Any";
            System.out.printf("%-4d %-12s %-12s %-20s %-20s%n",
                    index++,
                    entry.getMemberId(),
                    entry.getMemberTier() + " (P" + entry.getTierPriority() + ")",
                    roomPref,
                    entry.getRegistrationTime());
        }
        enterToReturn();
        System.out.println();
    
    }

    private void handleRemoveVIPMember() {
        clearScreen();
        printPageHeader("REMOVE VIP MEMBER FROM QUEUE", PAGE_WIDTH);

        String memberId = readString("Member ID to remove: ");

        System.out.print("Are you sure you want to remove member " + memberId + "? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("Cancelled.");
            enterToReturn();
            return;
        }

        ControllerResult result = vipController.dequeueVIPMember(memberId);
        printResult(result);
        enterToReturn();
    }

    private void handleViewAvailableRooms() {
        clearScreen();
        printPageHeader("AVAILABLE ROOMS", PAGE_WIDTH);

        List<Room> rooms = vipController.viewAvailableRooms();
        if (rooms.isEmpty()) {
            System.out.println("No rooms currently available for allocation.");
            enterToReturn();
            return;
        }

        System.out.printf("%-10s %-15s %-10s %-15s%n",
                "Room No", "Type", "Price", "Status");
        System.out.println("-".repeat(55));

        for (Room room : rooms) {
            System.out.printf("%-10s %-15s RM%-9.2f %-15s%n",
                    room.getRoomNo(),
                    room.getRoomType(),
                    room.getPrice(),
                    room.getStatus());
        }
        enterToReturn();
        System.out.println();
    }

    private void handleViewReports() {
        boolean returnToVipMenu = false;
        while (!returnToVipMenu) {
            clearScreen();
            printPageHeader("VIP ALLOCATION REPORTS", PAGE_WIDTH);
            System.out.println("                   1. VIP Queue & Room Demand Report");
            System.out.println("                   2. VIP Allocation Performance Report");
            System.out.println("                   0. Return to VIP Allocation Menu");
            System.out.println("-".repeat(PAGE_WIDTH));

            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> handleQueueDemandReport();
                case 2 -> handleAllocationPerformanceReport();
                case 0 -> returnToVipMenu = true;
                default -> {
                    System.out.println("Invalid choice, please try again.");
                    enterToReturn();
                }
            }
        }
    }

    private void handleQueueDemandReport() {
        clearScreen();
        VIPQueueDemandReport report = vipController.getQueueDemandReport();

        printPageHeader("VIP QUEUE & ROOM DEMAND REPORT", REPORT_WIDTH);
        System.out.println("Generated at: " + formatDateTime(report.getGeneratedAt()));
        System.out.println();
        System.out.printf("%-4s %-10s %-18s %-10s %-8s %-14s %-19s %-12s%n",
                "Rank", "Member ID", "Member Name", "Tier", "Priority",
                "Preferred", "Registered At", "Waiting");
        System.out.println("-".repeat(105));

        if (report.getQueueRows().isEmpty()) {
            System.out.println("No VIP members are currently waiting.");
        } else {
            for (VIPQueueDemandReport.QueueRow row : report.getQueueRows()) {
                System.out.printf("%-4d %-10s %-18s %-10s %-8d %-14s %-19s %-12s%n",
                        row.rank(), row.memberId(), shorten(row.memberName(), 18),
                        row.memberTier(), row.priority(), row.preferredRoomType(),
                        formatDateTime(row.registrationTime()), formatDuration(row.waitingMinutes()));
            }
        }

        System.out.println("\nRoom Demand vs Allocatable Supply");
        System.out.printf("%-16s %10s %12s %10s%n", "Room Type", "Demand", "Available", "Shortage");
        System.out.println("-".repeat(52));
        for (VIPQueueDemandReport.RoomDemandRow row : report.getRoomDemandRows()) {
            System.out.printf("%-16s %10d %12d %10d%n",
                    row.roomType(), row.demand(), row.available(), row.shortage());
        }

        System.out.println("\nSummary");
        System.out.println("Total Waiting       : " + report.getTotalWaiting());
        printCounts("Waiting by Tier     : ", report.getTierCounts());
        System.out.println("Average Waiting     : " + formatDuration(Math.round(report.getAverageWaitingMinutes())));
        System.out.println("Longest Waiting     : " + formatDuration(report.getLongestWaitingMinutes()));
        enterToReturn();
    }

    private void handleAllocationPerformanceReport() {
        clearScreen();
        printPageHeader("VIP ALLOCATION PERFORMANCE REPORT", REPORT_WIDTH);
        System.out.println("Enter an optional allocation date range (format: yyyy-MM-dd).");
        LocalDate fromDate;
        LocalDate toDate;
        while (true) {
            fromDate = readOptionalDate("Start date (blank for earliest): ");
            toDate = readOptionalDate("End date   (blank for latest)  : ");
            if (fromDate == null || toDate == null || !fromDate.isAfter(toDate)) break;
            System.out.println("Start date cannot be after end date. Please try again.");
        }

        VIPAllocationPerformanceReport report =
                vipController.getAllocationPerformanceReport(fromDate, toDate);
        System.out.println("-".repeat(REPORT_WIDTH));
        System.out.println("Allocation period: "
                + (fromDate == null ? "Earliest" : fromDate) + " to "
                + (toDate == null ? "Latest" : toDate));
        System.out.println();
        System.out.printf("%-10s %-10s %-10s %-13s %-8s %-13s %-19s %-12s %-9s%n",
                "Confirm", "Member", "Tier", "Preferred", "Room", "Room Type",
                "Allocated At", "Waiting", "Matched");
        System.out.println("-".repeat(118));

        if (report.getRecords().isEmpty()) {
            System.out.println("No successful VIP allocations found for this date range.");
        } else {
            for (VIPAllocationRecord record : report.getRecords()) {
                String preferred = record.hasRoomPreference()
                        ? record.getPreferredRoomType() : "Any";
                String matched = record.hasRoomPreference()
                        ? (record.isPreferenceMatched() ? "Yes" : "No") : "N/A";
                System.out.printf("%-10s %-10s %-10s %-13s %-8s %-13s %-19s %-12s %-9s%n",
                        record.getConfirmationNo(), record.getMemberId(), record.getMemberTier(),
                        preferred, record.getAllocatedRoomNo(), record.getAllocatedRoomType(),
                        formatDateTime(record.getAllocationTime()),
                        formatDuration(record.getWaitingMinutes()), matched);
            }
        }

        System.out.println("\nSummary");
        System.out.println("Successful Allocations : " + report.getTotalAllocations());
        printCounts("Allocations by Tier   : ", report.getTierCounts());
        printCounts("Allocations by Room   : ", report.getRoomTypeCounts());
        System.out.println("Average Waiting        : "
                + formatDuration(Math.round(report.getAverageWaitingMinutes())));
        System.out.println("Longest Waiting        : "
                + formatDuration(report.getLongestWaitingMinutes()));
        if (report.getPreferenceRequestCount() == 0) {
            System.out.println("Preference Match Rate  : N/A (no room preferences requested)");
        } else {
            System.out.printf("Preference Match Rate  : %.2f%% (%d/%d)%n",
                    report.getPreferenceMatchRate(), report.getPreferenceMatchCount(),
                    report.getPreferenceRequestCount());
        }
        enterToReturn();
    }

    private void printPageHeader(String title, int width) {
        System.out.println("-".repeat(width));
        int padding = Math.max(0, (width - title.length()) / 2);
        System.out.println(" ".repeat(padding) + title);
        System.out.println("-".repeat(width));
    }

    private LocalDate readOptionalDate(String prompt) {
        while (true) {
            String input = readOptionalString(prompt);
            if (input == null) return null;
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Please use yyyy-MM-dd, for example 2026-08-17.");
            }
        }
    }

    private void printCounts(String label, Map<String, Integer> counts) {
        StringBuilder text = new StringBuilder(label);
        boolean first = true;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (!first) text.append(" | ");
            text.append(entry.getKey()).append(": ").append(entry.getValue());
            first = false;
        }
        System.out.println(text);
    }

    private String formatDuration(long totalMinutes) {
        long safeMinutes = Math.max(0, totalMinutes);
        long days = safeMinutes / (24 * 60);
        long hours = (safeMinutes % (24 * 60)) / 60;
        long minutes = safeMinutes % 60;
        if (days > 0) return String.format("%dd %02dh %02dm", days, hours, minutes);
        if (hours > 0) return String.format("%dh %02dm", hours, minutes);
        return minutes + "m";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "N/A" : dateTime.format(REPORT_DATE_TIME_FORMAT);
    }

    private String shorten(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 1) + ".";
    }

    // ───────────────────── Input Helpers ─────────────────────

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Reads an optional string — returns null if the input is blank.
     */
    private String readOptionalString(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? null : input;
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void printResult(ControllerResult result) {
        if (result.isOk()) {
            System.out.println("SUCCESS" + (result.getMessage() != null ? ": " + result.getMessage() : "."));
        } else {
            System.out.println("FAILED: " + result.getMessage());
        }
    }
}
