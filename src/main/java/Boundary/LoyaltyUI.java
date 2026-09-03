package Boundary;

import ADT.ListInterface;
import Control.LoyaltyController;
import Control.LoyaltyController.ExpiredPointsResult;
import Control.LoyaltyController.RewardPopularity;
import Control.MemberController;
import Entity.Member;
import Entity.PointsTransaction;
import Entity.Reward;
import Entity.RewardRedemption;
import Utility.ControllerResult;

import java.util.Scanner;
import java.time.format.DateTimeFormatter;

/*
 * Author: Tan Pei Xing
 */
public class LoyaltyUI {

    private static final int WIDTH = 80;

    private final Scanner scanner;
    private final LoyaltyController loyaltyController;
    private Member currentMember;

    // Initializes the loyalty UI with a new member controller.
    public LoyaltyUI() {
        this.scanner = new Scanner(System.in);
        MemberController memberController = new MemberController();
        this.loyaltyController = new LoyaltyController(memberController);
    }

    // Initializes the UI using the shared member controller.
    public LoyaltyUI(MemberController sharedMemberController) {
        this.scanner = new Scanner(System.in);
        this.loyaltyController = new LoyaltyController(sharedMemberController);
    }

    private static final String[] BANNER_MAIN = {
        "          .-.                             /\\         .-.  .--------'       ",
        "         / (_)    .--.    .-.-.   .-  _  / |        / (_)(_)   /  .-.   .- ",
        "        /        /    )`-'    /  (   (  /  |  .    /          /     /  (   ",
        "       /        /    /       (    )   `/.__|_.'   /          /     (    )  ",
        "    .-/.    .-.(    /      .  `..'.:' /    |   .-/.    .-..-/._  .  `..'   ",
        "   (_/ `-._.    `-.'      (__.-' (__.'     `-'(_/ `-._.  (_/  `-(__.-'     "
    };

    private static final String[] BANNER_MEMBER = {
        "          .-.             .-  .-.       .-.               .-.-.         ",
        "            /|/|  .---;`-'      /|/|   (_) )-.    .---;`-' (_) )-.      ",
        "           /   | (   (_)       /   |      / __)  (   (_)      /   \\     ",
        "          /    |  )--         /    |     /    `.  )--        /     )    ",
        "     .-' /     | (      /.-' /     |    /'      )(      / .-/  `--'     ",
        "    (__.'      `.`\\___.'(__.'      `.(_/  `----' `\\___.' (_/     `-._)  "
    };

    private static final String[] BANNER_REGISTER = {
        "      .-.                .-       .-. .----.        .-..--------'     .-.-.         ",
        "     (_) )-.     .---;`-'  .--.`-'      /   ` .--.-'  (_)   / .---;`-' (_) )-.     ",
        "        /   \\   (   (_)   /  (_;       /     (  (_)        / (   (_)      /   \\    ",
        "       /     )   )--     /            /       `-.         /   )--        /     )   ",
        "    .-/  `--'   (      /(     --;-   /      _    )     .-/._ (      / .-/  `--'    ",
        "   (_/     `-._)`\\___.'  `.___.'.---------'(_.--'     (_/  `-`\\___.' (_/     `-._) "
    };

    private static final String[] BANNER_REWARDS = {
        "       .-.                .-                /\\     .-.          .-.              .-.  ",
        "      (_) )-.     .---;`-'..-.     .-.   _  / |    (_) )-.      (_) )-.     .--.-'     ",
        "         /   \\   (   (_)     )   (     (  /  |  .    /   \\        /   \\   (  (_)      ",
        "        /     )   )--       /     \\     `/.__|_.'   /     )      /     \\   `-.        ",
        "     .-/  `--'   (      /  (   .   ).:' /    |   .-/  `--'    .-/.      )_    )       ",
        "    (_/     `-._)`\\___.'    `-' `-'(__.'     `-'(_/     `-._)(_/  `----'(_.--'        "
    };

    private static final String[] BANNER_TIERS = {
        "     .--------'  .----.         .-.-.                .-.  ",
        "    (_)   /        /   `.---;`-' (_) )-.       .--.-'     ",
        "         /        /    (   (_)      /   \\     (  (_)      ",
        "        /        /      )--        /     )     `-.        ",
        "     .-/._      /      (      / .-/  `--'    _    )      ",
        "    (_/  `-.---------' `\\___.' (_/     `-._)(_.--'       "
    };

    private static final String[] BANNER_NOTIFICATIONS = {
        "          .-.                 .--------'  .----.  .-._.---'.----.            ",
        "            /  |   .--.    .-(_)   /        /   `(_) /       /   `           ",
        "            /\\  |  /    )`-'       /        /        /--.    /                ",
        "           /  \\ | /    /          /        /        /       /                 ",
        "        .-' /    \\|(    /        .-/._      /      .-/       /                  ",
        "       (__.'      `.`-.'        (_/  `-.---------'(_/   .---------'             ",
        "           .-._   .-._. /\\   .--------'  .----.         .-.             .-.  ",
        "         ..' (_)`-'  _  / |  (_)   /        /   `.--.    .-/  |    .--.-'     ",
        "         |         (  /  |  .    /        /    /    )`-' /\\  |   (  (_)      ",
        "         |    _     `/.__|_.'   /    /        /    /    /  \\ |    `-.        ",
        "         `.    ).:' /    |   .-/._      /    (    /.-' /    \\|  _    )       ",
        "           `--'(__.'     `-'(_/  `-.---------'`-.'(__.'      `.(_.--'        "
    };

    private static final String[] BANNER_REPORTS = {
        "      .-.                .-.-.                  .-.        .--------'     .-. ",
        "     (_) )-.     .---;`-' (_) )-.    .--.    .-(_) )-.    (_)   /   .--.-'    ",
        "        /   \\   (   (_)      /   \\  /    )`-'     /   \\        /   (  (_)     ",
        "       /     )   )--        /     )/    /        /     )      /     `-.       ",
        "    .-/  `--'   (      / .-/  `--'(    /      .-/  `--'    .-/._  _    )      ",
        "   (_/     `-._)`\\___.' (_/        `-.'      (_/     `-._)(_/  `-(_.--'       "
    };

    // Displays a banner centered within the UI width.
    private void printCenteredBanner(String[] banner) {

        if (banner == null || banner.length == 0) {
            return;
        }

        System.out.println();

        for (String line : banner) {

            int padding = Math.max(
                    0,
                    (WIDTH - line.length()) / 2
            );

            System.out.println(
                    " ".repeat(padding) + line
            );
        }
    }

    // Displays a centered section header.
    private void printHeader(String title) {
        System.out.println();
        printDivider();
        int padding = Math.max(0, (WIDTH - title.length()) / 2);
        System.out.println(" ".repeat(padding) + title);
        printDivider();
    }

    // Displays a horizontal divider.
    private void printDivider() {
        System.out.println("-".repeat(WIDTH));
    }

    // Displays the cancel input hint.
    private void printBackHint() {
        System.out.println("(Enter 'back' at any input to cancel.)");
        System.out.println();
    }

    // Formats points with comma separators.
    private String formatPoints(int points) {
        return String.format("%,d", points);
    }

    // Returns the length of text safely.
    private int textLength(String text) {
        return text == null ? 0 : text.length();
    }

// Cleans text so it stays on one table line.
    private String safeText(String text) {
        if (text == null || text.isBlank()) {
            return "-";
        }

        return text
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
    }

// Prints a table row using dynamic column widths.
    private void printDynamicRow(String[] values, int[] widths) {

        System.out.print("|");

        for (int i = 0; i < values.length; i++) {

            String value = safeText(values[i]);

            System.out.printf(
                    " %-" + widths[i] + "s |",
                    value
            );
        }

        System.out.println();
    }

    // Builds a compact member summary.
    private String memberSummaryLine(Member member) {
        return member.getMemberId() + " | " + member.getName()
                + " | " + member.getTier() + " | " + formatPoints(member.getPoints()) + " pts";
    }

    // Adds blank lines to clear the console view.
    private void clearScreen() {
        for (int i = 0; i < 60; i++) {
            System.out.println();
        }
    }

    // Displays a short loading animation.
    private void showLoading(String message) {
        if (System.console() != null) {
            String[] frames = {"|", "/", "-", "\\"};
            try {
                for (int cycle = 0; cycle < 6; cycle++) {
                    for (String frame : frames) {
                        System.out.print("\r" + message + " " + frame);
                        Thread.sleep(125);
                    }
                }
                System.out.print("\r" + " ".repeat(message.length() + 2) + "\r");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            System.out.print(message);
            try {
                for (int i = 0; i < 6; i++) {
                    Thread.sleep(500);
                    System.out.print(".");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println();
        }
    }

    // Gets confirmation before an action.
    private boolean confirmAction(String message) {
        while (true) {
            System.out.print(message + " (Y/N): ");
            String input = scanner.nextLine().trim();

            if (isCancelKeyword(input)) {
                throw new ActionCancelledException();
            }

            if (input.equalsIgnoreCase("Y")) {
                return true;
            }

            if (input.equalsIgnoreCase("N")) {
                return false;
            }

            System.out.println("Please enter Y or N.");
        }
    }

    // Sets the current member.
    private void setCurrentMember(Member member) {
        this.currentMember = member;
    }

    // Displays the current member summary.
    private void printCurrentMemberBanner() {

        if (currentMember == null) {
            return;
        }

        System.out.println(
                "CURRENT MEMBER: "
                + memberSummaryLine(currentMember)
        );
    }

    // Gets an existing member ID using the current member when available.
    private String resolveMemberId(String prompt) {

        while (true) {

            if (currentMember != null) {

                System.out.print(
                        prompt
                        + "(blank = "
                        + currentMember.getMemberId()
                        + ") : "
                );

            } else {

                System.out.print(prompt);
            }

            String input
                    = scanner.nextLine().trim();

            if (isCancelKeyword(input)) {
                throw new ActionCancelledException();
            }

            // Use current member when input is blank.
            if (input.isBlank()) {

                if (currentMember != null) {
                    return currentMember.getMemberId();
                }

                System.out.println(
                        "Member ID cannot be empty."
                );

                continue;
            }

            // Check ID format.
            if (!input.matches("[A-Za-z0-9]+")) {

                System.out.println(
                        "Invalid format - use letters and numbers only, "
                        + "no spaces or symbols."
                );

                continue;
            }

            // Check whether the member actually exists.
            Member member
                    = loyaltyController.searchMemberById(input);

            if (member == null) {

                System.out.println(
                        "Member not found. Please enter a valid Member ID."
                );

                continue;
            }

            return input;
        }
    }

    // Refreshes the current member if it matches the ID.
    private void refreshCurrentMemberIfMatches(String memberId) {
        if (currentMember != null
                && currentMember.getMemberId().equals(memberId)) {

            Member updated = loyaltyController.searchMemberById(memberId);

            if (updated != null) {
                currentMember = updated;
            }
        }
    }

    // Displays a notice when the membership tier changes.
    private void printTierChangeNotice(Member member, String tierBefore, String tierAfter) {
        if (tierBefore.equals(tierAfter)) {
            return;
        }

        boolean isUpgrade = tierRank(tierAfter) > tierRank(tierBefore);

        System.out.println();
        if (isUpgrade) {
            System.out.println("==================================================");
            System.out.println("                CONGRATULATIONS!                  ");
            System.out.println("==================================================");
            System.out.println("Member        : " + member.getMemberId() + " - " + member.getName());
            System.out.println("Previous Tier : " + tierBefore);
            System.out.println("New Tier      : " + tierAfter);
            System.out.println();
            System.out.println("You have reached the " + tierAfter + " tier!");
            System.out.println("==================================================");
        } else {
            System.out.println("--------------------------------------------------");
            System.out.println("             MEMBERSHIP TIER UPDATED               ");
            System.out.println("--------------------------------------------------");
            System.out.println("Member        : " + member.getMemberId() + " - " + member.getName());
            System.out.println("Previous Tier : " + tierBefore);
            System.out.println("New Tier      : " + tierAfter);
            System.out.println("--------------------------------------------------");
        }
    }

    // Returns the ranking value of a tier.
    private int tierRank(String tier) {
        return switch (tier) {
            case "Gold" ->
                1;
            case "Elite" ->
                2;
            case "Diamond" ->
                3;
            case "Platinum" ->
                4;
            default ->
                0;
        };
    }

    // Reads and validates an ID.
    private String readValidId(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input.matches("[A-Za-z0-9]+")) {
                return input;
            }
            System.out.println("Invalid format - use letters and numbers only, no spaces or symbols.");
        }
    }

    // Reads and validates an existing member ID.
    private String readExistingMemberId(String prompt) {

        while (true) {

            String input = readString(prompt).trim();

            if (isCancelKeyword(input)) {
                throw new ActionCancelledException();
            }

            if (!input.matches("[A-Za-z0-9]+")) {

                System.out.println(
                        "Invalid format - use letters and numbers only, "
                        + "no spaces or symbols."
                );

                continue;
            }

            Member member
                    = loyaltyController.searchMemberById(input);

            if (member == null) {

                System.out.println(
                        "Member not found. Please enter a valid Member ID."
                );

                continue;
            }

            return input;
        }
    }

    // Reads and validates a name.
    private String readValidName(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input.matches("[A-Za-z ]+")) {
                return input;
            }
            System.out.println("Invalid format - letters and spaces only.");
        }
    }

    // Reads a positive integer.
    private int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("Value must be greater than 0.");
        }
    }

    // Reads a non-negative integer.
    private int readNonNegativeInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value >= 0) {
                return value;
            }
            System.out.println("Value cannot be negative.");
        }
    }

    // Reads an optional non-negative integer.
    private int readOptionalNonNegativeInt(String prompt) {
        while (true) {
            String input = readString(prompt);

            if (input.isBlank()) {
                return Integer.MAX_VALUE;
            }

            try {
                int value = Integer.parseInt(input);

                if (value < 0) {
                    System.out.println("Value cannot be negative.");
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println(
                        "Please enter a valid non-negative number or leave blank.");
            }
        }
    }

    // Reads an optional membership tier.
    private String readOptionalTier(String prompt) {
        String[] tiers = {
            "Silver",
            "Gold",
            "Elite",
            "Diamond",
            "Platinum"
        };

        while (true) {
            String input = readString(prompt);

            if (input.isBlank()) {
                return "";
            }

            for (String tier : tiers) {
                if (tier.equalsIgnoreCase(input)) {
                    return tier;
                }
            }

            System.out.println(
                    "Invalid tier. Choose Silver, Gold, Elite, Diamond, or Platinum.");
        }
    }

    // Reads a reward category choice.
    private String readCategoryChoice() {
        String[] categories = {"Dining", "Spa", "Room", "Others"};

        System.out.println("Select Category:");
        for (int i = 0; i < categories.length; i++) {
            System.out.println("  " + (i + 1) + ". " + categories[i]);
        }

        while (true) {
            int choice = readInt("Enter choice: ");
            if (choice >= 1 && choice <= categories.length) {
                return categories[choice - 1];
            }
            System.out.println("Invalid choice, please enter a number from 1 to " + categories.length + ".");
        }
    }

    // Reads and validates text input.
    private String readValidText(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input.matches("[A-Za-z0-9 ]+")) {
                return input;
            }
            System.out.println("Invalid format - letters, numbers, and spaces only.");
        }
    }

    // Reads an optional member ID.
    private String readOptionalValidId(String prompt) {
        while (true) {
            String input = readString(prompt);

            if (input.isBlank()) {
                return input;
            }

            if (input.equalsIgnoreCase("all")) {
                return "all";
            }

            if (input.matches("[A-Za-z0-9]+")) {
                return input;
            }

            System.out.println("Invalid format - enter a Member ID, 'all', or leave blank.");
        }
    }

    // Reads non-empty text input.
    private String readNonEmptyString(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (!input.isBlank()) {
                return input;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    // Processes expired points and displays the result.
    private void processExpiredPointsAndNotify() {
        ListInterface<ExpiredPointsResult> justExpired = loyaltyController.processExpiredPoints();

        if (currentMember != null) {
            refreshCurrentMemberIfMatches(currentMember.getMemberId());
        }

        if (justExpired.isEmpty()) {
            return;
        }

        System.out.println();
        System.out.println("[!] POINTS EXPIRED");
        for (int i = 1; i <= justExpired.size(); i++) {
            ExpiredPointsResult r = justExpired.getEntry(i);
            System.out.println("  " + r.memberId + " - " + r.expiredPoints + " points expired and were removed from the balance.");
        }
        System.out.println();
    }

    // Runs the loyalty and rewards menu.
    public void run() {

        processExpiredPointsAndNotify();

        boolean running = true;

        while (running) {

            printMainMenu();

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {

                    case "1":
                        memberDashboardMenu();
                        break;

                    case "2":
                        rewardsMenu();
                        break;

                    case "3":
                        loyaltyTiersMenu();
                        break;

                    case "4":
                        reportsManagementMenu();
                        break;

                    case "0":
                        running = false;
                        break;

                    default:
                        System.out.println("\nInvalid option. Please try again.\n");
                }
            } catch (ActionCancelledException e) {
                System.out.println("\nCancelled.\n");
            }
        }
    }

    // Displays the main loyalty menu.
    private void printMainMenu() {

        clearScreen();
        printCenteredBanner(BANNER_MAIN);
        printHeader("LOYALTY & REWARDS");

        if (currentMember != null) {
            System.out.println(memberSummaryLine(currentMember));
            System.out.println();
        }

        System.out.print(
                "                         1. Member Dashboard                                 \n"
                + "                         2. Rewards                                          \n"
                + "                         3. Loyalty & Tiers                                   \n"
                + "                         4. Reports & Management                              \n"
                + "                         0. Return to Main Menu                               \n"
        );
        printDivider();
        System.out.print("Enter your choice: ");
    }

    // Runs the member dashboard menu.
    private void memberDashboardMenu() {

        boolean running = true;

        while (running) {

            clearScreen();
            printCenteredBanner(BANNER_MEMBER);
            printHeader("MEMBER DASHBOARD");

            processExpiredPointsAndNotify();

            if (currentMember != null) {
                Member fresh = loyaltyController.searchMemberById(currentMember.getMemberId());
                if (fresh != null) {
                    setCurrentMember(fresh);
                }
                printDashboardDetails(currentMember);
            }

            System.out.print(
                    "                         1. Register New Member                              \n"
                    + "                         2. View / Select Member                           \n"
                    + "                         3. Earn Points                                       \n"
                    + "                         4. Transaction History                              \n"
                    + "                         5. Clear Current Member                              \n"
                    + "                         0. Back                                              \n"
            );
            printDivider();
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {

                    case "1":
                        registerLoyaltyMember();
                        break;

                    case "2":
                        viewAndSelectMember();
                        break;

                    case "3":
                        earnPoints();
                        break;

                    case "4":
                        transactionHistory();
                        break;

                    case "5":
                        clearCurrentMember();
                        break;
                    case "0":
                        running = false;
                        break;

                    default:
                        System.out.println("\nInvalid option. Please try again.\n");
                }
            } catch (ActionCancelledException e) {
                System.out.println("\nCancelled.\n");
            }
        }
    }

    // Clears the selected current member.
    private void clearCurrentMember() {

        clearScreen();
        printCenteredBanner(BANNER_MEMBER);
        printHeader("CLEAR CURRENT MEMBER");

        if (currentMember == null) {
            System.out.println();
            System.out.println("No current member is selected.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.println(
                "Current Member : "
                + memberSummaryLine(currentMember)
        );

        System.out.println();
        System.out.println(
                "This will remove the current member selection."
        );

        if (confirmAction("Clear current member?")) {

            String oldMemberId = currentMember.getMemberId();

            currentMember = null;

            System.out.println();
            System.out.println(
                    "[OK] Current member cleared."
            );
            System.out.println(
                    "Member " + oldMemberId + " is no longer selected."
            );

        } else {
            System.out.println();
            System.out.println("Current member unchanged.");
        }

        pressEnterToContinue();
    }

    // Handles earning points for a member.
// Handles earning points for a member.
    private void earnPoints() {

        clearScreen();
        printCenteredBanner(BANNER_MEMBER);
        printHeader("EARN REWARD POINTS");
        printCurrentMemberBanner();
        printBackHint();

        String id
                = resolveMemberId("Member ID                : ");

        int points
                = readPositiveInt("Points Earned            : ");

        Member before
                = loyaltyController.searchMemberById(id);

        if (before == null) {

            System.out.println();
            System.out.println("Member not found.");
            pressEnterToContinue();
            return;
        }

        String tierBefore = before.getTier();
        int previousPoints = before.getPoints();

        showLoading("Processing...");

        ControllerResult result
                = loyaltyController.earnPoints(id, points);

        if (!result.isOk()) {

            printResult(result);
            pressEnterToContinue();
            return;
        }

        Member after
                = loyaltyController.searchMemberById(id);

        int awarded
                = after.getPoints() - previousPoints;

        double multiplier
                = loyaltyController.getTierMultiplier(tierBefore);

        // Get tier progress information.
        String tierProgress
                = loyaltyController.getTierProgressDisplay(
                        after.getTier(),
                        after.getPoints()
                );

        String[] progressLines
                = tierProgress.split("\\R");

        String progressTo
                = progressLines.length > 0
                        ? progressLines[0]
                        : "-";

        String progressBar
                = progressLines.length > 2
                        ? progressLines[2]
                        : "";

        String pointsNeeded
                = progressLines.length > 3
                        ? progressLines[3]
                        : "";

        pointsNeeded
                = pointsNeeded
                        .replace("Need ", "")
                        .trim();

        // Get points expiry date.
        String expiry = "-";

        ListInterface<PointsTransaction> historyAfter
                = loyaltyController.getTransactionHistoryByMember(id);

        if (!historyAfter.isEmpty()) {

            PointsTransaction latest
                    = historyAfter.getEntry(
                            historyAfter.size()
                    );

            if (latest.getExpiryDate() != null) {

                expiry
                        = String.valueOf(
                                latest.getExpiryDate()
                        );
            }
        }

        // ================================
        // DISPLAY RESULT
        // ================================
        clearScreen();
        printCenteredBanner(BANNER_MEMBER);
        printHeader("POINTS EARNED SUCCESSFULLY");

        System.out.println();

        // ================================
        // POINTS CALCULATION
        // ================================
        printBox(
                "POINTS CALCULATION",
                new String[]{
                    String.format(
                            "%-16s : %s",
                            "Member",
                            after.getMemberId()
                            + " - "
                            + after.getName()
                    ),
                    String.format(
                            "%-16s : %s",
                            "Previous Balance",
                            formatPoints(previousPoints)
                            + " pts"
                    ),
                    String.format(
                            "%-16s : %s",
                            "Calculation",
                            formatPoints(points)
                            + " pts x "
                            + String.format(
                                    "%.2f",
                                    multiplier
                            )
                    ),
                    String.format(
                            "%-16s : %s",
                            "Points Awarded",
                            "+"
                            + formatPoints(awarded)
                            + " pts"
                    )
                }
        );

        System.out.println();

        // ================================
        // UPDATED MEMBER STATUS
        // ================================
        printBox(
                "UPDATED MEMBER STATUS",
                new String[]{
                    String.format(
                            "%-16s : %s",
                            "New Balance",
                            formatPoints(after.getPoints())
                            + " pts"
                    ),
                    String.format(
                            "%-16s : %s",
                            "Current Tier",
                            after.getTier()
                    ),
                    String.format(
                            "%-16s : %s",
                            "Tier Progress",
                            progressTo
                            + " ["
                            + progressBar
                            + "]"
                    ),
                    String.format(
                            "%-16s : %s",
                            "Points Needed",
                            pointsNeeded
                            + " more points"
                    ),
                    String.format(
                            "%-16s : %s",
                            "Points Expiry",
                            expiry
                    )
                }
        );

        // Display tier change notice if the tier changed.
        printTierChangeNotice(
                after,
                tierBefore,
                after.getTier()
        );

        refreshCurrentMemberIfMatches(id);

        pressEnterToContinue();
    }

    // Displays the current member dashboard details.
    private void printDashboardDetails(Member member) {

        System.out.println(
                "CURRENT MEMBER: "
                + memberSummaryLine(member)
        );

        System.out.println();
    }

    // Displays detailed member information.
    private void printMemberDetails(Member member) {

        int rank
                = loyaltyController.getMemberRank(
                        member.getMemberId()
                );

        int totalMembers
                = loyaltyController.getTotalMemberCount();

        clearScreen();
        printCenteredBanner(BANNER_MEMBER);
        printHeader("MEMBER DETAILS");

        // ==================================================
        // MEMBER INFORMATION
        // ==================================================
        System.out.println();
        System.out.println("MEMBER INFORMATION");
        printDivider();

        System.out.printf(
                "%-20s : %-45s%n",
                "Member ID",
                member.getMemberId()
        );

        System.out.printf(
                "%-20s : %-45s%n",
                "Name",
                member.getName()
        );

        System.out.printf(
                "%-20s : %-45s%n",
                "Membership Tier",
                member.getTier()
        );

        System.out.printf(
                "%-20s : %-45s%n",
                "Points Balance",
                formatPoints(member.getPoints()) + " pts"
        );

        System.out.printf(
                "%-20s : #%d of %d%n",
                "Member Ranking",
                rank,
                totalMembers
        );

        printDivider();

        // ==================================================
// TIER PROGRESS
// ==================================================
        System.out.println();
        System.out.println("TIER PROGRESS");
        printDivider();

        String progressDisplay
                = loyaltyController.getTierProgressDisplay(
                        member.getTier(),
                        member.getPoints()
                );

        String[] progressLines
                = progressDisplay.split("\\R");

        String progressTo
                = progressLines.length > 0
                        ? progressLines[0].trim()
                        : "";

        String progressCurrentTier
                = progressLines.length > 1
                        ? progressLines[1].trim()
                        : member.getTier();

        String progressText
                = progressLines.length > 2
                        ? progressLines[2].trim()
                        : "";

        String pointsNeeded
                = progressLines.length > 3
                        ? progressLines[3].trim()
                        : "";

// ==================================================
// HIGHEST TIER
// ==================================================
        boolean highestTier
                = progressTo.toLowerCase().contains(
                        "highest tier"
                )
                || member.getTier().equalsIgnoreCase("Platinum");

        if (highestTier) {

            System.out.printf(
                    "%-20s : %-45s%n",
                    "Current Tier",
                    member.getTier()
            );

            System.out.printf(
                    "%-20s : %-45s%n",
                    "Next Tier",
                    "Highest tier reached"
            );

            System.out.printf(
                    "%-20s : %-45s%n",
                    "Progress",
                    "Completed"
            );

            System.out.printf(
                    "%-20s : %-45s%n",
                    "Points Needed",
                    "No further points needed"
            );

        } else {

            // ==================================================
            // NORMAL TIER PROGRESS
            // ==================================================
            String progressBar = progressText;
            String percentage = "";

            int percentIndex
                    = progressText.lastIndexOf("%");

            if (percentIndex >= 0) {

                int percentStart
                        = progressText.lastIndexOf(" ");

                if (percentStart >= 0
                        && percentStart < percentIndex) {

                    percentage
                            = progressText.substring(
                                    percentStart + 1
                            ).trim();

                    progressBar
                            = progressText.substring(
                                    0,
                                    percentStart
                            ).trim();
                }
            }

            String nextTier
                    = progressTo
                            .replace(
                                    "Progress to ",
                                    ""
                            )
                            .trim();

            String needed
                    = pointsNeeded
                            .replace(
                                    "Need ",
                                    ""
                            )
                            .trim();

            System.out.printf(
                    "%-20s : %-45s%n",
                    "Current Tier",
                    progressCurrentTier
            );

            System.out.printf(
                    "%-20s : %-45s%n",
                    "Next Tier",
                    nextTier
            );

            System.out.printf(
                    "%-20s : [%-30s] %s%n",
                    "Progress",
                    progressBar,
                    percentage
            );

            System.out.printf(
                    "%-20s : %-45s%n",
                    "Points Needed",
                    needed
            );
        }

        printDivider();

        // ==================================================
        // RECENT ACTIVITY
        // ==================================================
        System.out.println();
        System.out.println("RECENT ACTIVITY");
        printDivider();

        RewardRedemption recent
                = loyaltyController
                        .getMostRecentRedemptionForMember(
                                member.getMemberId()
                        );

        if (recent == null) {

            System.out.printf(
                    "%-20s : %-45s%n",
                    "Last Redemption",
                    "No recent redemption"
            );

        } else {

            Reward reward
                    = loyaltyController.findRewardById(
                            recent.getRewardId()
                    );

            String rewardName
                    = reward != null
                            ? safeText(
                                    reward.getRewardName()
                            )
                            : recent.getRewardId();

            System.out.printf(
                    "%-20s : %-45s%n",
                    "Last Redemption",
                    rewardName
            );

            System.out.printf(
                    "%-20s : %-45s%n",
                    "Redemption Date",
                    String.valueOf(
                            recent.getRedeemDate()
                    )
            );
        }

        printDivider();

        // ==================================================
        // POINTS EXPIRY
        // ==================================================
        printMemberDetailsExpiry(member.getMemberId());

        pressEnterToContinue();
    }

    // Displays a compact points expiry box for Member Details only.
    private void printMemberDetailsExpiry(String memberId) {

        ListInterface<PointsTransaction> expiring
                = loyaltyController.getExpiringSoonTransactions(memberId);

        ListInterface<PointsTransaction> expired
                = loyaltyController.getRecentlyExpiredTransactions(memberId);

        System.out.println();

        // ==========================================
        // BUILD CONTENT FIRST
        // ==========================================
        java.util.ArrayList<String> lines
                = new java.util.ArrayList<>();

        // ---------- EXPIRING SOON ----------
        lines.add("EXPIRING SOON");

        if (expiring.isEmpty()) {

            lines.add(
                    "No points are currently expiring soon."
            );

        } else {

            lines.add(
                    String.format(
                            "%-18s | %12s | %-12s | %10s",
                            "Status",
                            "Points",
                            "Expires",
                            "Days Left"
                    )
            );

            for (int i = 1; i <= expiring.size(); i++) {

                PointsTransaction t
                        = expiring.getEntry(i);

                String points
                        = formatPoints(
                                Math.abs(
                                        t.getPointsChange()
                                )
                        ) + " pts";

                String expiryDate
                        = String.valueOf(
                                t.getExpiryDate()
                        );

                String daysLeft
                        = loyaltyController
                                .getDaysUntilExpiry(t)
                        + " days";

                lines.add(
                        String.format(
                                "%-18s | %12s | %-12s | %10s",
                                "Expiring Soon",
                                points,
                                expiryDate,
                                daysLeft
                        )
                );
            }
        }

        lines.add("");

        // ---------- RECENTLY EXPIRED ----------
        lines.add("RECENTLY EXPIRED");

        if (expired.isEmpty()) {

            lines.add(
                    "No points have recently expired."
            );

        } else {

            lines.add(
                    String.format(
                            "%-18s | %12s | %-12s",
                            "Status",
                            "Points",
                            "Expired On"
                    )
            );

            for (int i = 1; i <= expired.size(); i++) {

                PointsTransaction t
                        = expired.getEntry(i);

                String points
                        = "-"
                        + formatPoints(
                                Math.abs(
                                        t.getPointsChange()
                                )
                        )
                        + " pts";

                String expiredDate
                        = String.valueOf(
                                t.getTransactionDate()
                        );

                lines.add(
                        String.format(
                                "%-18s | %12s | %-12s",
                                "Points Expired",
                                points,
                                expiredDate
                        )
                );
            }
        }

        // ==========================================
        // CALCULATE EXACT BOX WIDTH
        // ==========================================
        int contentWidth = "POINTS EXPIRY".length();

        for (String line : lines) {
            contentWidth = Math.max(
                    contentWidth,
                    line.length()
            );
        }

        int boxWidth = contentWidth + 4;

        // ==========================================
        // PRINT BOX
        // ==========================================
        System.out.println(
                "+" + "-".repeat(boxWidth - 2) + "+"
        );

        String title = "POINTS EXPIRY";

        int leftPadding
                = (contentWidth - title.length()) / 2;

        int rightPadding
                = contentWidth
                - title.length()
                - leftPadding;

        System.out.println(
                "| "
                + " ".repeat(leftPadding)
                + title
                + " ".repeat(rightPadding)
                + " |"
        );

        System.out.println(
                "+" + "-".repeat(boxWidth - 2) + "+"
        );

        for (String line : lines) {

            System.out.printf(
                    "| %-"
                    + contentWidth
                    + "s |%n",
                    line
            );
        }

        System.out.println(
                "+" + "-".repeat(boxWidth - 2) + "+"
        );
    }

    // Displays expiring and recently expired points.
    private void printExpiryAlerts(String memberId) {

        ListInterface<PointsTransaction> expiring
                = loyaltyController.getExpiringSoonTransactions(memberId);

        ListInterface<PointsTransaction> expired
                = loyaltyController.getRecentlyExpiredTransactions(memberId);

        boolean showMember = memberId == null;

        // ==================================================
        // POINTS EXPIRY
        // ==================================================
        System.out.println();
        System.out.println("POINTS EXPIRY");
        printDivider();

        // ==================================================
        // EXPIRING SOON
        // ==================================================
        System.out.println();
        System.out.println("EXPIRING SOON");
        printDivider();

        if (expiring.isEmpty()) {

            System.out.println(
                    "No points are currently expiring soon."
            );

        } else {

            if (showMember) {

                System.out.printf(
                        "| %-8s | %-18s | %12s | %-12s | %10s |%n",
                        "Member",
                        "Status",
                        "Points",
                        "Expires",
                        "Days Left"
                );

                printDivider();

                for (int i = 1; i <= expiring.size(); i++) {

                    PointsTransaction t
                            = expiring.getEntry(i);

                    String points
                            = formatPoints(
                                    Math.abs(
                                            t.getPointsChange()
                                    )
                            ) + " pts";

                    String expiryDate
                            = String.valueOf(
                                    t.getExpiryDate()
                            );

                    String daysLeft
                            = loyaltyController
                                    .getDaysUntilExpiry(t)
                            + " days";

                    System.out.printf(
                            "| %-8s | %-18s | %12s | %-12s | %10s |%n",
                            t.getMemberId(),
                            "Expiring Soon",
                            points,
                            expiryDate,
                            daysLeft
                    );
                }

            } else {

                System.out.printf(
                        "| %-18s | %12s | %-12s | %10s |%n",
                        "Status",
                        "Points",
                        "Expires",
                        "Days Left"
                );

                printDivider();

                for (int i = 1; i <= expiring.size(); i++) {

                    PointsTransaction t
                            = expiring.getEntry(i);

                    String points
                            = formatPoints(
                                    Math.abs(
                                            t.getPointsChange()
                                    )
                            ) + " pts";

                    String expiryDate
                            = String.valueOf(
                                    t.getExpiryDate()
                            );

                    String daysLeft
                            = loyaltyController
                                    .getDaysUntilExpiry(t)
                            + " days";

                    System.out.printf(
                            "| %-18s | %12s | %-12s | %10s |%n",
                            "Expiring Soon",
                            points,
                            expiryDate,
                            daysLeft
                    );
                }
            }

            printDivider();
        }

        // ==================================================
        // RECENTLY EXPIRED
        // ==================================================
        System.out.println();
        System.out.println("RECENTLY EXPIRED");
        printDivider();

        if (expired.isEmpty()) {

            System.out.println(
                    "No points have recently expired."
            );

        } else {

            if (showMember) {

                System.out.printf(
                        "| %-8s | %-18s | %12s | %-12s |%n",
                        "Member",
                        "Status",
                        "Points",
                        "Expired On"
                );

                printDivider();

                for (int i = 1; i <= expired.size(); i++) {

                    PointsTransaction t
                            = expired.getEntry(i);

                    String points
                            = "-"
                            + formatPoints(
                                    Math.abs(
                                            t.getPointsChange()
                                    )
                            )
                            + " pts";

                    String expiredDate
                            = String.valueOf(
                                    t.getTransactionDate()
                            );

                    System.out.printf(
                            "| %-8s | %-18s | %12s | %-12s |%n",
                            t.getMemberId(),
                            "Points Expired",
                            points,
                            expiredDate
                    );
                }

            } else {

                System.out.printf(
                        "| %-18s | %12s | %-12s |%n",
                        "Status",
                        "Points",
                        "Expired On"
                );

                printDivider();

                for (int i = 1; i <= expired.size(); i++) {

                    PointsTransaction t
                            = expired.getEntry(i);

                    String points
                            = "-"
                            + formatPoints(
                                    Math.abs(
                                            t.getPointsChange()
                                    )
                            )
                            + " pts";

                    String expiredDate
                            = String.valueOf(
                                    t.getTransactionDate()
                            );

                    System.out.printf(
                            "| %-18s | %12s | %-12s |%n",
                            "Points Expired",
                            points,
                            expiredDate
                    );
                }
            }

            printDivider();
        }

        // ==================================================
        // OVERALL STATUS
        // ==================================================
        if (expiring.isEmpty() && expired.isEmpty()) {

            System.out.println();
            System.out.println(
                    "[OK] No points expiry records found."
            );
        }
    }

    // Displays and selects a member.
    private void viewAndSelectMember() {

        clearScreen();
        printCenteredBanner(BANNER_MEMBER);
        printHeader("VIEW / SELECT MEMBER");

        ListInterface<Member> members
                = loyaltyController.viewAllMembers();

        if (members.isEmpty()) {

            System.out.println();
            System.out.println("[!] No members registered yet.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.println("ALL MEMBERS");
        printDivider();

        printMemberTable(members, false);

        printDivider();

        System.out.println();
        System.out.println(
                "Enter a Member ID or Name to view details."
        );
        System.out.println(
                "Type back to return."
        );

        System.out.println();

        String input;

        while (true) {

            input = readString("Member ID or Name: ");

            if (input.equals("0")) {
                return;
            }

            if (!input.isBlank()) {
                break;
            }

            System.out.println(
                    "Member ID or Name cannot be empty."
            );
        }

        Member found
                = loyaltyController.searchMemberById(input);

        if (found == null) {

            ListInterface<Member> matches
                    = loyaltyController.searchMemberByName(input);

            if (matches.isEmpty()) {

                System.out.println();
                System.out.println(
                        "[!] No member found matching: "
                        + input
                );

                pressEnterToContinue();
                return;
            }

            if (matches.size() == 1) {

                found = matches.getEntry(1);

            } else {

                clearScreen();
                printCenteredBanner(BANNER_MEMBER);
                printHeader("SELECT MEMBER");

                System.out.println(
                        "Multiple members found:"
                );

                System.out.println();

                printMemberTable(matches, false);

                printDivider();

                String selectedId
                        = readExistingMemberId(
                                "Enter Member ID to select: "
                        );

                found
                        = loyaltyController.searchMemberById(
                                selectedId
                        );
            }
        }

        printMemberDetails(found);

        System.out.println();
        System.out.println(
                "Set this member as the current member?"
        );

        boolean useAsCurrent
                = readYesNoWithDefaultYes(
                        "Set "
                        + found.getMemberId()
                        + " as the current member? (Y/N): "
                );

        if (useAsCurrent) {

            setCurrentMember(found);

            System.out.println();
            System.out.println(
                    "[OK] Current member set to "
                    + found.getMemberId()
                    + "."
            );

        } else {

            System.out.println();
            System.out.println(
                    "Current member unchanged."
            );
        }

        pressEnterToContinue();
    }

    // Displays the transaction history options.
    private void transactionHistory() {

        clearScreen();
        printCenteredBanner(BANNER_MEMBER);
        printHeader("TRANSACTION HISTORY");

        System.out.println();
        System.out.println("1. Select Member");
        System.out.println("2. Current Member");
        System.out.println("3. View All Transactions");
        System.out.println("0. Back");

        printDivider();

        int choice = readInt("Enter choice: ");

        String memberId = null;

        switch (choice) {

            case 1:
                memberId = selectMemberForTransactionHistory();
                if (memberId == null) {
                    return;
                }
                break;

            case 2:

                if (currentMember == null) {
                    System.out.println();
                    System.out.println(
                            "No current member selected."
                    );
                    pressEnterToContinue();
                    return;
                }

                memberId = currentMember.getMemberId();
                break;

            case 3:
                showAllTransactionHistory();
                return;

            case 0:
                return;

            default:
                System.out.println();
                System.out.println("Invalid option.");
                pressEnterToContinue();
                return;
        }

        showMemberTransactionHistory(memberId);
    }

    // Selects a member for transaction history.
    private String selectMemberForTransactionHistory() {

        clearScreen();
        printCenteredBanner(BANNER_MEMBER);
        printHeader("SELECT MEMBER");

        ListInterface<Member> members
                = loyaltyController.viewAllMembers();

        if (members.isEmpty()) {
            System.out.println();
            System.out.println("No members found.");
            pressEnterToContinue();
            return null;
        }

        printMemberTable(members, false);

        System.out.println();
        printDivider();

        String input
                = readString(
                        "Enter Member ID or Name: "
                ).trim();

        Member found
                = loyaltyController.searchMemberById(input);

        if (found == null) {

            ListInterface<Member> matches
                    = loyaltyController.searchMemberByName(input);

            if (matches.isEmpty()) {
                System.out.println();
                System.out.println("No matching member found.");
                pressEnterToContinue();
                return null;
            }

            if (matches.size() == 1) {

                found = matches.getEntry(1);

            } else {

                clearScreen();
                printCenteredBanner(BANNER_MEMBER);
                printHeader("SELECT MEMBER");

                System.out.println(
                        "Multiple members found:"
                );

                printDivider();

                printMemberTable(matches, false);

                printDivider();

                String selectedId
                        = readExistingMemberId(
                                "Enter Member ID to select: "
                        );

                found
                        = loyaltyController.searchMemberById(
                                selectedId
                        );
            }
        }

        return found.getMemberId();
    }

    // Displays transactions for one member.
    private void showMemberTransactionHistory(String memberId) {

        Member member
                = loyaltyController.searchMemberById(memberId);

        if (member == null) {
            System.out.println();
            System.out.println("Member not found.");
            pressEnterToContinue();
            return;
        }

        ListInterface<PointsTransaction> history
                = loyaltyController.getTransactionHistoryByMember(
                        memberId
                );

        clearScreen();
        printCenteredBanner(BANNER_MEMBER);

        int tableWidth = getTransactionTableWidth(history, false);
        printTransactionHeader("MEMBER TRANSACTION HISTORY", tableWidth);
        System.out.println("CURRENT MEMBER: " + memberSummaryLine(member));
        System.out.println();

        if (history.isEmpty()) {
            System.out.println();
            System.out.println(
                    "No transactions found for this member."
            );
        } else {
            printTransactionTable(history, false);
        }

        pressEnterToContinue();
    }

    // Displays all point transactions.
    private void showAllTransactionHistory() {

        ListInterface<PointsTransaction> all
                = loyaltyController.getAllTransactions();

        clearScreen();
        printCenteredBanner(BANNER_MEMBER);

        int tableWidth = getTransactionTableWidth(all, true);
        printTransactionHeader("ALL TRANSACTIONS", tableWidth);
        System.out.println("Total Records: " + all.size());
        System.out.println();

        if (all.isEmpty()) {
            System.out.println();
            System.out.println("No transactions found.");
        } else {
            printTransactionTable(all, true);
        }

        pressEnterToContinue();
    }

    // Calculates the transaction description column width.
    private int getTransactionDescriptionWidth(
            ListInterface<PointsTransaction> transactions) {

        int width = "Description".length();
        for (int i = 1; i <= transactions.size(); i++) {
            String description = transactions.getEntry(i).getNote();
            if (description == null || description.isBlank()) {
                description = "-";
            }
            description = description.replace("\r", " ").replace("\n", " ").trim();
            width = Math.max(width, description.length());
        }
        return width;
    }

    private int getTransactionPointsWidth(
            ListInterface<PointsTransaction> transactions) {

        int width = "Points".length();

        for (int i = 1; i <= transactions.size(); i++) {
            PointsTransaction t = transactions.getEntry(i);

            String pointsStr = (t.getPointsChange() >= 0 ? "+" : "")
                    + formatPoints(t.getPointsChange());

            width = Math.max(width, pointsStr.length());
        }

        return width;
    }

    // Calculates the transaction table width.
    private int getTransactionTableWidth(
            ListInterface<PointsTransaction> transactions, boolean showMember) {

        int descriptionWidth = getTransactionDescriptionWidth(transactions);
        int pointsWidth = getTransactionPointsWidth(transactions);

        int width = 4 + 7 + 3 + 10 + 3 + 8 + 3
                + pointsWidth + 3
                + descriptionWidth + 3 + 8;

        if (showMember) {
            width += 3 + 6;
        }

        return width;
    }

    // Displays the transaction table header.
    private void printTransactionHeader(String title, int tableWidth) {
        System.out.println();
        System.out.println("-".repeat(tableWidth));
        int padding = Math.max(0, (tableWidth - title.length()) / 2);
        System.out.println(" ".repeat(padding) + title);
        System.out.println("-".repeat(tableWidth));
    }

    // Displays the transaction records in a table.
    private void printTransactionTable(
            ListInterface<PointsTransaction> transactions,
            boolean showMember) {

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        DateTimeFormatter dateFormat
                = DateTimeFormatter.ofPattern("dd/MM/yy");

        final int txnWidth = 7;
        final int memberWidth = 6;
        final int dateWidth = 10;
        final int typeWidth = 8;
        final int pointsWidth = getTransactionPointsWidth(transactions);
        final int expiryWidth = 8;
        final int descriptionWidth = getTransactionDescriptionWidth(transactions);
        final int tableWidth = getTransactionTableWidth(transactions, showMember);

        System.out.println("-".repeat(tableWidth));

        if (showMember) {
            System.out.printf(
                    "| %-" + txnWidth + "s | %-" + memberWidth + "s | %-" + dateWidth
                    + "s | %-" + typeWidth + "s | %" + pointsWidth + "s | %-"
                    + descriptionWidth + "s | %-" + expiryWidth + "s |%n",
                    "TXN ID", "Member", "Date", "Type", "Points", "Description", "Expiry");
        } else {
            System.out.printf(
                    "| %-" + txnWidth + "s | %-" + dateWidth + "s | %-" + typeWidth
                    + "s | %" + pointsWidth + "s | %-" + descriptionWidth + "s | %-"
                    + expiryWidth + "s |%n",
                    "TXN ID", "Date", "Type", "Points", "Description", "Expiry");
        }

        System.out.println("-".repeat(tableWidth));

        for (int i = 1; i <= transactions.size(); i++) {
            PointsTransaction t = transactions.getEntry(i);

            String pointsStr = (t.getPointsChange() >= 0 ? "+" : "")
                    + formatPoints(t.getPointsChange());
            String expiryStr = t.getExpiryDate() != null
                    ? t.getExpiryDate().format(dateFormat)
                    : "-";
            String description = t.getNote();
            if (description == null || description.isBlank()) {
                description = "-";
            }
            description = description.replace("\r", " ").replace("\n", " ").trim();

            if (showMember) {
                System.out.printf(
                        "| %-" + txnWidth + "s | %-" + memberWidth + "s | %-" + dateWidth
                        + "s | %-" + typeWidth + "s | %" + pointsWidth + "s | %-"
                        + descriptionWidth + "s | %-" + expiryWidth + "s |%n",
                        t.getTransactionId(), t.getMemberId(),
                        t.getTransactionDate().format(dateFormat), t.getType(),
                        pointsStr, description, expiryStr);
            } else {
                System.out.printf(
                        "| %-" + txnWidth + "s | %-" + dateWidth + "s | %-" + typeWidth
                        + "s | %" + pointsWidth + "s | %-" + descriptionWidth + "s | %-"
                        + expiryWidth + "s |%n",
                        t.getTransactionId(), t.getTransactionDate().format(dateFormat),
                        t.getType(), pointsStr, description, expiryStr);
            }
        }

        System.out.println("-".repeat(tableWidth));
    }

    // Registers a new loyalty member.
    private void registerLoyaltyMember() {

        clearScreen();
        printCenteredBanner(BANNER_REGISTER);
        printHeader("REGISTER NEW MEMBER");
        printBackHint();

        String id = loyaltyController.generateNextMemberId();

        System.out.println("Assigned Member ID       : " + id);

        String name = readValidName("Member Name              : ");
        int points = readNonNegativeInt("Starting Points          : ");

        showLoading("Registering member...");

        ControllerResult result
                = loyaltyController.registerMember(id, name, points);

        if (!result.isOk()) {
            printResult(result);
            pressEnterToContinue();
            return;
        }

        Member registered
                = loyaltyController.searchMemberById(id);

        System.out.println();
        System.out.println("[OK] MEMBER REGISTERED SUCCESSFULLY");
        System.out.println();

        System.out.printf(
                "%-12s %-25s %-15s %-12s%n",
                "Member ID",
                "Name",
                "Tier",
                "Points"
        );

        printDivider();

        System.out.printf(
                "%-12s %-25s %-15s %-12s%n",
                registered.getMemberId(),
                registered.getName(),
                registered.getTier(),
                formatPoints(registered.getPoints()) + " pts"
        );

        printDivider();

        System.out.println();

        boolean useAsCurrent = readYesNoWithDefaultYes(
                "Use " + registered.getMemberId()
                + " as the current member? (Y/N): "
        );

        if (useAsCurrent) {
            setCurrentMember(registered);
            System.out.println(
                    "Current member set to "
                    + registered.getMemberId()
                    + "."
            );
        } else {
            System.out.println("Current member unchanged.");
        }

        pressEnterToContinue();
    }

    // Displays all loyalty members.
    private void viewAllMembers() {

        clearScreen();
        printCenteredBanner(BANNER_MEMBER);

        ListInterface<Member> members = loyaltyController.viewAllMembers();

        printHeader("ALL MEMBERS");
        printMemberTable(members, false);

        pressEnterToContinue();
    }

    // Displays members in a table.
    private void printMemberTable(ListInterface<Member> members, boolean showRank) {
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        if (showRank) {
            System.out.printf("%-5s %-10s %-25s %-15s %-10s%n", "Rank", "ID", "Name", "Tier", "Points");
        } else {
            System.out.printf("%-10s %-25s %-15s %-10s%n", "ID", "Name", "Tier", "Points");
        }
        printDivider();

        for (int i = 1; i <= members.size(); i++) {
            Member m = members.getEntry(i);
            if (showRank) {
                System.out.printf("%-5d %-10s %-25s %-15s %-10d%n",
                        i, m.getMemberId(), m.getName(), m.getTier(), m.getPoints());
            } else {
                System.out.printf("%-10s %-25s %-15s %-10d%n",
                        m.getMemberId(), m.getName(), m.getTier(), m.getPoints());
            }
        }
    }

    // Runs the rewards menu.
    private void rewardsMenu() {

        boolean running = true;

        while (running) {

            clearScreen();
            printCenteredBanner(BANNER_REWARDS);
            printHeader("REWARDS");

            printCurrentMemberBanner();

            System.out.print(
                    "                         1. Browse / Search / Sort Rewards                   \n"
                    + "                         2. Redeem Reward                                     \n"
                    + "                         3. Redemption History                               \n"
                    + "                         4. Undo Most Recent Redemption                       \n"
                    + "                         5. Add Reward                                        \n"
                    + "                         6. Restock Reward                                    \n"
                    + "                         0. Back                                              \n"
            );
            printDivider();
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {

                    case "1":
                        browseSearchSortRewards();
                        break;

                    case "2":
                        redeemReward();
                        break;

                    case "3":
                        viewRedemptionHistory();
                        break;

                    case "4":
                        undoLastRedemption();
                        break;

                    case "5":
                        addReward();
                        break;

                    case "6":
                        restockReward();
                        break;

                    case "0":
                        running = false;
                        break;

                    default:
                        System.out.println("\nInvalid option. Please try again.\n");
                }
            } catch (ActionCancelledException e) {
                System.out.println("\nCancelled.\n");
            }
        }
    }

    // Displays rewards in a dynamically sized table.
    private void printRewardTable(ListInterface<Reward> rewards) {

        if (rewards.isEmpty()) {
            System.out.println("No rewards found.");
            return;
        }

        int idWidth = "ID".length();
        int rewardWidth = "Reward".length();
        int categoryWidth = "Category".length();
        int pointsWidth = "Points Required".length();
        int qtyWidth = "Qty".length();
        int statusWidth = "Status".length();

        for (int i = 1; i <= rewards.size(); i++) {

            Reward r = rewards.getEntry(i);

            String rewardName = safeText(r.getRewardName());

            String status;

            if (r.getQuantity() == 0) {
                status = "OUT";
            } else if (r.getQuantity()
                    <= loyaltyController.getLowStockThreshold()) {
                status = "LOW";
            } else {
                status = "OK";
            }

            idWidth = Math.max(
                    idWidth,
                    textLength(r.getRewardId())
            );

            rewardWidth = Math.max(
                    rewardWidth,
                    rewardName.length()
            );

            categoryWidth = Math.max(
                    categoryWidth,
                    textLength(r.getCategory())
            );

            pointsWidth = Math.max(
                    pointsWidth,
                    formatPoints(r.getPointsRequired()).length()
            );

            qtyWidth = Math.max(
                    qtyWidth,
                    String.valueOf(r.getQuantity()).length()
            );

            statusWidth = Math.max(
                    statusWidth,
                    status.length()
            );
        }

        int tableWidth = idWidth
                + rewardWidth
                + categoryWidth
                + pointsWidth
                + qtyWidth
                + statusWidth
                + (6 * 3)
                + 2;

        System.out.println("-".repeat(tableWidth));

        printDynamicRow(
                new String[]{
                    "ID",
                    "Reward",
                    "Category",
                    "Points Required",
                    "Qty",
                    "Status"
                },
                new int[]{
                    idWidth,
                    rewardWidth,
                    categoryWidth,
                    pointsWidth,
                    qtyWidth,
                    statusWidth
                }
        );

        System.out.println("-".repeat(tableWidth));

        for (int i = 1; i <= rewards.size(); i++) {

            Reward r = rewards.getEntry(i);

            String status;

            if (r.getQuantity() == 0) {
                status = "OUT";
            } else if (r.getQuantity()
                    <= loyaltyController.getLowStockThreshold()) {
                status = "LOW";
            } else {
                status = "OK";
            }

            printDynamicRow(
                    new String[]{
                        r.getRewardId(),
                        r.getRewardName(),
                        r.getCategory(),
                        formatPoints(r.getPointsRequired()),
                        String.valueOf(r.getQuantity()),
                        status
                    },
                    new int[]{
                        idWidth,
                        rewardWidth,
                        categoryWidth,
                        pointsWidth,
                        qtyWidth,
                        statusWidth
                    }
            );
        }

        System.out.println("-".repeat(tableWidth));
    }

    // Browses, searches, and sorts rewards.
    private void browseSearchSortRewards() {

        ListInterface<Reward> currentView
                = loyaltyController.viewRewards();

        boolean browsing = true;

        while (browsing) {

            clearScreen();
            printCenteredBanner(BANNER_REWARDS);
            printHeader("BROWSE REWARDS");

            printCurrentMemberBanner();

            System.out.println();
            printRewardTable(currentView);

            System.out.println();
            printDivider();

            System.out.print(
                    "  1. Search   2. Points    3. Name A-Z   4. Stock    5. Reset   0. Back\n"
            );

            printDivider();
            System.out.print("Enter your choice: ");

            String choice
                    = scanner.nextLine().trim();

            switch (choice) {

                case "1": {

                    String keyword
                            = readNonEmptyString(
                                    "Enter keyword: "
                            );

                    showLoading("Searching...");

                    currentView
                            = loyaltyController
                                    .searchRewardsByKeyword(keyword);

                    break;
                }

                case "2":

                    currentView
                            = applySortChoice("price");

                    break;

                case "3":

                    currentView
                            = applySortChoice("name");

                    break;

                case "4":

                    currentView
                            = applySortChoice("stock");

                    break;

                case "5":

                    currentView
                            = loyaltyController.viewRewards();

                    break;

                case "0":

                    browsing = false;

                    break;

                default:

                    System.out.println();
                    System.out.println(
                            "Invalid option. Please try again."
                    );

                    pressEnterToContinue();
            }
        }
    }

    // Applies the selected reward sorting option.
    private ListInterface<Reward> applySortChoice(String sortBy) {

        showLoading("Sorting...");
        return loyaltyController.sortRewards(sortBy);
    }

    // Handles reward redemption.
    private void redeemReward() {

        clearScreen();
        printCenteredBanner(BANNER_REWARDS);
        printHeader("REDEEM REWARD");

        printCurrentMemberBanner();
        printBackHint();

        System.out.println("Available Rewards:\n");
        printRewardTable(loyaltyController.viewRewards());

        System.out.println();

        String memberId = resolveMemberId(
                "Member ID                : "
        );

        String rewardId = readValidId(
                "Reward ID                : "
        );

        Member member = loyaltyController.searchMemberById(memberId);

        if (member == null) {
            System.out.println("\nMember not found.");
            pressEnterToContinue();
            return;
        }

        Reward reward = loyaltyController.findRewardById(rewardId);

        if (reward == null) {
            System.out.println("\nReward not found.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.println("Member       : " + member.getName() + " (" + formatPoints(member.getPoints()) + " pts)");
        System.out.println("Reward       : " + reward.getRewardName()
                + " (" + reward.getPointsRequired() + " pts required)");
        System.out.println("Stock Left   : " + reward.getQuantity());

        if (reward.getQuantity() <= 0) {
            System.out.println("\nThis reward is out of stock and cannot be redeemed.");
            pressEnterToContinue();
            return;
        }

        if (member.getPoints() < reward.getPointsRequired()) {
            int shortfall = reward.getPointsRequired() - member.getPoints();
            System.out.println("\nInsufficient points - " + shortfall + " more point"
                    + (shortfall == 1 ? "" : "s") + " needed.");
            pressEnterToContinue();
            return;
        }

        if (!confirmAction("\nConfirm this redemption?")) {
            System.out.println("Redemption cancelled.");
            pressEnterToContinue();
            return;
        }

        String tierBefore = member.getTier();
        String redemptionId = loyaltyController.generateNextRedemptionId();

        showLoading("Processing redemption...");

        ControllerResult result = loyaltyController.redeemReward(redemptionId, memberId, rewardId);

        printResult(result);

        if (result.isOk()) {
            Member updatedMember = loyaltyController.searchMemberById(memberId);
            Reward updatedReward = loyaltyController.findRewardById(rewardId);

            printRedemptionReceipt(redemptionId, member, updatedMember, updatedReward);
            printTierChangeNotice(updatedMember, tierBefore, updatedMember.getTier());

            refreshCurrentMemberIfMatches(memberId);
        }

        pressEnterToContinue();
    }

    // Displays the redemption receipt in a dynamically sized box.
    private void printRedemptionReceipt(
            String redemptionId,
            Member memberBeforeRedemption,
            Member updatedMember,
            Reward updatedReward) {

        int remainingPoints
                = updatedMember != null
                        ? updatedMember.getPoints()
                        : 0;

        int remainingStock
                = updatedReward != null
                        ? updatedReward.getQuantity()
                        : 0;

        String rewardName
                = updatedReward != null
                        ? safeText(updatedReward.getRewardName())
                        : "-";

        int pointsUsed
                = updatedReward != null
                        ? updatedReward.getPointsRequired()
                        : 0;

        String title = "REDEMPTION SUCCESSFUL";

        String[] lines = {
            "Receipt No       : " + redemptionId,
            "Member           : " + memberBeforeRedemption.getName(),
            "Reward           : " + rewardName,
            "Points Used      : " + formatPoints(pointsUsed),
            "Remaining Points : " + formatPoints(remainingPoints),
            "Remaining Stock  : " + remainingStock
        };

        int contentWidth = title.length();

        for (String line : lines) {
            contentWidth = Math.max(
                    contentWidth,
                    line.length()
            );
        }

        int boxWidth = contentWidth + 4;

        System.out.println();

        System.out.println("-".repeat(boxWidth));

        int padding
                = Math.max(
                        0,
                        (contentWidth - title.length()) / 2
                );

        System.out.printf(
                "| %-" + contentWidth + "s |%n",
                " ".repeat(padding) + title
        );

        System.out.println("-".repeat(boxWidth));

        for (String line : lines) {

            System.out.printf(
                    "| %-" + contentWidth + "s |%n",
                    line
            );
        }

        System.out.printf(
                "| %-" + contentWidth + "s |%n",
                "Thank You!"
        );

        System.out.println("-".repeat(boxWidth));
    }

    // Undoes the latest reward redemption.
    private void undoLastRedemption() {

        clearScreen();
        printCenteredBanner(BANNER_REWARDS);
        printHeader("UNDO MOST RECENT REDEMPTION");

        RewardRedemption last = loyaltyController.getLastRedemption();
        if (last == null) {
            System.out.println("\nNo redemptions to undo.");
            pressEnterToContinue();
            return;
        }

        Reward lastReward = loyaltyController.findRewardById(last.getRewardId());
        Member lastMember = loyaltyController.searchMemberById(last.getMemberId());

        System.out.println();
        System.out.println("MOST RECENT REDEMPTION");
        printDivider();

        System.out.printf("%-18s : %s%n",
                "Redemption ID", last.getRedemptionId());

        System.out.printf("%-18s : %s%n",
                "Member",
                lastMember != null
                        ? lastMember.getMemberId() + " - " + lastMember.getName()
                        : last.getMemberId());

        System.out.printf("%-18s : %s%n",
                "Reward",
                lastReward != null
                        ? lastReward.getRewardName()
                        : last.getRewardId());

        System.out.printf("%-18s : %s%n",
                "Points Used",
                formatPoints(last.getRedeemedPoints()));

        System.out.printf("%-18s : %s%n",
                "Redeemed Date",
                last.getRedeemDate());

        printDivider();

        if (!confirmAction("\nUndo this redemption and refund the points?")) {
            System.out.println("Undo cancelled.");
            pressEnterToContinue();
            return;
        }

        Member memberBefore = loyaltyController.searchMemberById(last.getMemberId());
        String tierBefore = (memberBefore != null) ? memberBefore.getTier() : null;

        showLoading("Reversing redemption...");
        ControllerResult result = loyaltyController.undoLastRedemption();

        printResult(result);

        if (result.isOk()) {
            Member memberAfter = loyaltyController.searchMemberById(last.getMemberId());
            Reward rewardAfter = loyaltyController.findRewardById(last.getRewardId());

            System.out.println();

            String redemptionId = last.getRedemptionId();
            String refundedPoints = formatPoints(last.getRedeemedPoints());
            String newBalance = memberAfter != null
                    ? formatPoints(memberAfter.getPoints())
                    : "-";
            String stockRestored = rewardAfter != null
                    ? String.valueOf(rewardAfter.getQuantity())
                    : "-";

            String[] undoDetails = {
                "Redemption ID   : " + redemptionId,
                "Member          : "
                + (lastMember != null
                ? lastMember.getMemberId()
                + " - "
                + lastMember.getName()
                : last.getMemberId()),
                "Reward          : "
                + (rewardAfter != null
                ? rewardAfter.getRewardName()
                : last.getRewardId()),
                "Refunded Points : " + refundedPoints,
                "New Balance     : " + newBalance,
                "Stock Restored  : " + stockRestored
            };

            int contentWidth = 0;

            for (String line : undoDetails) {
                contentWidth = Math.max(contentWidth, line.length());
            }

            int boxWidth = contentWidth + 4;

            System.out.println("-".repeat(boxWidth));
            System.out.printf("| %-" + contentWidth + "s |%n", "REDEMPTION UNDONE SUCCESSFULLY");

            for (String line : undoDetails) {
                System.out.printf("| %-" + contentWidth + "s |%n", line);
            }

            System.out.println("-".repeat(boxWidth));

            if (memberAfter != null && tierBefore != null) {
                printTierChangeNotice(memberAfter, tierBefore, memberAfter.getTier());
            }

            refreshCurrentMemberIfMatches(last.getMemberId());
        }

        pressEnterToContinue();
    }

    // Displays reward redemption history.
    private void viewRedemptionHistory() {

        clearScreen();
        printCenteredBanner(BANNER_REWARDS);
        printHeader("REDEMPTION HISTORY");
        printCurrentMemberBanner();
        printBackHint();

        System.out.println();
        System.out.println("View redemption history for:");

        if (currentMember != null) {
            System.out.println(
                    "  1. Current Member (" + currentMember.getMemberId() + ")"
            );
        } else {
            System.out.println("  1. Current Member (not selected)");
        }

        System.out.println("  2. Enter Member ID");
        System.out.println("  3. All Members");
        System.out.println("  0. Back");

        printDivider();

        int choice = readInt("Enter choice: ");

        String memberId;

        switch (choice) {

            case 1:

                if (currentMember == null) {
                    System.out.println();
                    System.out.println("No current member selected.");
                    pressEnterToContinue();
                    return;
                }

                memberId = currentMember.getMemberId();
                break;

            case 2:

                memberId
                        = readExistingMemberId(
                                "Enter Member ID: "
                        );

                break;

            case 3:

                memberId = "all";
                break;

            case 0:
                return;

            default:

                System.out.println();
                System.out.println("Invalid choice.");
                pressEnterToContinue();
                return;
        }

        showLoading("Loading redemption history...");

        ListInterface<RewardRedemption> history
                = memberId.equalsIgnoreCase("all")
                ? loyaltyController.viewRedemptionHistory()
                : loyaltyController.viewRedemptionHistoryByMember(memberId);

        clearScreen();
        printCenteredBanner(BANNER_REWARDS);

        boolean showMember = memberId.equalsIgnoreCase("all");

        int tableWidth = getRedemptionTableWidth(history, showMember);

        printRedemptionHeader(
                showMember
                        ? "ALL REDEMPTIONS"
                        : "MEMBER REDEMPTION HISTORY",
                tableWidth
        );

        if (showMember) {

            System.out.println("VIEW: ALL MEMBERS");

        } else {

            Member member
                    = loyaltyController.searchMemberById(memberId);

            if (member != null) {

                System.out.println(
                        "CURRENT MEMBER: "
                        + memberSummaryLine(member)
                );
            }
        }

        System.out.println();

        if (history.isEmpty()) {

            System.out.println("No redemption records found.");

        } else {

            printRedemptionTable(history, showMember);
        }

        pressEnterToContinue();
    }

// Calculates the redemption table width dynamically.
    private int getRedemptionTableWidth(
            ListInterface<RewardRedemption> history,
            boolean showMember) {

        int redemptionWidth = "Redemp ID".length();
        int memberWidth = "Member".length();
        int rewardWidth = "Reward".length();
        int pointsWidth = "Points".length();
        int dateWidth = "Date".length();

        for (int i = 1; i <= history.size(); i++) {

            RewardRedemption r = history.getEntry(i);

            Reward reward
                    = loyaltyController.findRewardById(
                            r.getRewardId()
                    );

            String rewardName
                    = reward != null
                            ? safeText(reward.getRewardName())
                            : safeText(r.getRewardId());

            redemptionWidth = Math.max(
                    redemptionWidth,
                    textLength(r.getRedemptionId())
            );

            memberWidth = Math.max(
                    memberWidth,
                    textLength(r.getMemberId())
            );

            rewardWidth = Math.max(
                    rewardWidth,
                    rewardName.length()
            );

            pointsWidth = Math.max(
                    pointsWidth,
                    formatPoints(r.getRedeemedPoints()).length()
            );

            dateWidth = Math.max(
                    dateWidth,
                    textLength(String.valueOf(r.getRedeemDate()))
            );
        }

        if (showMember) {

            return redemptionWidth
                    + memberWidth
                    + rewardWidth
                    + pointsWidth
                    + dateWidth
                    + (5 * 3)
                    + 2;

        } else {

            return redemptionWidth
                    + rewardWidth
                    + pointsWidth
                    + dateWidth
                    + (4 * 3)
                    + 2;
        }
    }

// Displays the redemption table header.
    private void printRedemptionHeader(
            String title,
            int tableWidth) {

        System.out.println();
        System.out.println("-".repeat(tableWidth));

        int padding
                = Math.max(
                        0,
                        (tableWidth - title.length()) / 2
                );

        System.out.println(
                " ".repeat(padding) + title
        );

        System.out.println("-".repeat(tableWidth));
    }
// Displays redemption records in a dynamically sized table.

    private void printRedemptionTable(
            ListInterface<RewardRedemption> history,
            boolean showMember) {

        if (history.isEmpty()) {
            System.out.println("No redemption records found.");
            return;
        }

        int redemptionWidth = "Redemp ID".length();
        int memberWidth = "Member".length();
        int rewardWidth = "Reward".length();
        int pointsWidth = "Points".length();
        int dateWidth = "Date".length();

        for (int i = 1; i <= history.size(); i++) {

            RewardRedemption r = history.getEntry(i);

            Reward reward
                    = loyaltyController.findRewardById(
                            r.getRewardId()
                    );

            String rewardName
                    = reward != null
                            ? safeText(reward.getRewardName())
                            : safeText(r.getRewardId());

            redemptionWidth = Math.max(
                    redemptionWidth,
                    textLength(r.getRedemptionId())
            );

            memberWidth = Math.max(
                    memberWidth,
                    textLength(r.getMemberId())
            );

            rewardWidth = Math.max(
                    rewardWidth,
                    rewardName.length()
            );

            pointsWidth = Math.max(
                    pointsWidth,
                    formatPoints(r.getRedeemedPoints()).length()
            );

            dateWidth = Math.max(
                    dateWidth,
                    textLength(String.valueOf(r.getRedeemDate()))
            );
        }

        int tableWidth;

        if (showMember) {

            tableWidth
                    = redemptionWidth
                    + memberWidth
                    + rewardWidth
                    + pointsWidth
                    + dateWidth
                    + (5 * 3)
                    + 2;

        } else {

            tableWidth
                    = redemptionWidth
                    + rewardWidth
                    + pointsWidth
                    + dateWidth
                    + (4 * 3)
                    + 2;
        }

        System.out.println("-".repeat(tableWidth));

        if (showMember) {

            printDynamicRow(
                    new String[]{
                        "Redemp ID",
                        "Member",
                        "Reward",
                        "Points",
                        "Date"
                    },
                    new int[]{
                        redemptionWidth,
                        memberWidth,
                        rewardWidth,
                        pointsWidth,
                        dateWidth
                    }
            );

        } else {

            printDynamicRow(
                    new String[]{
                        "Redemp ID",
                        "Reward",
                        "Points",
                        "Date"
                    },
                    new int[]{
                        redemptionWidth,
                        rewardWidth,
                        pointsWidth,
                        dateWidth
                    }
            );
        }

        System.out.println("-".repeat(tableWidth));

        for (int i = 1; i <= history.size(); i++) {

            RewardRedemption r = history.getEntry(i);

            Reward reward
                    = loyaltyController.findRewardById(
                            r.getRewardId()
                    );

            String rewardName
                    = reward != null
                            ? safeText(reward.getRewardName())
                            : safeText(r.getRewardId());

            if (showMember) {

                printDynamicRow(
                        new String[]{
                            r.getRedemptionId(),
                            r.getMemberId(),
                            rewardName,
                            formatPoints(r.getRedeemedPoints()),
                            String.valueOf(r.getRedeemDate())
                        },
                        new int[]{
                            redemptionWidth,
                            memberWidth,
                            rewardWidth,
                            pointsWidth,
                            dateWidth
                        }
                );

            } else {

                printDynamicRow(
                        new String[]{
                            r.getRedemptionId(),
                            rewardName,
                            formatPoints(r.getRedeemedPoints()),
                            String.valueOf(r.getRedeemDate())
                        },
                        new int[]{
                            redemptionWidth,
                            rewardWidth,
                            pointsWidth,
                            dateWidth
                        }
                );
            }
        }

        System.out.println("-".repeat(tableWidth));
    }

    // Adds a new reward.
    private void addReward() {

        clearScreen();
        printCenteredBanner(BANNER_REWARDS);
        printHeader("ADD REWARD");
        printBackHint();

        String id
                = loyaltyController.generateNextRewardId();

        System.out.println(
                "Assigned Reward ID : " + id
        );

        String name
                = readValidText("Reward Name        : ");

        String category
                = readCategoryChoice();

        int points
                = readPositiveInt("Points Required    : ");

        int qty
                = readNonNegativeInt("Quantity           : ");

        showLoading("Adding reward...");

        ControllerResult result
                = loyaltyController.addReward(
                        id,
                        name,
                        category,
                        points,
                        qty
                );

        if (!result.isOk()) {

            printResult(result);
            pressEnterToContinue();
            return;
        }

        Reward added
                = loyaltyController.findRewardById(id);

        clearScreen();
        printHeader("REWARD ADDED SUCCESSFULLY");

        System.out.println();
        System.out.println("ADDED REWARD");
        printDivider();

        String[] addedRewardDetails = {
            added.getRewardId(),
            added.getRewardName(),
            added.getCategory(),
            formatPoints(added.getPointsRequired()) + " pts",
            "Qty " + added.getQuantity()
        };

        int addedWidth = 0;

        for (String line : addedRewardDetails) {
            addedWidth = Math.max(addedWidth, line.length());
        }

        System.out.println("-".repeat(addedWidth + 4));

        for (String line : addedRewardDetails) {
            System.out.printf(
                    "| %-" + addedWidth + "s |%n",
                    line
            );
        }

        System.out.println("-".repeat(addedWidth + 4));

        printDivider();

        System.out.println();
        System.out.println("UPDATED REWARD CATALOGUE");
        printDivider();

        printRewardTable(
                loyaltyController.viewRewards()
        );

        printDivider();

        pressEnterToContinue();
    }

    // Restocks an existing reward.
    private void restockReward() {

        clearScreen();
        printCenteredBanner(BANNER_REWARDS);
        printHeader("RESTOCK REWARD");
        printBackHint();

        System.out.println();
        System.out.println("CURRENT REWARD STOCK");
        printDivider();

        printRewardTable(
                loyaltyController.viewRewards()
        );

        printDivider();

        System.out.println();
        System.out.println(
                "Stock Status:"
        );

        System.out.println(
                "  LOW = 1-"
                + loyaltyController.getLowStockThreshold()
                + " units remaining"
        );

        System.out.println(
                "  OUT = 0 units remaining"
        );

        System.out.println(
                "  OK  = more than "
                + loyaltyController.getLowStockThreshold()
                + " units"
        );

        System.out.println();

        String rewardId
                = readValidId("Reward ID       : ");

        Reward reward
                = loyaltyController.findRewardById(rewardId);

        if (reward == null) {

            System.out.println();
            System.out.println("Reward not found.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        String rewardDisplay = safeText(reward.getRewardName());

        int rewardInfoWidth = Math.max(
                20,
                rewardDisplay.length()
        );

        System.out.printf(
                "%-20s %-" + rewardInfoWidth + "s%n",
                "Reward",
                rewardDisplay
        );

        System.out.printf(
                "%-20s %d%n",
                "Current Stock",
                reward.getQuantity()
        );

        int quantity
                = readPositiveInt("Quantity to Add : ");

        showLoading("Restocking reward...");

        ControllerResult result
                = loyaltyController.restockReward(
                        rewardId,
                        quantity
                );

        if (!result.isOk()) {

            printResult(result);
            pressEnterToContinue();
            return;
        }

        Reward updated
                = loyaltyController.findRewardById(rewardId);

        clearScreen();
        printHeader("REWARD RESTOCKED SUCCESSFULLY");

        System.out.println();
        System.out.println("RESTOCKED REWARD");
        printDivider();

        System.out.printf(
                "%s | %s%n",
                updated.getRewardId(),
                updated.getRewardName()
        );

        System.out.printf(
                "Stock: %d -> %d  (+%d)%n",
                reward.getQuantity() - quantity,
                updated.getQuantity(),
                quantity
        );

        printDivider();

        System.out.println();
        System.out.println("UPDATED REWARD CATALOGUE");
        printDivider();

        printRewardTable(
                loyaltyController.viewRewards()
        );

        printDivider();

        pressEnterToContinue();
    }

    // Runs the loyalty tiers menu.
    private void loyaltyTiersMenu() {

        boolean running = true;

        while (running) {

            clearScreen();
            printCenteredBanner(BANNER_TIERS);
            printHeader("LOYALTY & TIERS");

            processExpiredPointsAndNotify();

            printTierLevelsTable();

            if (currentMember != null) {
                System.out.println();
                printCurrentMemberBanner();
            }

            System.out.println();

            printDivider();
            System.out.print(
                    "                         1. View Tier Progress                               \n"
                    + "                         2. Simulate Tier Progress                            \n"
                    + "                         3. Tier Distribution                                 \n"
                    + "                         4. Notifications                                     \n"
                    + "                         0. Back                                              \n"
            );
            printDivider();
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {

                    case "1":
                        viewMyTierProgress();
                        break;

                    case "2":
                        simulateTierUpgrade();
                        break;

                    case "3":
                        tierDistributionReport();
                        break;

                    case "4":
                        notificationCentre();
                        break;

                    case "0":
                        running = false;
                        break;

                    default:
                        System.out.println("\nInvalid option. Please try again.\n");
                }
            } catch (ActionCancelledException e) {
                System.out.println("\nCancelled.\n");
            }
        }
    }

    // Displays the available membership tiers.
    private void printTierLevelsTable() {

        String[] tiers = {
            "Silver",
            "Gold",
            "Elite",
            "Diamond",
            "Platinum"
        };

        System.out.println();
        System.out.println("TIER LEVELS");
        printDivider();

        System.out.printf(
                "%-15s %-20s %-12s%n",
                "Tier",
                "Required Points",
                "Multiplier"
        );

        printDivider();

        for (String tier : tiers) {

            double multiplier
                    = loyaltyController.getTierMultiplier(tier);

            int threshold
                    = loyaltyController.getTierThreshold(tier);

            System.out.printf(
                    "%-15s %-20s x%-11.2f%n",
                    tier,
                    formatPoints(threshold),
                    multiplier
            );
        }

        printDivider();
    }

    // Displays the current member's tier progress.
    private void viewMyTierProgress() {

        clearScreen();
        printCenteredBanner(BANNER_TIERS);
        printHeader("TIER PROGRESS");
        printCurrentMemberBanner();
        printBackHint();

        String id;

        if (currentMember == null) {

            System.out.println();
            System.out.println("No current member selected.");
            System.out.println(
                    "Please enter a Member ID to view tier progress."
            );
            System.out.println();

            id = readExistingMemberId(
                    "Member ID: "
            );

        } else {

            id = resolveMemberId(
                    "Member ID (blank = current): "
            );
        }

        Member member
                = loyaltyController.searchMemberById(id);

        if (currentMember != null
                && currentMember.getMemberId()
                        .equals(member.getMemberId())) {

            setCurrentMember(member);
        }

        // =========================
        // MEMBER BOX
        // =========================
        printBox(
                "MEMBER",
                new String[]{
                    "Member ID   : " + member.getMemberId(),
                    "Name        : " + member.getName(),
                    "Tier        : " + member.getTier(),
                    "Points      : "
                    + formatPoints(member.getPoints())
                    + " pts"
                }
        );

        System.out.println();

        // =========================
        // TIER PROGRESS
        // =========================
        String progressDisplay
                = loyaltyController.getTierProgressDisplay(
                        member.getTier(),
                        member.getPoints()
                );

        String[] progressLines
                = progressDisplay.split("\\R");

        String progressTo
                = progressLines.length > 0
                        ? progressLines[0]
                        : "-";

        String currentTier
                = progressLines.length > 1
                        ? progressLines[1]
                        : member.getTier();

        String progressText
                = progressLines.length > 2
                        ? progressLines[2]
                        : "";

        String pointsNeeded
                = progressLines.length > 3
                        ? progressLines[3]
                        : "";

        // Separate progress percentage from progress bar.
        String progressBar = progressText;
        String percentage = "";

        int percentIndex
                = progressText.lastIndexOf("%");

        if (percentIndex >= 0) {

            int percentStart
                    = progressText.lastIndexOf(" ");

            if (percentStart >= 0
                    && percentStart < percentIndex) {

                percentage
                        = progressText.substring(
                                percentStart + 1
                        ).trim();

                progressBar
                        = progressText.substring(
                                0,
                                percentStart
                        ).trim();
            }
        }

        String nextTier
                = progressTo
                        .replace("Progress to ", "")
                        .trim();

        String needed
                = pointsNeeded
                        .replace("Need ", "")
                        .trim();

        // =========================
// TIER PROGRESS
// =========================
        if (progressTo.toLowerCase().contains("highest tier")
                || currentTier.equalsIgnoreCase("Platinum")) {

            printBox(
                    "TIER PROGRESS",
                    new String[]{
                        "Current Tier : " + currentTier,
                        "Next Tier    : Highest tier reached",
                        "Progress     : Completed",
                        "Points Needed: No further points needed"
                    }
            );

        } else {

            printBox(
                    "TIER PROGRESS",
                    new String[]{
                        "Current Tier : " + currentTier,
                        "Next Tier    : " + nextTier,
                        "Progress     : ["
                        + progressBar
                        + "] "
                        + percentage,
                        "Points Needed: " + needed
                    }
            );
        }

        // Keep the screen visible before returning.
        pressEnterToContinue();
    }

// Displays information inside a fixed-width box.
    private void printBox(String title, String[] lines) {

        int contentWidth = WIDTH - 4;

        System.out.println(
                "+" + "-".repeat(contentWidth + 2) + "+"
        );

        System.out.printf(
                "| %-"
                + contentWidth
                + "s |%n",
                title
        );

        System.out.println(
                "+" + "-".repeat(contentWidth + 2) + "+"
        );

        for (String line : lines) {

            String safeLine
                    = line == null ? "" : line;

            // Prevent text from escaping the box.
            if (safeLine.length() > contentWidth) {
                safeLine
                        = safeLine.substring(
                                0,
                                contentWidth
                        );
            }

            System.out.printf(
                    "| %-"
                    + contentWidth
                    + "s |%n",
                    safeLine
            );
        }

        System.out.println(
                "+" + "-".repeat(contentWidth + 2) + "+"
        );
    }

    // Displays a report inside a clean fixed-width box.
    private void printReportBox(
            String title,
            java.util.ArrayList<String> lines) {

        int contentWidth = WIDTH - 4;

        // Find the longest line.
        int longestLine = title.length();

        for (String line : lines) {

            if (line != null) {
                longestLine = Math.max(
                        longestLine,
                        line.length()
                );
            }
        }

        // Use the normal screen width.
        // Do not allow content to exceed the box.
        longestLine = Math.min(
                longestLine,
                contentWidth
        );

        System.out.println();

        // Top border
        System.out.println(
                "+"
                + "-".repeat(contentWidth + 2)
                + "+"
        );

        // Centered title
        int titlePadding
                = Math.max(
                        0,
                        (contentWidth - title.length()) / 2
                );

        int titleRightPadding
                = Math.max(
                        0,
                        contentWidth
                        - titlePadding
                        - title.length()
                );

        System.out.println(
                "| "
                + " ".repeat(titlePadding)
                + title
                + " ".repeat(titleRightPadding)
                + " |"
        );

        // Header border
        System.out.println(
                "+"
                + "-".repeat(contentWidth + 2)
                + "+"
        );

        // Content
        for (String line : lines) {

            String safeLine
                    = line == null
                            ? ""
                            : line;

            // Pad only if shorter.
            if (safeLine.length() < contentWidth) {

                safeLine
                        = String.format(
                                "%-" + contentWidth + "s",
                                safeLine
                        );

            } else if (safeLine.length() > contentWidth) {

                // Prevent the right border from being pushed out.
                safeLine
                        = safeLine.substring(
                                0,
                                contentWidth
                        );
            }

            System.out.println(
                    "| "
                    + safeLine
                    + " |"
            );
        }

        // Bottom border
        System.out.println(
                "+"
                + "-".repeat(contentWidth + 2)
                + "+"
        );
    }

    // Displays the notification centre.
    private void notificationCentre() {

        clearScreen();
        printCenteredBanner(BANNER_NOTIFICATIONS);
        printHeader("NOTIFICATIONS");

        // Process expired points before displaying notifications.
        loyaltyController.processExpiredPoints();

        ListInterface<PointsTransaction> expiring
                = loyaltyController.getExpiringSoonTransactions(null);

        ListInterface<PointsTransaction> expired
                = loyaltyController.getRecentlyExpiredTransactions(null);

        ListInterface<Reward> lowStock
                = loyaltyController.getLowStockRewards();

        // ==================================================
        // NOTIFICATION SUMMARY
        // ==================================================
        System.out.println();

        int notificationCount
                = expiring.size()
                + expired.size()
                + lowStock.size();

        if (notificationCount == 0) {

            printBox(
                    "NOTIFICATION SUMMARY",
                    new String[]{
                        "[OK] No active notifications.",
                        "Everything looks good."
                    }
            );

            pressEnterToContinue();
            return;
        }

        // Build a clean summary.
        java.util.ArrayList<String> summary
                = new java.util.ArrayList<>();

        if (!expiring.isEmpty()) {
            summary.add(
                    "[!] "
                    + expiring.size()
                    + " point record(s) expiring soon."
            );
        }

        if (!expired.isEmpty()) {
            summary.add(
                    "[!] "
                    + expired.size()
                    + " recently expired point record(s)."
            );
        }

        if (!lowStock.isEmpty()) {
            summary.add(
                    "[!] "
                    + lowStock.size()
                    + " reward(s) low in stock."
            );
        }

        printBox(
                "NOTIFICATION SUMMARY",
                summary.toArray(new String[0])
        );

        // ==================================================
        // POINTS EXPIRY
        // ==================================================
        if (!expiring.isEmpty() || !expired.isEmpty()) {

            /*
         * printExpiryAlerts() already prints its own
         * POINTS EXPIRY header, so do not print another
         * POINTS EXPIRY header here.
             */
            printExpiryAlerts(null);
        }

        // ==================================================
        // LOW STOCK REWARDS
        // ==================================================
        if (!lowStock.isEmpty()) {

            System.out.println();
            System.out.println("LOW STOCK REWARDS");
            printDivider();

            System.out.printf(
                    "%-35s | %10s |%n",
                    "Reward",
                    "Remaining"
            );

            printDivider();

            for (int i = 1; i <= lowStock.size(); i++) {

                Reward reward
                        = lowStock.getEntry(i);

                System.out.printf(
                        "%-35s | %10d |%n",
                        safeText(reward.getRewardName()),
                        reward.getQuantity()
                );
            }

            printDivider();

            System.out.println();
            System.out.println(
                    "Tip: Use Rewards > Restock Reward to add stock."
            );
        }

        pressEnterToContinue();
    }

    // Simulates a membership tier upgrade.
    private void simulateTierUpgrade() {

        clearScreen();
        printCenteredBanner(BANNER_TIERS);
        printHeader("SIMULATE TIER PROGRESS");
        printCurrentMemberBanner();
        printBackHint();

        String id = resolveMemberId(
                "Member ID                : "
        );

        Member member
                = loyaltyController.searchMemberById(id);

        if (member == null) {
            System.out.println();
            System.out.println("Member not found.");
            pressEnterToContinue();
            return;
        }

        // =========================
        // CURRENT MEMBER
        // =========================
        printBox(
                "CURRENT MEMBER",
                new String[]{
                    "Member ID : " + member.getMemberId(),
                    "Name      : " + member.getName(),
                    "Tier      : " + member.getTier(),
                    "Points    : "
                    + formatPoints(member.getPoints())
                    + " pts"
                }
        );

        System.out.println();

        // =========================
        // SIMULATION INPUT
        // =========================
        int hypothetical
                = readNonNegativeInt(
                        "Simulate earning how many points: "
                );

        int simulatedPoints
                = member.getPoints() + hypothetical;

        String simulatedTier
                = loyaltyController.calculateTier(
                        simulatedPoints
                );

        String status;

        if (simulatedTier.equalsIgnoreCase("Platinum")) {

            status = "Highest tier reached";

        } else if (!simulatedTier.equalsIgnoreCase(member.getTier())) {

            status = "Tier upgrade achieved";

        } else {

            status = "No tier change";
        }

        // =========================
        // SIMULATION RESULT
        // =========================
        printBox(
                "SIMULATION RESULT",
                new String[]{
                    "Points Earned : +"
                    + formatPoints(hypothetical)
                    + " pts",
                    "New Points    : "
                    + formatPoints(simulatedPoints)
                    + " pts",
                    "New Tier      : "
                    + simulatedTier,
                    "Status        : "
                    + status
                }
        );

        pressEnterToContinue();
    }

    // Displays the tier distribution report.
    private void tierDistributionReport() {

        clearScreen();
        printCenteredBanner(BANNER_TIERS);
        printHeader("TIER DISTRIBUTION");

        showLoading("Calculating tier distribution...");

        int[] counts
                = loyaltyController.membershipTierDistribution();

        String[] tiers = {
            "Silver",
            "Gold",
            "Elite",
            "Diamond",
            "Platinum"
        };

        int totalMembers = 0;

        for (int count : counts) {
            totalMembers += count;
        }

        // ==========================================
        // NO MEMBERS
        // ==========================================
        if (totalMembers == 0) {

            System.out.println();

            printBox(
                    "TIER DISTRIBUTION",
                    new String[]{
                        "No members registered yet."
                    }
            );

            pressEnterToContinue();
            return;
        }

        // ==========================================
        // COLUMN WIDTHS
        // ==========================================
        int tierWidth = 10;
        int memberWidth = 7;
        int percentageWidth = 8;
        int chartWidth = 20;

        /*
     * Exact width of:
     *
     * | Tier       | Members |        % | Chart                |
     *
     * The +13 accounts for the spaces and separators.
         */
        int tableWidth
                = tierWidth
                + memberWidth
                + percentageWidth
                + chartWidth
                + 13;

        // ==========================================
        // TOP BORDER
        // ==========================================
        System.out.println(
                "+"
                + "-".repeat(tableWidth - 2)
                + "+"
        );

        // ==========================================
        // TITLE
        // ==========================================
        String title = "TIER DISTRIBUTION";

        int titleWidth = tableWidth - 4;

        int leftPadding
                = (titleWidth - title.length()) / 2;

        int rightPadding
                = titleWidth
                - leftPadding
                - title.length();

        System.out.println(
                "| "
                + " ".repeat(leftPadding)
                + title
                + " ".repeat(rightPadding)
                + " |"
        );

        // ==========================================
        // HEADER BORDER
        // ==========================================
        System.out.println(
                "+"
                + "-".repeat(tableWidth - 2)
                + "+"
        );

        // ==========================================
        // TABLE HEADER
        // ==========================================
        System.out.printf(
                "| %-10s | %7s | %8s | %-20s |%n",
                "Tier",
                "Members",
                "%",
                "Chart"
        );

        System.out.println(
                "+"
                + "-".repeat(tableWidth - 2)
                + "+"
        );

        // ==========================================
        // TIER DATA
        // ==========================================
        for (int i = 0; i < tiers.length; i++) {

            double percentage
                    = (double) counts[i]
                    / totalMembers
                    * 100;

            int barLength
                    = (int) Math.round(
                            (double) counts[i]
                            / totalMembers
                            * chartWidth
                    );

            barLength
                    = Math.min(
                            barLength,
                            chartWidth
                    );

            String bar
                    = "#".repeat(barLength);

            System.out.printf(
                    "| %-10s | %7d | %7.1f%% | %-20s |%n",
                    tiers[i],
                    counts[i],
                    percentage,
                    bar
            );
        }

        // ==========================================
        // TOTAL BORDER
        // ==========================================
        System.out.println(
                "+"
                + "-".repeat(tableWidth - 2)
                + "+"
        );

        // ==========================================
        // TOTAL ROW
        // ==========================================
        System.out.printf(
                "| %-10s | %7d | %7.1f%% | %-20s |%n",
                "Total",
                totalMembers,
                100.0,
                ""
        );

        // ==========================================
        // BOTTOM BORDER
        // ==========================================
        System.out.println(
                "+"
                + "-".repeat(tableWidth - 2)
                + "+"
        );

        pressEnterToContinue();
    }

    // Runs the reports and management menu.
    private void reportsManagementMenu() {

        boolean running = true;

        while (running) {

            clearScreen();
            printCenteredBanner(BANNER_REPORTS);
            printHeader("REPORTS & MANAGEMENT");

            System.out.print(
                    "                         1. Member Loyalty Report                            \n"
                    + "                         2. Member Ranking                                    \n"
                    + "                         3. Redemption Analysis                               \n"
                    + "                         4. Reward Popularity                                 \n"
                    + "                         5. System Statistics                                 \n"
                    + "                         0. Back                                              \n"
            );
            printDivider();
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {

                    case "1":
                        topMembersReport();
                        break;

                    case "2":
                        memberRankingReport();
                        break;

                    case "3":
                        redemptionStatisticsReport();
                        break;

                    case "4":
                        rewardPopularityReport();
                        break;

                    case "5":
                        systemStatisticsDashboard();
                        break;

                    case "0":
                        running = false;
                        break;

                    default:
                        System.out.println("\nInvalid option. Please try again.\n");
                }
            } catch (ActionCancelledException e) {
                System.out.println("\nCancelled.\n");
            }
        }
    }

    // Displays the member loyalty report.
    private void topMembersReport() {

        clearScreen();
        printCenteredBanner(BANNER_REPORTS);
        printHeader("MEMBER LOYALTY REPORT");
        printBackHint();

        System.out.println(
                "Filter by Tier (blank = all): "
                + "Silver / Gold / Elite / Diamond / Platinum"
        );

        String tierFilter = readOptionalTier(
                "Tier             : "
        );

        int minPoints = readNonNegativeInt(
                "Minimum Points (0 = no minimum) : "
        );

        int maxPoints = readOptionalNonNegativeInt(
                "Maximum Points (blank = no maximum) : "
        );

        if (maxPoints != Integer.MAX_VALUE
                && minPoints > maxPoints) {

            printReportBox(
                    "INVALID FILTER",
                    new java.util.ArrayList<>(
                            java.util.List.of(
                                    "Maximum Points must be greater",
                                    "than or equal to Minimum Points."
                            )
                    )
            );

            pressEnterToContinue();
            return;
        }

        showLoading(
                "Filtering and sorting members..."
        );

        ListInterface<Member> report
                = loyaltyController.getMemberLoyaltyReport(
                        tierFilter,
                        minPoints,
                        maxPoints
                );

        // ==========================================
        // FILTER INFORMATION
        // ==========================================
        java.util.ArrayList<String> filterLines
                = new java.util.ArrayList<>();

        filterLines.add(
                String.format(
                        "%-16s : %s",
                        "Tier",
                        tierFilter.isBlank()
                        ? "All Tiers"
                        : tierFilter
                )
        );

        filterLines.add(
                String.format(
                        "%-16s : %s",
                        "Minimum Points",
                        formatPoints(minPoints)
                )
        );

        filterLines.add(
                String.format(
                        "%-16s : %s",
                        "Maximum Points",
                        maxPoints == Integer.MAX_VALUE
                                ? "Unlimited"
                                : formatPoints(maxPoints)
                )
        );

        filterLines.add(
                String.format(
                        "%-16s : %s",
                        "Sort",
                        "Points Descending"
                )
        );

        filterLines.add(
                String.format(
                        "%-16s : %d",
                        "Members Found",
                        report.size()
                )
        );

        printReportBox(
                "REPORT FILTER",
                filterLines
        );

        // ==========================================
        // RESULTS
        // ==========================================
        if (report.isEmpty()) {

            printReportBox(
                    "MEMBER LOYALTY RESULTS",
                    new java.util.ArrayList<>(
                            java.util.List.of(
                                    "No members match the selected filter."
                            )
                    )
            );

        } else {

            java.util.ArrayList<String> resultLines
                    = new java.util.ArrayList<>();

            resultLines.add(
                    String.format(
                            "%-6s %-9s %-20s %-12s %10s",
                            "Rank",
                            "ID",
                            "Name",
                            "Tier",
                            "Points"
                    )
            );

            resultLines.add(
                    "-".repeat(WIDTH - 4)
            );

            for (int i = 1; i <= report.size(); i++) {

                Member member
                        = report.getEntry(i);

                resultLines.add(
                        String.format(
                                "%-6d %-9s %-20s %-12s %10s",
                                i,
                                safeText(
                                        member.getMemberId()
                                ),
                                safeText(
                                        member.getName()
                                ),
                                safeText(
                                        member.getTier()
                                ),
                                formatPoints(
                                        member.getPoints()
                                )
                        )
                );
            }

            printReportBox(
                    "MEMBER LOYALTY RESULTS",
                    resultLines
            );
        }

        pressEnterToContinue();
    }

    // Displays redemption statistics.
    private void redemptionStatisticsReport() {

        clearScreen();
        printCenteredBanner(BANNER_REPORTS);
        printHeader("REDEMPTION ANALYSIS");
        printBackHint();

        System.out.println(
                "Filter by Category (blank = all): "
                + "Dining / Spa / Room / Others"
        );

        String categoryFilter
                = readOptionalCategory(
                        "Category         : "
                );

        showLoading(
                "Filtering and counting redemptions..."
        );

        int total
                = loyaltyController.getTotalRedemptions();

        ListInterface<LoyaltyController.CategoryCount> stats
                = loyaltyController.getRedemptionStatsByCategory(
                        categoryFilter
                );

        int filteredTotal = 0;

        for (int i = 1; i <= stats.size(); i++) {

            filteredTotal
                    += stats.getEntry(i).count;
        }

        if (stats.isEmpty()
                || filteredTotal == 0) {

            printReportBox(
                    "REDEMPTION ANALYSIS",
                    new java.util.ArrayList<>(
                            java.util.List.of(
                                    "Category Filter : "
                                    + (categoryFilter.isBlank()
                                    ? "All Categories"
                                    : categoryFilter),
                                    "No redemption records match this filter."
                            )
                    )
            );

            pressEnterToContinue();
            return;
        }

        // ==========================================
        // ANALYSIS SUMMARY
        // ==========================================
        java.util.ArrayList<String> summary
                = new java.util.ArrayList<>();

        summary.add(
                "Category Filter : "
                + (categoryFilter.isBlank()
                ? "All Categories"
                : categoryFilter)
        );

        summary.add(
                "Matching Total  : "
                + filteredTotal
                + " (of "
                + total
                + " overall)"
        );

        printReportBox(
                "ANALYSIS SUMMARY",
                summary
        );

        // ==========================================
        // CATEGORY RESULTS
        // ==========================================
        java.util.ArrayList<String> categoryLines
                = new java.util.ArrayList<>();

        categoryLines.add(
                String.format(
                        "%-14s %7s %7s   %-20s",
                        "Category",
                        "Count",
                        "%",
                        "Chart"
                )
        );

        categoryLines.add(
                "-".repeat(WIDTH - 4)
        );

        int barMaxWidth = 20;

        for (int i = 1; i <= stats.size(); i++) {

            LoyaltyController.CategoryCount c
                    = stats.getEntry(i);

            int percent
                    = (int) Math.round(
                            (double) c.count
                            / filteredTotal
                            * 100
                    );

            int barLength
                    = (int) Math.round(
                            (double) c.count
                            / filteredTotal
                            * barMaxWidth
                    );

            barLength
                    = Math.min(
                            barLength,
                            barMaxWidth
                    );

            String bar
                    = "#".repeat(
                            Math.max(
                                    barLength,
                                    0
                            )
                    );

            categoryLines.add(
                    String.format(
                            "%-14s %7d %6s   %-20s",
                            safeText(c.category),
                            c.count,
                            percent + "%",
                            bar
                    )
            );
        }

        printReportBox(
                "REDEMPTION BY CATEGORY",
                categoryLines
        );

        pressEnterToContinue();
    }

    // Displays reward popularity statistics.
    private void rewardPopularityReport() {

        clearScreen();
        printCenteredBanner(BANNER_REPORTS);
        printHeader("REWARD POPULARITY CHART");

        showLoading(
                "Calculating popularity..."
        );

        int total
                = loyaltyController.getTotalRedemptions();

        ListInterface<RewardPopularity> stats
                = loyaltyController.getRewardPopularity();

        if (stats.isEmpty() || total == 0) {

            printReportBox(
                    "REWARD POPULARITY",
                    new java.util.ArrayList<>(
                            java.util.List.of(
                                    "No redemption records yet."
                            )
                    )
            );

            pressEnterToContinue();
            return;
        }

        java.util.ArrayList<String> lines
                = new java.util.ArrayList<>();

        lines.add(
                String.format(
                        "%-25s %7s %7s   %-20s",
                        "Reward",
                        "Count",
                        "%",
                        "Chart"
                )
        );

        lines.add(
                "-".repeat(WIDTH - 4)
        );

        int barMaxWidth = 20;

        for (int i = 1; i <= stats.size(); i++) {

            RewardPopularity p
                    = stats.getEntry(i);

            int percent
                    = (int) Math.round(
                            (double) p.count
                            / total
                            * 100
                    );

            int barLength
                    = (int) Math.round(
                            (double) p.count
                            / total
                            * barMaxWidth
                    );

            barLength
                    = Math.min(
                            barLength,
                            barMaxWidth
                    );

            String bar
                    = "#".repeat(
                            Math.max(
                                    barLength,
                                    0
                            )
                    );

            lines.add(
                    String.format(
                            "%-25s %7d %6s   %-20s",
                            safeText(p.rewardName),
                            p.count,
                            percent + "%",
                            bar
                    )
            );
        }

        printReportBox(
                "REWARD POPULARITY",
                lines
        );

        pressEnterToContinue();
    }

    // Displays the member ranking report.
    private void memberRankingReport() {

        clearScreen();
        printCenteredBanner(BANNER_REPORTS);
        printHeader("MEMBER RANKING");

        showLoading(
                "Calculating rankings..."
        );

        int total
                = loyaltyController.getTotalMemberCount();

        ListInterface<Member> ranked
                = loyaltyController.topMembersByPoints(
                        total
                );

        if (ranked.isEmpty()) {

            printReportBox(
                    "MEMBER RANKING",
                    new java.util.ArrayList<>(
                            java.util.List.of(
                                    "No members found."
                            )
                    )
            );

            pressEnterToContinue();
            return;
        }

        // ==========================================
        // SUMMARY
        // ==========================================
        java.util.ArrayList<String> summary
                = new java.util.ArrayList<>();

        summary.add(
                String.format(
                        "%-18s : %d",
                        "Total Members",
                        total
                )
        );

        summary.add(
                String.format(
                        "%-18s : Points Balance",
                        "Ranking Basis"
                )
        );

        summary.add(
                String.format(
                        "%-18s : Highest to Lowest",
                        "Order"
                )
        );

        printReportBox(
                "RANKING SUMMARY",
                summary
        );

        // ==========================================
        // RANKING TABLE
        // ==========================================
        java.util.ArrayList<String> rankingLines
                = new java.util.ArrayList<>();

        rankingLines.add(
                String.format(
                        "%-6s %-9s %-22s %-12s %10s",
                        "Rank",
                        "ID",
                        "Member",
                        "Tier",
                        "Points"
                )
        );

        // Full-width separator
        rankingLines.add(
                "-".repeat(WIDTH - 4)
        );

        for (int i = 1; i <= ranked.size(); i++) {

            Member member
                    = ranked.getEntry(i);

            rankingLines.add(
                    String.format(
                            "%-6d %-9s %-22s %-12s %10s",
                            i,
                            safeText(
                                    member.getMemberId()
                            ),
                            safeText(
                                    member.getName()
                            ),
                            safeText(
                                    member.getTier()
                            ),
                            formatPoints(
                                    member.getPoints()
                            )
                    )
            );
        }

        printReportBox(
                "MEMBER RANKING",
                rankingLines
        );

        pressEnterToContinue();
    }

    // Displays system statistics.
    private void systemStatisticsDashboard() {

        clearScreen();
        printCenteredBanner(BANNER_REPORTS);
        printHeader("SYSTEM STATISTICS");

        showLoading(
                "Gathering system statistics..."
        );

        int[] tierCounts
                = loyaltyController.membershipTierDistribution();

        int totalMembers
                = loyaltyController.getTotalMemberCount();

        int rewardTypes
                = loyaltyController.getTotalRewardTypes();

        int totalPoints
                = loyaltyController.getTotalPointsAcrossMembers();

        int totalRedemptions
                = loyaltyController.getTotalRedemptions();

        int transactionsToday
                = loyaltyController.getTransactionsTodayCount();

        String popularCategory
                = loyaltyController.getMostPopularCategory();

        // ==========================================
        // SYSTEM OVERVIEW
        // ==========================================
        java.util.ArrayList<String> overview
                = new java.util.ArrayList<>();

        overview.add(
                String.format(
                        "%-25s : %d",
                        "Total Members",
                        totalMembers
                )
        );

        overview.add(
                String.format(
                        "%-25s : %d",
                        "Reward Types",
                        rewardTypes
                )
        );

        overview.add(
                String.format(
                        "%-25s : %s pts",
                        "Total Points",
                        formatPoints(totalPoints)
                )
        );

        overview.add(
                String.format(
                        "%-25s : %d",
                        "Rewards Redeemed",
                        totalRedemptions
                )
        );

        overview.add(
                String.format(
                        "%-25s : %d",
                        "Transactions Today",
                        transactionsToday
                )
        );

        overview.add(
                String.format(
                        "%-25s : %s",
                        "Most Popular Category",
                        safeText(popularCategory)
                )
        );

        printReportBox(
                "SYSTEM OVERVIEW",
                overview
        );

        // ==========================================
        // MEMBERSHIP TIER SUMMARY
        // ==========================================
        String[] tiers = {
            "Silver",
            "Gold",
            "Elite",
            "Diamond",
            "Platinum"
        };

        java.util.ArrayList<String> tierLines
                = new java.util.ArrayList<>();

        tierLines.add(
                String.format(
                        "%-18s %10s",
                        "Tier",
                        "Members"
                )
        );

        tierLines.add(
                "-".repeat(WIDTH - 4)
        );

        for (int i = 0; i < tiers.length; i++) {

            int count
                    = i < tierCounts.length
                            ? tierCounts[i]
                            : 0;

            tierLines.add(
                    String.format(
                            "%-18s %10d",
                            tiers[i],
                            count
                    )
            );
        }

        tierLines.add(
                "-".repeat(WIDTH - 4)
        );

        tierLines.add(
                String.format(
                        "%-18s %10d",
                        "Total",
                        totalMembers
                )
        );

        printReportBox(
                "MEMBERSHIP TIER SUMMARY",
                tierLines
        );

        // NO QUICK INSIGHTS HERE
        pressEnterToContinue();
    }

    private static class ActionCancelledException extends RuntimeException {
    }

    // Checks whether the input is a cancel keyword.
    private boolean isCancelKeyword(String input) {
        return input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b");
    }

    // Reads a string from the user.
    private String readString(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (isCancelKeyword(input)) {
            throw new ActionCancelledException();
        }
        return input;
    }

    // Reads an integer from the user.
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (isCancelKeyword(input)) {
                throw new ActionCancelledException();
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // Displays a controller result in a dynamically sized box.
    private void printResult(ControllerResult result) {

        String status = result.isOk()
                ? "Operation completed successfully."
                : "Operation failed.";

        String message = result.getMessage();

        int contentWidth = status.length();

        if (message != null && !message.isBlank()) {
            contentWidth = Math.max(contentWidth, message.length());
        }

        int boxWidth = contentWidth + 4;

        System.out.println();
        System.out.println("-".repeat(boxWidth));
        System.out.printf("| %-" + contentWidth + "s |%n", status);

        if (message != null && !message.isBlank()) {
            System.out.printf("| %-" + contentWidth + "s |%n", message);
        }

        System.out.println("-".repeat(boxWidth));
    }

    // Waits for the user to continue.
    private void pressEnterToContinue() {
        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }

    // Reads an optional reward category.
    private String readOptionalCategory(String prompt) {
        String[] categories = {
            "Dining",
            "Spa",
            "Room",
            "Others"
        };

        while (true) {
            String input = readString(prompt);

            if (input.isBlank()) {
                return "";
            }

            for (String category : categories) {
                if (category.equalsIgnoreCase(input)) {
                    return category;
                }
            }

            System.out.println(
                    "Invalid category. Choose Dining, Spa, Room, or Others.");
        }
    }

    // Reads a yes/no choice with Yes as the default.
    private boolean readYesNoWithDefaultYes(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (isCancelKeyword(input)) {
                throw new ActionCancelledException();
            }

            if (input.isBlank() || input.equalsIgnoreCase("Y")) {
                return true;
            }

            if (input.equalsIgnoreCase("N")) {
                return false;
            }

            System.out.println("Please enter Y or N.");
        }
    }
}
