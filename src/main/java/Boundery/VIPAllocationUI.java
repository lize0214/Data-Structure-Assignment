package Boundery;

import Control.*;
import Entity.Room;
import Entity.VIPQueueEntry;
import Utility.ControllerResult;

import java.util.List;
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
            printMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> handleAddVIPMember();
                case 2 -> handleAllocateRoom();
                case 3 -> handleViewQueue();
                case 4 -> handleRemoveVIPMember();
                case 5 -> handleViewAvailableRooms();
                case 0 -> exit = true;
                default -> System.out.println("Invalid choice, please try again.");
            }
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
            "                   0. Return to Main Menu                \n" +
            "------------------------------------------------------------------------------\n" +
            "                             Queue size: " + vipController.getQueueSize() + "\n"
        );
    }

    // ───────────────────── Menu Handlers ─────────────────────

    private void handleAddVIPMember() {
        System.out.println("\n--- Add VIP Member to Priority Queue ---");

        String memberId = readString("Member ID: ");
        String preferredRoomType = readOptionalString("Preferred Room Type (Single/Deluxe/Suite/Presidential, or blank for any): ");

        ControllerResult result = vipController.enqueueVIPMember(memberId, preferredRoomType);
        printResult(result);
    }

    private void handleAllocateRoom() {
        System.out.println("\n--- Allocate Room to Next VIP Member ---");
        System.out.println("Allocating room to highest-priority VIP member...");

        ControllerResult result = vipController.allocateNextVIPRoom();
        printResult(result);
    }

    private void handleViewQueue() {
        System.out.println("\n--- VIP Priority Queue ---");

        List<VIPQueueEntry> queue = vipController.viewQueue();
        if (queue.isEmpty()) {
            System.out.println("The VIP queue is currently empty.");
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
        System.out.println("\n--- Remove VIP Member from Queue ---");

        String memberId = readString("Member ID to remove: ");

        System.out.print("Are you sure you want to remove member " + memberId + "? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("Cancelled.");
            return;
        }

        ControllerResult result = vipController.dequeueVIPMember(memberId);
        printResult(result);
    }

    private void handleViewAvailableRooms() {
        System.out.println("\n--- Available Rooms ---");

        List<Room> rooms = vipController.viewAvailableRooms();
        if (rooms.isEmpty()) {
            System.out.println("No rooms currently available for allocation.");
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
