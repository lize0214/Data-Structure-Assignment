package Boundery;

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

    // Displays the selected banner.
    private void printBanner(String[] banner) {
        if (banner == null || banner.length == 0) {
            return;
        }
        System.out.println();
        for (String line : banner) {
            System.out.println(line);
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

    // Gets the member ID using the current member when available.
    private String resolveMemberId(String prompt) {
        if (currentMember != null) {
            System.out.print(prompt + "(blank = " + currentMember.getMemberId() + ") : ");

            String input = scanner.nextLine().trim();

            if (isCancelKeyword(input)) {
                throw new ActionCancelledException();
            }

            if (input.isBlank()) {
                return currentMember.getMemberId();
            }

            if (!input.matches("[A-Za-z0-9]+")) {
                System.out.println("Invalid format - use letters and numbers only.");
                return resolveMemberId(prompt);
            }

            return input;
        }

        return readValidId(prompt);
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
        printBanner(BANNER_MAIN);
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
            printBanner(BANNER_MEMBER);
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
        printBanner(BANNER_MEMBER);
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
    private void earnPoints() {

        clearScreen();
        printBanner(BANNER_MEMBER);
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

        clearScreen();
        printBanner(BANNER_MEMBER);
        printHeader("POINTS EARNED SUCCESSFULLY");

        double multiplier = loyaltyController.getTierMultiplier(tierBefore);

        System.out.println("POINTS CALCULATION");
        printDivider();

        System.out.printf("%-25s %s%n", "Member",
                after.getMemberId() + " - " + after.getName());
        System.out.printf("%-25s %s pts%n",
                "Previous Balance", formatPoints(previousPoints));

        System.out.println();

        System.out.println("                    " + formatPoints(points)
                + " pts x" + String.format("%.2f", multiplier));

        System.out.printf("%-25s +%s pts%n",
                "Points Awarded", formatPoints(awarded));

        printDivider();
        System.out.printf("%-25s %s pts%n", "New Balance", formatPoints(after.getPoints()));
        System.out.printf("%-25s %s%n", "Current Tier", after.getTier());

        String tierProgress = loyaltyController.getTierProgressDisplay(
                after.getTier(),
                after.getPoints()
        );

        String[] progressLines = tierProgress.split("\n");

        if (progressLines.length >= 4) {
            System.out.printf("%-25s %s%n", "Tier Progress", progressLines[0]);
            System.out.printf("%-25s %s%n", "", "[" + progressLines[2] + "]");
            System.out.printf("%-25s %s%n", "", progressLines[3]);
        } else {
            System.out.printf("%-25s %s%n", "Tier Progress", progressLines[0]);
        }

        ListInterface<PointsTransaction> historyAfter
                = loyaltyController.getTransactionHistoryByMember(id);

        if (!historyAfter.isEmpty()) {

            PointsTransaction latest
                    = historyAfter.getEntry(historyAfter.size());

            if (latest.getExpiryDate() != null) {

                System.out.printf(
                        "%-25s %s%n",
                        "Points Expiry",
                        latest.getExpiryDate()
                );
            }
        }

        printDivider();

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
                = loyaltyController.getMemberRank(member.getMemberId());

        int totalMembers
                = loyaltyController.getTotalMemberCount();

        clearScreen();
        printBanner(BANNER_MEMBER);
        printHeader("MEMBER DETAILS");

        System.out.println("MEMBER INFORMATION");
        printDivider();

        System.out.printf(
                "%-25s %-25s%n",
                "Field",
                "Details"
        );

        printDivider();

        System.out.printf(
                "%-25s %-25s%n",
                "Member ID",
                member.getMemberId()
        );

        System.out.printf(
                "%-25s %-25s%n",
                "Name",
                member.getName()
        );

        System.out.printf(
                "%-25s %-25s%n",
                "Membership Tier",
                member.getTier()
        );

        System.out.printf(
                "%-25s %-25s%n",
                "Points Balance",
                formatPoints(member.getPoints()) + " pts"
        );

        System.out.printf(
                "%-25s #%d of %d%n",
                "Member Ranking",
                rank,
                totalMembers
        );

        printDivider();

        System.out.println();
        System.out.println("TIER PROGRESS");
        printDivider();

        System.out.println(
                loyaltyController.getTierProgressDisplay(
                        member.getTier(),
                        member.getPoints()
                )
        );

        printDivider();

        System.out.println();
        System.out.println("RECENT ACTIVITY");
        printDivider();

        RewardRedemption recent
                = loyaltyController.getMostRecentRedemptionForMember(
                        member.getMemberId()
                );

        if (recent == null) {

            System.out.println("No recent redemption.");

        } else {

            Reward reward
                    = loyaltyController.findRewardById(
                            recent.getRewardId()
                    );

            String rewardName
                    = (reward != null)
                            ? reward.getRewardName()
                            : recent.getRewardId();

            System.out.printf(
                    "%-25s %s - %s%n",
                    "Last Redemption",
                    rewardName,
                    recent.getRedeemDate()
            );
        }

        printDivider();

        System.out.println();
        System.out.println("POINTS EXPIRY");
        printDivider();

        printExpiryAlerts(member.getMemberId());

        printDivider();
    }

    // Displays expiring and recently expired points.
    private void printExpiryAlerts(String memberId) {

        ListInterface<PointsTransaction> expiring
                = loyaltyController.getExpiringSoonTransactions(memberId);

        ListInterface<PointsTransaction> expired
                = loyaltyController.getRecentlyExpiredTransactions(memberId);

        System.out.println();
        System.out.println("POINTS EXPIRING SOON");

        if (expiring.isEmpty()) {

            System.out.println("No points expiring soon.");

        } else {

            int memberWidth = "Member".length();
            int pointsWidth = "Points".length();
            int expiryWidth = "Expires".length();
            int daysWidth = "Days Left".length();

            for (int i = 1; i <= expiring.size(); i++) {

                PointsTransaction t
                        = expiring.getEntry(i);

                memberWidth
                        = Math.max(
                                memberWidth,
                                t.getMemberId().length()
                        );

                pointsWidth
                        = Math.max(
                                pointsWidth,
                                formatPoints(
                                        t.getPointsChange()
                                ).length()
                        );

                expiryWidth
                        = Math.max(
                                expiryWidth,
                                String.valueOf(
                                        t.getExpiryDate()
                                ).length()
                        );

                daysWidth
                        = Math.max(
                                daysWidth,
                                (loyaltyController
                                        .getDaysUntilExpiry(t)
                                        + " days").length()
                        );
            }

            int tableWidth
                    = 4
                    + memberWidth
                    + 3
                    + pointsWidth
                    + 3
                    + expiryWidth
                    + 3
                    + daysWidth
                    + 3;

            System.out.println("-".repeat(tableWidth));

            System.out.printf(
                    "| %-" + memberWidth
                    + "s | %" + pointsWidth
                    + "s | %-" + expiryWidth
                    + "s | %-" + daysWidth
                    + "s |%n",
                    "Member",
                    "Points",
                    "Expires",
                    "Days Left"
            );

            System.out.println("-".repeat(tableWidth));

            for (int i = 1; i <= expiring.size(); i++) {

                PointsTransaction t
                        = expiring.getEntry(i);

                System.out.printf(
                        "| %-" + memberWidth
                        + "s | %" + pointsWidth
                        + "s | %-" + expiryWidth
                        + "s | %-" + daysWidth
                        + "s |%n",
                        t.getMemberId(),
                        formatPoints(t.getPointsChange()),
                        t.getExpiryDate(),
                        loyaltyController
                                .getDaysUntilExpiry(t)
                        + " days"
                );
            }

            System.out.println("-".repeat(tableWidth));
        }

        System.out.println();
        System.out.println("RECENTLY EXPIRED POINTS");

        if (expired.isEmpty()) {

            System.out.println("No recently expired points.");

        } else {

            int memberWidth = "Member".length();
            int pointsWidth = "Points".length();
            int dateWidth = "Expired".length();

            for (int i = 1; i <= expired.size(); i++) {

                PointsTransaction t
                        = expired.getEntry(i);

                memberWidth
                        = Math.max(
                                memberWidth,
                                t.getMemberId().length()
                        );

                pointsWidth
                        = Math.max(
                                pointsWidth,
                                formatPoints(
                                        -t.getPointsChange()
                                ).length()
                        );

                dateWidth
                        = Math.max(
                                dateWidth,
                                String.valueOf(
                                        t.getTransactionDate()
                                ).length()
                        );
            }

            int tableWidth
                    = 4
                    + memberWidth
                    + 3
                    + pointsWidth
                    + 3
                    + dateWidth
                    + 3;

            System.out.println("-".repeat(tableWidth));

            System.out.printf(
                    "| %-" + memberWidth
                    + "s | %" + pointsWidth
                    + "s | %-" + dateWidth
                    + "s |%n",
                    "Member",
                    "Points",
                    "Expired"
            );

            System.out.println("-".repeat(tableWidth));

            for (int i = 1; i <= expired.size(); i++) {

                PointsTransaction t
                        = expired.getEntry(i);

                System.out.printf(
                        "| %-" + memberWidth
                        + "s | %" + pointsWidth
                        + "s | %-" + dateWidth
                        + "s |%n",
                        t.getMemberId(),
                        formatPoints(-t.getPointsChange()),
                        t.getTransactionDate()
                );
            }

            System.out.println("-".repeat(tableWidth));
        }
    }

    // Displays and selects a member.
    private void viewAndSelectMember() {

        clearScreen();
        printBanner(BANNER_MEMBER);
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
                printBanner(BANNER_MEMBER);
                printHeader("SELECT MEMBER");

                System.out.println(
                        "Multiple members found:"
                );

                System.out.println();

                printMemberTable(matches, false);

                printDivider();

                String selectedId
                        = readValidId(
                                "Enter Member ID to select: "
                        );

                found
                        = loyaltyController.searchMemberById(
                                selectedId
                        );

                if (found == null) {

                    System.out.println();
                    System.out.println(
                            "[!] Member not found."
                    );

                    pressEnterToContinue();
                    return;
                }
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
        printBanner(BANNER_MEMBER);
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
        printBanner(BANNER_MEMBER);
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
                printBanner(BANNER_MEMBER);
                printHeader("SELECT MEMBER");

                System.out.println(
                        "Multiple members found:"
                );

                printDivider();

                printMemberTable(matches, false);

                printDivider();

                String selectedId
                        = readValidId(
                                "Enter Member ID to select: "
                        );

                found
                        = loyaltyController.searchMemberById(
                                selectedId
                        );

                if (found == null) {
                    System.out.println();
                    System.out.println(
                            "Member not found."
                    );
                    pressEnterToContinue();
                    return null;
                }
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
        printBanner(BANNER_MEMBER);

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
        printBanner(BANNER_MEMBER);

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
        printBanner(BANNER_REGISTER);
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

        System.out.println(
                registered.getMemberId()
                + " | "
                + registered.getName()
                + " | "
                + registered.getTier()
                + " | "
                + formatPoints(registered.getPoints())
                + " pts"
        );

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
        printBanner(BANNER_MEMBER);

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
            printBanner(BANNER_REWARDS);
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

    // Displays rewards in a table.
    private void printRewardTable(ListInterface<Reward> rewards) {

        if (rewards.isEmpty()) {
            System.out.println("No rewards found.");
            return;
        }

        System.out.printf(
                "%-8s %-25s %-15s %-15s %-8s %-10s%n",
                "ID",
                "Reward",
                "Category",
                "Points Required",
                "Qty",
                "Status"
        );

        printDivider();

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

            String rewardName = r.getRewardName();

            if (rewardName.length() > 25) {
                rewardName
                        = rewardName.substring(0, 22) + "...";
            }

            System.out.printf(
                    "%-8s %-25s %-15s %-15d %-8d %-10s%n",
                    r.getRewardId(),
                    rewardName,
                    r.getCategory(),
                    r.getPointsRequired(),
                    r.getQuantity(),
                    status
            );
        }
    }

    // Browses, searches, and sorts rewards.
    private void browseSearchSortRewards() {

        ListInterface<Reward> currentView
                = loyaltyController.viewRewards();

        boolean browsing = true;

        while (browsing) {

            clearScreen();
            printBanner(BANNER_REWARDS);
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

    // Displays the redemption receipt.
    private void printRedemptionReceipt(String redemptionId, Member memberBeforeRedemption,
            Member updatedMember, Reward updatedReward) {
        int remainingPoints = (updatedMember != null) ? updatedMember.getPoints() : 0;
        int remainingStock = (updatedReward != null) ? updatedReward.getQuantity() : 0;

        System.out.println();
        System.out.println("==============================");
        System.out.println("     REDEMPTION SUCCESSFUL     ");
        System.out.println("==============================");
        System.out.println("Receipt No       : " + redemptionId);
        System.out.println("Member           : " + memberBeforeRedemption.getName());
        System.out.println("Reward           : " + (updatedReward != null ? updatedReward.getRewardName() : "-"));
        System.out.println("Points Used      : " + formatPoints(updatedReward != null
                ? (memberBeforeRedemption.getPoints() - remainingPoints) : 0));
        System.out.println("Remaining Points : " + formatPoints(remainingPoints));
        System.out.println("Remaining Stock  : " + remainingStock);
        System.out.println();
        System.out.println("         Thank You!");
        System.out.println("==============================");
    }

    // Undoes the latest reward redemption.
    private void undoLastRedemption() {

        clearScreen();
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
            System.out.println("Redemption ID   : " + last.getRedemptionId());
            System.out.println("Refunded Points : " + formatPoints(last.getRedeemedPoints()));

            if (memberAfter != null) {
                System.out.println("New Balance     : " + formatPoints(memberAfter.getPoints()));
            }
            if (rewardAfter != null) {
                System.out.println("Stock Restored  : " + rewardAfter.getQuantity());
            }

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
        printBanner(BANNER_REWARDS);
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

                memberId = readValidId("Enter Member ID: ");

                if (loyaltyController.searchMemberById(memberId) == null) {
                    System.out.println();
                    System.out.println("Member not found: " + memberId);
                    pressEnterToContinue();
                    return;
                }

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
        printBanner(BANNER_REWARDS);

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
// Calculates the reward column width.

    private int getRedemptionRewardWidth(
            ListInterface<RewardRedemption> history) {

        int width = "Reward".length();

        for (int i = 1; i <= history.size(); i++) {

            RewardRedemption r = history.getEntry(i);

            Reward reward
                    = loyaltyController.findRewardById(
                            r.getRewardId()
                    );

            String rewardName
                    = reward != null
                            ? reward.getRewardName()
                            : r.getRewardId();

            rewardName
                    = rewardName
                            .replace("\r", " ")
                            .replace("\n", " ")
                            .trim();

            width = Math.max(width, rewardName.length());
        }

        return width;
    }
// Calculates the redemption table width.

    private int getRedemptionTableWidth(
            ListInterface<RewardRedemption> history,
            boolean showMember) {

        int rewardWidth
                = getRedemptionRewardWidth(history);

        int width
                = 4
                + 10
                + 3
                + rewardWidth
                + 3
                + 8
                + 3
                + 10
                + 3;

        if (showMember) {
            width += 6 + 3;
        }

        return width;
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
// Displays redemption records in a table.

    private void printRedemptionTable(
            ListInterface<RewardRedemption> history,
            boolean showMember) {

        final int redemptionWidth = 10;
        final int memberWidth = 6;
        final int dateWidth = 10;
        final int pointsWidth = 8;

        final int rewardWidth
                = getRedemptionRewardWidth(history);

        final int tableWidth
                = getRedemptionTableWidth(
                        history,
                        showMember
                );

        System.out.println("-".repeat(tableWidth));

        if (showMember) {

            System.out.printf(
                    "| %-" + redemptionWidth + "s | %-"
                    + memberWidth + "s | %-"
                    + rewardWidth + "s | %"
                    + pointsWidth + "s | %-"
                    + dateWidth + "s |%n",
                    "Redemp ID",
                    "Member",
                    "Reward",
                    "Points",
                    "Date"
            );

        } else {

            System.out.printf(
                    "| %-" + redemptionWidth + "s | %-"
                    + rewardWidth + "s | %"
                    + pointsWidth + "s | %-"
                    + dateWidth + "s |%n",
                    "Redemp ID",
                    "Reward",
                    "Points",
                    "Date"
            );
        }

        System.out.println("-".repeat(tableWidth));

        for (int i = 1; i <= history.size(); i++) {

            RewardRedemption r
                    = history.getEntry(i);

            Reward reward
                    = loyaltyController.findRewardById(
                            r.getRewardId()
                    );

            String rewardName
                    = reward != null
                            ? reward.getRewardName()
                            : r.getRewardId();

            rewardName
                    = rewardName
                            .replace("\r", " ")
                            .replace("\n", " ")
                            .trim();

            if (showMember) {

                System.out.printf(
                        "| %-" + redemptionWidth
                        + "s | %-" + memberWidth
                        + "s | %-" + rewardWidth
                        + "s | %" + pointsWidth
                        + "d | %-" + dateWidth
                        + "s |%n",
                        r.getRedemptionId(),
                        r.getMemberId(),
                        rewardName,
                        r.getRedeemedPoints(),
                        r.getRedeemDate()
                );

            } else {

                System.out.printf(
                        "| %-" + redemptionWidth
                        + "s | %-" + rewardWidth
                        + "s | %" + pointsWidth
                        + "d | %-" + dateWidth
                        + "s |%n",
                        r.getRedemptionId(),
                        rewardName,
                        r.getRedeemedPoints(),
                        r.getRedeemDate()
                );
            }
        }

        System.out.println("-".repeat(tableWidth));
    }

    // Adds a new reward.
    private void addReward() {

        clearScreen();
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

        System.out.printf(
                "%s | %s | %s | %d pts | Qty %d%n",
                added.getRewardId(),
                added.getRewardName(),
                added.getCategory(),
                added.getPointsRequired(),
                added.getQuantity()
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

    // Restocks an existing reward.
    private void restockReward() {

        clearScreen();
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
        System.out.printf(
                "%-20s %s%n",
                "Reward",
                reward.getRewardName()
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
            printBanner(BANNER_TIERS);
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
        printBanner(BANNER_TIERS);
        printHeader("TIER PROGRESS");
        printCurrentMemberBanner();
        printBackHint();

        String id;

        if (currentMember == null) {

            System.out.println();
            System.out.println("No current member selected.");
            System.out.println("Please enter a Member ID to view tier progress.");
            System.out.println();

            id = readValidId("Member ID: ");

        } else {

            id = resolveMemberId(
                    "Member ID (blank = current): "
            );
        }

        Member member
                = loyaltyController.searchMemberById(id);

        if (member == null) {
            System.out.println();
            System.out.println("Member not found.");
            pressEnterToContinue();
            return;
        }

        if (currentMember != null
                && currentMember.getMemberId().equals(member.getMemberId())) {
            setCurrentMember(member);
        }

        System.out.println();
        System.out.println("MEMBER");
        printDivider();
        System.out.println(memberSummaryLine(member));

        System.out.println();
        System.out.println("TIER PROGRESS");
        printDivider();
        System.out.println(
                loyaltyController.getTierProgressDisplay(
                        member.getTier(), member.getPoints())
        );
        printDivider();

        pressEnterToContinue();
    }

    // Displays the notification centre.
    private void notificationCentre() {

        clearScreen();
        printHeader("NOTIFICATIONS");

        loyaltyController.processExpiredPoints();

        ListInterface<PointsTransaction> expiring
                = loyaltyController.getExpiringSoonTransactions(null);

        ListInterface<PointsTransaction> expired
                = loyaltyController.getRecentlyExpiredTransactions(null);

        ListInterface<Reward> lowStock
                = loyaltyController.getLowStockRewards();

        System.out.println();
        System.out.println("NOTIFICATION SUMMARY");
        printDivider();

        if (expiring.isEmpty()
                && expired.isEmpty()
                && lowStock.isEmpty()) {

            System.out.println("[OK] No active notifications.");
            System.out.println("Everything looks good.");

            printDivider();
            pressEnterToContinue();
            return;
        }

        if (!expiring.isEmpty()) {
            System.out.println(
                    "[!] " + expiring.size()
                    + " point record(s) expiring soon."
            );
        }

        if (!expired.isEmpty()) {
            System.out.println(
                    "[!] " + expired.size()
                    + " recently expired point record(s)."
            );
        }

        if (!lowStock.isEmpty()) {
            System.out.println(
                    "[!] " + lowStock.size()
                    + " reward(s) low in stock."
            );
        }

        printDivider();

        if (!expiring.isEmpty() || !expired.isEmpty()) {
            System.out.println();
            System.out.println("POINTS EXPIRY");
            printExpiryAlerts(null);
        }

        if (!lowStock.isEmpty()) {

            System.out.println();
            System.out.println("LOW STOCK REWARDS");
            printDivider();

            System.out.printf(
                    "%-35s %-12s%n",
                    "Reward",
                    "Remaining"
            );

            printDivider();

            for (int i = 1; i <= lowStock.size(); i++) {

                Reward reward
                        = lowStock.getEntry(i);

                System.out.printf(
                        "%-35s %-12d%n",
                        reward.getRewardName(),
                        reward.getQuantity()
                );
            }

            printDivider();

            System.out.println(
                    "Tip: Use Rewards > Restock Reward to add stock."
            );
        }

        pressEnterToContinue();
    }

    // Simulates a membership tier upgrade.
    private void simulateTierUpgrade() {

        clearScreen();
        printBanner(BANNER_TIERS);
        printHeader("SIMULATE TIER PROGRESS");
        printCurrentMemberBanner();
        printBackHint();

        String id = resolveMemberId("Member ID                : ");
        Member member = loyaltyController.searchMemberById(id);
        if (member == null) {
            System.out.println("\nMember not found.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.println("Current Tier   : " + member.getTier());
        System.out.println("Current Points : " + formatPoints(member.getPoints()));

        int hypothetical = readNonNegativeInt("\nSimulate earning how many points (0 to skip): ");

        if (hypothetical > 0) {
            int simulatedPoints = member.getPoints() + hypothetical;
            String simulatedTier = loyaltyController.calculateTier(simulatedPoints);

            System.out.println();
            System.out.println("If you earn " + hypothetical + " more points:");
            System.out.println("  New Points : " + formatPoints(simulatedPoints));
            System.out.println("  New Tier   : " + simulatedTier
                    + (simulatedTier.equals(member.getTier()) ? " (no change)" : " (upgrade!)"));
        }

        pressEnterToContinue();
    }

    // Displays the tier distribution report.
    private void tierDistributionReport() {

        clearScreen();
        printBanner(BANNER_TIERS);
        printHeader("TIER DISTRIBUTION");

        showLoading("Calculating tier distribution...");
        int[] counts = loyaltyController.membershipTierDistribution();
        String[] tiers = {"Silver", "Gold", "Elite", "Diamond", "Platinum"};

        int totalMembers = 0;
        for (int count : counts) {
            totalMembers += count;
        }

        if (totalMembers == 0) {
            System.out.println("\nNo members found.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.printf("%-10s %-9s %-6s  %s%n", "Tier", "Members", "%", "Chart");
        printDivider();

        int barMaxWidth = 20;

        for (int i = 0; i < tiers.length; i++) {
            int percent = (int) Math.round((double) counts[i] / totalMembers * 100);
            int barLength = (int) Math.round((double) counts[i] / totalMembers * barMaxWidth);

            String bar = "#".repeat(Math.max(barLength, 0)) + "-".repeat(barMaxWidth - barLength);

            System.out.printf("%-10s %-9d %-6s  %s%n", tiers[i], counts[i], percent + "%", bar);
        }

        pressEnterToContinue();
    }

    // Runs the reports and management menu.
    private void reportsManagementMenu() {

        boolean running = true;

        while (running) {

            clearScreen();
            printBanner(BANNER_REPORTS);
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

    // Displays the top members report.
    private void topMembersReport() {

        clearScreen();
        printBanner(BANNER_REPORTS);
        printHeader("MEMBER LOYALTY REPORT");
        printBackHint();

        System.out.println("Filter by Tier (blank = all): Silver / Gold / Elite / Diamond / Platinum");

        String tierFilter = readOptionalTier(
                "Tier             : ");

        int minPoints = readNonNegativeInt(
                "Minimum Points (0 = no minimum) : ");

        int maxPoints = readOptionalNonNegativeInt(
                "Maximum Points (blank = no maximum) : ");

        if (maxPoints != Integer.MAX_VALUE && minPoints > maxPoints) {
            System.out.println(
                    "\nMaximum Points must be greater than or equal to Minimum Points.");
            pressEnterToContinue();
            return;
        }

        showLoading("Filtering and sorting members...");

        ListInterface<Member> report = loyaltyController.getMemberLoyaltyReport(tierFilter, minPoints, maxPoints);

        System.out.println();
        System.out.println("Filter : " + (tierFilter.isBlank() ? "All Tiers" : tierFilter)
                + ", Points " + minPoints + "-" + (maxPoints == Integer.MAX_VALUE ? "Unlimited" : maxPoints));
        System.out.println("Sort   : Points Descending");
        System.out.println();

        if (report.isEmpty()) {
            System.out.println("No members match this filter.");
        } else {
            printMemberTable(report, true);
        }

        pressEnterToContinue();
    }

    // Displays redemption statistics.
    private void redemptionStatisticsReport() {

        clearScreen();
        printBanner(BANNER_REPORTS);
        printHeader("REDEMPTION ANALYSIS");
        printBackHint();

        System.out.println(
                "Filter by Category (blank = all): Dining / Spa / Room / Others");

        String categoryFilter
                = readOptionalCategory("Category         : ");

        showLoading("Filtering and counting redemptions...");

        int total = loyaltyController.getTotalRedemptions();
        ListInterface<LoyaltyController.CategoryCount> stats
                = loyaltyController.getRedemptionStatsByCategory(categoryFilter);

        int filteredTotal = 0;
        for (int i = 1; i <= stats.size(); i++) {
            filteredTotal += stats.getEntry(i).count;
        }

        if (stats.isEmpty() || filteredTotal == 0) {
            System.out.println("\nNo redemption records match this filter.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.println("Filter          : " + (categoryFilter.isBlank() ? "All Categories" : categoryFilter));
        System.out.println("Matching Total  : " + filteredTotal + " (of " + total + " overall)");
        System.out.println();

        System.out.printf("%-12s %-7s %-6s  %s%n", "Category", "Count", "%", "Chart");
        printDivider();

        int barMaxWidth = 20;

        for (int i = 1; i <= stats.size(); i++) {
            LoyaltyController.CategoryCount c = stats.getEntry(i);

            int percent = (int) Math.round((double) c.count / filteredTotal * 100);
            int barLength = (int) Math.round((double) c.count / filteredTotal * barMaxWidth);

            String bar = "#".repeat(Math.max(barLength, 0)) + "-".repeat(barMaxWidth - barLength);

            System.out.printf("%-12s %-7d %-6s  %s%n", c.category, c.count, percent + "%", bar);
        }

        pressEnterToContinue();
    }

    // Displays reward popularity statistics.
    private void rewardPopularityReport() {

        clearScreen();
        printBanner(BANNER_REPORTS);
        printHeader("REWARD POPULARITY CHART");

        showLoading("Calculating popularity...");

        int total = loyaltyController.getTotalRedemptions();
        ListInterface<RewardPopularity> stats = loyaltyController.getRewardPopularity();

        if (stats.isEmpty() || total == 0) {
            System.out.println("\nNo redemption records yet.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.printf("%-25s %-7s %-6s  %s%n", "Reward", "Count", "%", "Chart");
        printDivider();

        int barMaxWidth = 20;

        for (int i = 1; i <= stats.size(); i++) {
            RewardPopularity p = stats.getEntry(i);
            int percent = (int) Math.round((double) p.count / total * 100);
            int barLength = (int) Math.round((double) p.count / total * barMaxWidth);

            String bar = "#".repeat(Math.max(barLength, 0)) + "-".repeat(barMaxWidth - barLength);

            System.out.printf("%-25s %-7d %-6s  %s%n", p.rewardName, p.count, percent + "%", bar);
        }

        pressEnterToContinue();
    }

    // Displays the member ranking report.
    private void memberRankingReport() {

        clearScreen();
        printBanner(BANNER_REPORTS);
        printHeader("MEMBER RANKING");

        showLoading("Calculating rankings...");

        int total = loyaltyController.getTotalMemberCount();
        ListInterface<Member> ranked = loyaltyController.topMembersByPoints(total);

        System.out.println();
        printMemberTable(ranked, true);

        pressEnterToContinue();
    }

    // Displays system statistics.
    private void systemStatisticsDashboard() {

        clearScreen();
        printBanner(BANNER_REPORTS);
        printHeader("SYSTEM STATISTICS");

        int[] tierCounts
                = loyaltyController.membershipTierDistribution();

        showLoading("Gathering system statistics...");

        System.out.println();

        System.out.printf(
                "%-30s %-20s%n",
                "Metric",
                "Value"
        );

        printDivider();

        System.out.printf(
                "%-30s %-20d%n",
                "Total Members",
                loyaltyController.getTotalMemberCount()
        );

        System.out.printf(
                "%-30s %-20d%n",
                "Reward Types",
                loyaltyController.getTotalRewardTypes()
        );

        System.out.printf(
                "%-30s %-20s%n",
                "Total Points",
                formatPoints(
                        loyaltyController.getTotalPointsAcrossMembers()
                )
        );

        System.out.printf(
                "%-30s %-20d%n",
                "Rewards Redeemed",
                loyaltyController.getTotalRedemptions()
        );

        System.out.printf(
                "%-30s %-20d%n",
                "Transactions Today",
                loyaltyController.getTransactionsTodayCount()
        );

        System.out.printf(
                "%-30s %-20s%n",
                "Most Popular Category",
                loyaltyController.getMostPopularCategory()
        );

        System.out.printf(
                "%-30s %-20d%n",
                "Platinum Members",
                tierCounts[4]
        );

        printDivider();

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

    // Displays a controller result.
    private void printResult(ControllerResult result) {

        System.out.println();

        if (result.isOk()) {
            System.out.println("----------------------------------------");
            System.out.println("Operation completed successfully.");
            if (result.getMessage() != null) {
                System.out.println(result.getMessage());
            }
            System.out.println("----------------------------------------");
        } else {
            System.out.println("----------------------------------------");
            System.out.println("Operation failed.");
            System.out.println(result.getMessage());
            System.out.println("----------------------------------------");
        }
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
