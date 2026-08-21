package Boundary;

import ADT.HashTableInterface;
import ADT.ListInterface;
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

    private record QueueReportCriteria(String keyword, String tier,
            String roomType, long minimumWaiting, String sortBy) { }

    private record AllocationReportCriteria(LocalDate fromDate, LocalDate toDate,
            String keyword, String tier, String roomType, Boolean preferenceMatched,
            long minimumWaiting, String sortBy) { }

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

        ListInterface<VIPQueueEntry> queue = vipController.viewQueue();
        if (queue.isEmpty()) {
            System.out.println("The VIP queue is currently empty.");
            enterToReturn();
            return;
        }

        System.out.printf("%-4s %-12s %-12s %-20s %-20s%n",
                "#", "Member ID", "Tier", "Preferred Room", "Registration Time");
        System.out.println("-".repeat(75));

        for (int index = 1; index <= queue.size(); index++) {
            VIPQueueEntry entry = queue.getEntry(index);
            String roomPref = entry.getPreferredRoomType() != null ? entry.getPreferredRoomType() : "Any";
            System.out.printf("%-4d %-12s %-12s %-20s %-20s%n",
                    index,
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

        ListInterface<Room> rooms = vipController.viewAvailableRooms();
        if (rooms.isEmpty()) {
            System.out.println("No rooms currently available for allocation.");
            enterToReturn();
            return;
        }

        System.out.printf("%-10s %-15s %-10s %-15s%n",
                "Room No", "Type", "Price", "Status");
        System.out.println("-".repeat(55));

        for (int i = 1; i <= rooms.size(); i++) {
            Room room = rooms.getEntry(i);
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
        QueueReportCriteria criteria = defaultQueueReportCriteria();
        boolean returnToReports = false;

        while (!returnToReports) {
            clearScreen();
            displayQueueDemandReport(criteria);
            System.out.println("\nActions: 1=Advanced Search / Filter / Sort"
                    + " | 2=Reset / View All | 0=Return to Reports");
            switch (readChoice("Select action: ", 0, 2)) {
                case 1 -> criteria = readAdvancedQueueReportCriteria();
                case 2 -> criteria = defaultQueueReportCriteria();
                case 0 -> returnToReports = true;
            }
        }
    }

    private QueueReportCriteria defaultQueueReportCriteria() {
        return new QueueReportCriteria(null, null, null, 0, "Priority");
    }

    private QueueReportCriteria readAdvancedQueueReportCriteria() {
        clearScreen();
        printPageHeader("ADVANCED QUEUE REPORT OPTIONS", REPORT_WIDTH);
        System.out.println("Leave keyword blank or select 0 to include all records.\n");
        String keyword = readOptionalString("Search Member ID / Name: ");
        String tier = selectTierFilter();
        String roomType = selectRoomTypeFilter(true);
        long minimumWaiting = readOptionalNonNegativeLong(
                "Minimum waiting minutes (blank for 0): ");
        String sortBy = selectQueueSort();
        return new QueueReportCriteria(
                keyword, tier, roomType, minimumWaiting, sortBy);
    }

    private void displayQueueDemandReport(QueueReportCriteria criteria) {
        VIPQueueDemandReport report = vipController.getQueueDemandReport(
                criteria.keyword(), criteria.tier(), criteria.roomType(),
                criteria.minimumWaiting(), criteria.sortBy());

        printPageHeader("VIP QUEUE & ROOM DEMAND REPORT", REPORT_WIDTH);
        System.out.println("Generated at: " + formatDateTime(report.getGeneratedAt()));
        System.out.println("Filters     : Keyword=" + displayFilter(criteria.keyword())
                + " | Tier=" + displayFilter(criteria.tier())
                + " | Preferred Room=" + displayFilter(criteria.roomType())
                + " | Minimum Waiting=" + criteria.minimumWaiting() + "m");
        System.out.println("Sorted by   : " + criteria.sortBy() + " (Merge Sort)");
        System.out.println("Search      : Member ID / Name substring (Linear Search)");
        System.out.println();
        System.out.printf("%-4s %-10s %-18s %-10s %-8s %-14s %-19s %-12s%n",
                "Rank", "Member ID", "Member Name", "Tier", "Priority",
                "Preferred", "Registered At", "Waiting");
        System.out.println("-".repeat(105));

        if (report.getQueueRows().isEmpty()) {
            System.out.println("No VIP members are currently waiting.");
        } else {
            ListInterface<VIPQueueDemandReport.QueueRow> rows = report.getQueueRows();
            for (int i = 1; i <= rows.size(); i++) {
                VIPQueueDemandReport.QueueRow row = rows.getEntry(i);
                System.out.printf("%-4d %-10s %-18s %-10s %-8d %-14s %-19s %-12s%n",
                        row.rank(), row.memberId(), shorten(row.memberName(), 18),
                        row.memberTier(), row.priority(), row.preferredRoomType(),
                        formatDateTime(row.registrationTime()), formatDuration(row.waitingMinutes()));
            }
        }

        System.out.println("\nRoom Demand vs Allocatable Supply");
        System.out.printf("%-16s %10s %12s %10s%n", "Room Type", "Demand", "Available", "Shortage");
        System.out.println("-".repeat(52));
        ListInterface<VIPQueueDemandReport.RoomDemandRow> demandRows = report.getRoomDemandRows();
        for (int i = 1; i <= demandRows.size(); i++) {
            VIPQueueDemandReport.RoomDemandRow row = demandRows.getEntry(i);
            System.out.printf("%-16s %10d %12d %10d%n",
                    row.roomType(), row.demand(), row.available(), row.shortage());
        }

        System.out.println("\nSummary");
        System.out.println("Total Waiting       : " + report.getTotalWaiting());
        printCounts("Waiting by Tier     : ", report.getTierCounts());
        System.out.println("Average Waiting     : " + formatDuration(Math.round(report.getAverageWaitingMinutes())));
        System.out.println("Longest Waiting     : " + formatDuration(report.getLongestWaitingMinutes()));
        System.out.println("Management Insight  : " + queueManagementInsight(report));
    }

    private void handleAllocationPerformanceReport() {
        AllocationReportCriteria criteria = defaultAllocationReportCriteria();
        boolean returnToReports = false;

        while (!returnToReports) {
            clearScreen();
            displayAllocationPerformanceReport(criteria);
            System.out.println("\nActions: 1=Advanced Search / Filter / Sort"
                    + " | 2=Reset / View All | 0=Return to Reports");
            switch (readChoice("Select action: ", 0, 2)) {
                case 1 -> criteria = readAdvancedAllocationReportCriteria();
                case 2 -> criteria = defaultAllocationReportCriteria();
                case 0 -> returnToReports = true;
            }
        }
    }

    private AllocationReportCriteria defaultAllocationReportCriteria() {
        return new AllocationReportCriteria(null, null, null, null,
                null, null, 0, "Latest allocation");
    }

    private AllocationReportCriteria readAdvancedAllocationReportCriteria() {
        clearScreen();
        printPageHeader("ADVANCED ALLOCATION REPORT OPTIONS", REPORT_WIDTH);
        System.out.println("Enter an optional allocation date range (format: yyyy-MM-dd).");
        LocalDate fromDate;
        LocalDate toDate;
        while (true) {
            fromDate = readOptionalDate("Start date (blank for earliest): ");
            toDate = readOptionalDate("End date   (blank for latest)  : ");
            if (fromDate == null || toDate == null || !fromDate.isAfter(toDate)) break;
            System.out.println("Start date cannot be after end date. Please try again.");
        }
        String keyword = readOptionalString("Search Confirmation No / Member ID: ");
        String tier = selectTierFilter();
        String roomType = selectRoomTypeFilter(false);
        Boolean preferenceMatched = selectPreferenceMatchFilter();
        long minimumWaiting = readOptionalNonNegativeLong(
                "Minimum waiting minutes (blank for 0): ");
        String sortBy = selectAllocationSort();
        return new AllocationReportCriteria(fromDate, toDate, keyword, tier,
                roomType, preferenceMatched, minimumWaiting, sortBy);
    }

    private void displayAllocationPerformanceReport(AllocationReportCriteria criteria) {
        VIPAllocationPerformanceReport report =
                vipController.getAllocationPerformanceReport(
                        criteria.fromDate(), criteria.toDate(), criteria.keyword(),
                        criteria.tier(), criteria.roomType(), criteria.preferenceMatched(),
                        criteria.minimumWaiting(), criteria.sortBy());
        printPageHeader("VIP ALLOCATION PERFORMANCE REPORT", REPORT_WIDTH);
        System.out.println("Allocation period: "
                + (criteria.fromDate() == null ? "Earliest" : criteria.fromDate()) + " to "
                + (criteria.toDate() == null ? "Latest" : criteria.toDate()));
        System.out.println("Filters          : Keyword=" + displayFilter(criteria.keyword())
                + " | Tier=" + displayFilter(criteria.tier())
                + " | Room=" + displayFilter(criteria.roomType())
                + " | Preference Match="
                + displayPreferenceFilter(criteria.preferenceMatched())
                + " | Minimum Waiting=" + criteria.minimumWaiting() + "m");
        System.out.println("Sorted by        : " + criteria.sortBy() + " (Merge Sort)");
        System.out.println("Search           : Confirmation / Member substring (Linear Search)");
        System.out.println();
        System.out.printf("%-10s %-10s %-10s %-13s %-8s %-13s %-19s %-12s %-9s%n",
                "Confirm", "Member", "Tier", "Preferred", "Room", "Room Type",
                "Allocated At", "Waiting", "Matched");
        System.out.println("-".repeat(118));

        if (report.getRecords().isEmpty()) {
            System.out.println("No successful VIP allocations found for this date range.");
        } else {
            ListInterface<VIPAllocationRecord> records = report.getRecords();
            for (int i = 1; i <= records.size(); i++) {
                VIPAllocationRecord record = records.getEntry(i);
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
        System.out.println("Management Insight    : " + allocationManagementInsight(report));
    }

    private String selectTierFilter() {
        System.out.println("Tier: 0=All, 1=Platinum, 2=Diamond, 3=Elite");
        return switch (readChoice("Select tier: ", 0, 3)) {
            case 1 -> "Platinum";
            case 2 -> "Diamond";
            case 3 -> "Elite";
            default -> null;
        };
    }

    private String selectRoomTypeFilter(boolean includeAnyPreference) {
        System.out.println("Room: 0=All, 1=Single, 2=Deluxe, 3=Suite, 4=Presidential"
                + (includeAnyPreference ? ", 5=Any preference" : ""));
        int maximum = includeAnyPreference ? 5 : 4;
        return switch (readChoice("Select room: ", 0, maximum)) {
            case 1 -> "Single";
            case 2 -> "Deluxe";
            case 3 -> "Suite";
            case 4 -> "Presidential";
            case 5 -> "Any";
            default -> null;
        };
    }

    private Boolean selectPreferenceMatchFilter() {
        System.out.println("Preference match: 0=All, 1=Matched, 2=Not matched");
        return switch (readChoice("Select preference match: ", 0, 2)) {
            case 1 -> Boolean.TRUE;
            case 2 -> Boolean.FALSE;
            default -> null;
        };
    }

    private String selectQueueSort() {
        System.out.println("Sort: 1=Priority, 2=Longest waiting, 3=Member name, 4=Room preference");
        return switch (readChoice("Select sorting: ", 1, 4)) {
            case 2 -> "Longest waiting";
            case 3 -> "Member name";
            case 4 -> "Room preference";
            default -> "Priority";
        };
    }

    private String selectAllocationSort() {
        System.out.println("Sort: 1=Latest allocation, 2=Longest waiting, 3=Member ID, 4=Tier priority");
        return switch (readChoice("Select sorting: ", 1, 4)) {
            case 2 -> "Longest waiting";
            case 3 -> "Member ID";
            case 4 -> "Tier priority";
            default -> "Latest allocation";
        };
    }

    private String queueManagementInsight(VIPQueueDemandReport report) {
        VIPQueueDemandReport.RoomDemandRow highestShortage = null;
        ListInterface<VIPQueueDemandReport.RoomDemandRow> demandRows = report.getRoomDemandRows();
        for (int i = 1; i <= demandRows.size(); i++) {
            VIPQueueDemandReport.RoomDemandRow row = demandRows.getEntry(i);
            if (highestShortage == null || row.shortage() > highestShortage.shortage()) {
                highestShortage = row;
            }
        }
        if (report.getTotalWaiting() == 0) {
            return "No VIP queue demand matches the selected business-cycle criteria.";
        }
        if (highestShortage != null && highestShortage.shortage() > 0) {
            return "Prioritise " + highestShortage.roomType() + " capacity; shortage is "
                    + highestShortage.shortage() + " room(s).";
        }
        return "Current allocatable supply covers the filtered VIP room demand.";
    }

    private String allocationManagementInsight(VIPAllocationPerformanceReport report) {
        if (report.getTotalAllocations() == 0) {
            return "No allocation activity matches the selected business-cycle criteria.";
        }
        if (report.getPreferenceRequestCount() > 0
                && report.getPreferenceMatchRate() < 80.0) {
            return String.format("Preference fulfilment is %.2f%%; review room-type capacity.",
                    report.getPreferenceMatchRate());
        }
        if (report.getAverageWaitingMinutes() >= 60.0) {
            return "Average waiting exceeds one hour; review VIP allocation staffing and supply.";
        }
        return "VIP allocation performance is within the report's review thresholds.";
    }

    private String displayFilter(String value) {
        return value == null || value.isBlank() ? "All" : value;
    }

    private String displayPreferenceFilter(Boolean value) {
        if (value == null) return "All";
        return value ? "Matched" : "Not matched";
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

    private void printCounts(String label, HashTableInterface<String, Integer> counts) {
        StringBuilder text = new StringBuilder(label);
        ListInterface<String> keys = counts.keys();
        for (int i = 1; i <= keys.size(); i++) {
            if (i > 1) text.append(" | ");
            String key = keys.getEntry(i);
            text.append(key).append(": ").append(counts.search(key));
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

    private int readChoice(String prompt, int minimum, int maximum) {
        while (true) {
            int choice = readInt(prompt);
            if (choice >= minimum && choice <= maximum) return choice;
            System.out.printf("Please enter a number from %d to %d.%n", minimum, maximum);
        }
    }

    private long readOptionalNonNegativeLong(String prompt) {
        while (true) {
            String input = readOptionalString(prompt);
            if (input == null) return 0;
            try {
                long value = Long.parseLong(input);
                if (value >= 0) return value;
            } catch (NumberFormatException ignored) {
                // A single validation message is printed below.
            }
            System.out.println("Please enter a whole number of 0 or greater.");
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
