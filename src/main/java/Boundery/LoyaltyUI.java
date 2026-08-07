package Boundery;

import ADT.ListInterface;
import Control.LoyaltyController;
import Control.LoyaltyController.CategoryCount;
import Control.LoyaltyController.RewardPopularity;
import Control.MemberController;
import Entity.Member;
import Entity.PointsTransaction;
import Entity.Reward;
import Entity.RewardRedemption;
import Utility.ControllerResult;

import java.util.Scanner;

/**
 * LoyaltyUI.java
 * Console menu for the Loyalty & Rewards Service.
 *
 * Structure follows the module flowchart exactly - 4 top-level functions:
 *   1. Manage Reward Points
 *   2. Register            (enroll a member into the Loyalty program)
 *   3. Redeem Reward
 *   4. Generate Reports
 *
 * NOTE: There is intentionally no standalone "Member Management" menu
 * (Update/Delete Member). Per the flowchart, those aren't part of this
 * module's function set - basic Member record upkeep belongs to whichever
 * module owns guest/member creation (e.g. Walk-In Registration). Loyalty
 * only enrolls, tracks points/tier, and reports on existing Members.
 */
public class LoyaltyUI {

    private final Scanner scanner;
    private final LoyaltyController loyaltyController;

    public LoyaltyUI() {
        this.scanner = new Scanner(System.in);
        MemberController memberController = new MemberController();
        this.loyaltyController = new LoyaltyController(memberController);
    }

    // Allows MainMenuUI (or any shared menu) to pass in an existing
    // MemberController instead of this class creating its own.
    public LoyaltyUI(MemberController sharedMemberController) {
        this.scanner = new Scanner(System.in);
        this.loyaltyController = new LoyaltyController(sharedMemberController);
    }

    // ───────────────────── Banners (Diet Cola font, patorjk.com/software/taag) ─────────────────────
    // Main "REWARDS" banner - already generated.
    private static final String[] BANNER_MAIN = {
        "          .-.                             /\\         .-.  .--------'       ",
        "         / (_)    .--.    .-.-.   .-  _  / |        / (_)(_)   /  .-.   .- ",
        "        /        /    )`-'    /  (   (  /  |  .    /          /     /  (   ",
        "       /        /    /       (    )   `/.__|_.'   /          /     (    )  ",
        "    .-/.    .-.(    /      .  `..'.:' /    |   .-/.    .-..-/._  .  `..'   ",
        "   (_/ `-._.    `-.'      (__.-' (__.'     `-'(_/ `-._.  (_/  `-(__.-'     "
    };

    // Each submenu below gets its own Diet Cola banner (patorjk.com/software/taag).
    private static final String[] BANNER_MANAGE_POINTS = {
        "      .-.                .-                /\\     .-.          .-.        ",
        "     (_) )-.     .---;`-'..-.     .-.   _  / |    (_) )-.      (_) )-.     ",
        "        /   \\   (   (_)     )   (     (  /  |  .    /   \\        /   \\    ",
        "       /     )   )--       /     \\     `/.__|_.'   /     )      /     \\   ",
        "    .-/  `--'   (      /  (   .   ).:' /    |   .-/  `--'    .-/.      )  ",
        "   (_/     `-._)`\\___.'    `-' `-'(__.'     `-'(_/     `-._)(_/  `----'   ",
        "          .-.               .----.     .-.     .--------'     .-.         ",
        "         (_) )-.    .--.    .-/   `      /  | (_)   /   .--.-'            ",
        "            /   \\  /    )`-' /          /\\  |      /   (  (_)             ",
        "           /     )/    /    /          /  \\ |     /     `-.               ",
        "        .-/  `--'(    /    /      .-' /    \\|  .-/._  _    )              ",
        "       (_/        `-.'.---------'(__.'      `.(_/  `-(_.--'               "
    };

    private static final String[] BANNER_REGISTER = {
        "      .-.                .-       .-. .----.        .-..--------'     .-.-.        ",
        "     (_) )-.     .---;`-'  .--.`-'      /   ` .--.-'  (_)   / .---;`-' (_) )-.     ",
        "        /   \\   (   (_)   /  (_;       /     (  (_)        / (   (_)      /   \\    ",
        "       /     )   )--     /            /       `-.         /   )--        /     )   ",
        "    .-/  `--'   (      /(     --;-   /      _    )     .-/._ (      / .-/  `--'    ",
        "   (_/     `-._)`\\___.'  `.___.'.---------'(_.--'     (_/  `-`\\___.' (_/     `-._) "
    };

    private static final String[] BANNER_REDEEM = {
        "      .-.                .-.-.               .-      .-  .-.                       ",
        "     (_) )-.     .---;`-' (_) )-.    .---;`-'.---;`-'      /|/|                    ",
        "        /   \\   (   (_)      /   \\  (   (_) (   (_)       /   |                    ",
        "       /     )   )--        /     \\  )--     )--         /    |                    ",
        "    .-/  `--'   (      / .-/.      )(      /(      /.-' /     |                    ",
        "   (_/     `-._)`\\___.' (_/  `----' `\\___.' `\\___.'(__.'      `.                   ",
        "                .-.                .-                /\\     .-.          .-.       ",
        "               (_) )-.     .---;`-'..-.     .-.   _  / |    (_) )-.      (_) )-.    ",
        "                  /   \\   (   (_)     )   (     (  /  |  .    /   \\        /   \\   ",
        "                 /     )   )--       /     \\     `/.__|_.'   /     )      /     \\  ",
        "              .-/  `--'   (      /  (   .   ).:' /    |   .-/  `--'    .-/.      ) ",
        "             (_/     `-._)`\\___.'    `-' `-'(__.'     `-'(_/     `-._)(_/  `----'  "
    };

    private static final String[] BANNER_REPORTS = {
        "      .-.                .-.-.                  .-.        .--------'     .-. ",
        "     (_) )-.     .---;`-' (_) )-.    .--.    .-(_) )-.    (_)   /   .--.-'    ",
        "        /   \\   (   (_)      /   \\  /    )`-'     /   \\        /   (  (_)     ",
        "       /     )   )--        /     )/    /        /     )      /     `-.       ",
        "    .-/  `--'   (      / .-/  `--'(    /      .-/  `--'    .-/._  _    )      ",
        "   (_/     `-._)`\\___.' (_/        `-.'      (_/     `-._)(_/  `-(_.--'       "
    };

    private void printBanner(String[] banner) {
        if (banner == null || banner.length == 0) {
            return;
        }
        System.out.println();
        for (String line : banner) {
            System.out.println(line);
        }
    }

    // ───────────────────── UX Helpers ─────────────────────

    /**
     * "Clears" the console. NetBeans' built-in Output panel is NOT a real
     * terminal - it has no API to actually erase previous output, and it
     * ignores "cls"/"clear" system commands and ANSI escape codes entirely.
     * The one thing that reliably works there is pushing enough blank
     * lines through that the old content scrolls out of view, which is
     * what this does. If you run the built .jar from an actual external
     * terminal (not NetBeans' Run button), a real terminal clear would
     * also be possible, but there's no need to add that complexity here
     * since NetBeans is what actually matters for your demo/screenshots.
     */
    private void clearScreen() {
        for (int i = 0; i < 60; i++) {
            System.out.println();
        }
    }

    /**
     * "Loading" animation that adapts to where it's actually running.
     *
     * System.console() returns null whenever standard output is being
     * captured rather than connected to a real terminal - which is exactly
     * what NetBeans' Output panel does. It returns a real object when the
     * program is run from an actual terminal window (cmd, PowerShell,
     * Terminal.app, etc). So:
     *
     *   - Real terminal  -> classic |/-\ spinner using "\r" to overwrite in place
     *   - NetBeans Output panel (or anywhere else output is redirected)
     *     -> falls back to appended dots, since "\r" doesn't work there
     *
     * Either way it runs for about 3 seconds.
     */
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

    /**
     * Asks a yes/no question before a significant action (redeeming,
     * undoing). Anything other than an explicit "Y" is treated as "no",
     * so accidental Enter-presses don't confirm by default.
     */
    private boolean confirmAction(String message) {
        System.out.print(message + " (Y/N): ");
        String input = scanner.nextLine().trim();
        return input.equalsIgnoreCase("Y");
    }

    /**
     * Reads an ID-style field, re-prompting until it's letters/numbers only
     * (no spaces or symbols) - prevents malformed IDs breaking the CSV
     * storage format used by FileUtility.
     */
    private String readValidId(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input.matches("[A-Za-z0-9]+")) {
                return input;
            }
            System.out.println("Invalid format - use letters and numbers only, no spaces or symbols.");
        }
    }

    /**
     * Reads a name-style field, re-prompting until it contains only
     * letters and spaces.
     */
    private String readValidName(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input.matches("[A-Za-z ]+")) {
                return input;
            }
            System.out.println("Invalid format - letters and spaces only.");
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("Value must be greater than 0.");
        }
    }

    private int readNonNegativeInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value >= 0) {
                return value;
            }
            System.out.println("Value cannot be negative.");
        }
    }

    /**
     * Numbered category picker for rewards - keeps category names
     * consistent (no "Spa" vs "spa" vs "SPA" fragmenting the stats report).
     */
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

    /**
     * Reads text that's allowed to be letters, numbers, and spaces -
     * more permissive than readValidName, for fields like reward names
     * that might include a number (e.g. "2-Night Stay" without symbols).
     */
    private String readValidText(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input.matches("[A-Za-z0-9 ]+")) {
                return input;
            }
            System.out.println("Invalid format - letters, numbers, and spaces only.");
        }
    }

    /**
     * Like readValidId, but blank input is also accepted - for optional
     * ID filters (e.g. "leave blank to show all").
     */
    private String readOptionalValidId(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input.isBlank() || input.matches("[A-Za-z0-9]+")) {
                return input;
            }
            System.out.println("Invalid format - use letters and numbers only, or leave blank.");
        }
    }

    /**
     * Requires non-blank text but otherwise allows anything - for loose
     * keyword-style searches where restricting characters isn't useful.
     */
    private String readNonEmptyString(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (!input.isBlank()) {
                return input;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    // ───────────────────── Main Menu ─────────────────────

    public void run() {

        printNotificationCentre();

        boolean running = true;

        while (running) {

            printMainMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    manageRewardPointsMenu();
                    break;

                case "2":
                    registerLoyaltyMember();
                    break;

                case "3":
                    redeemRewardMenu();
                    break;

                case "4":
                    generateReportsMenu();
                    break;

                case "0":
                    running = false;
                    break;

                default:
                    System.out.println("\nInvalid option. Please try again.\n");
            }
        }
    }

    private void printMainMenu() {

        clearScreen();
        printBanner(BANNER_MAIN);

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                          LOYALTY MANAGEMENT                                  \n" +
                "------------------------------------------------------------------------------\n" +

                "                         1. Manage Reward Points                              \n" +
                "                         2. Register                                          \n" +
                "                         3. Redeem Reward                                      \n" +
                "                         4. Generate Reports                                   \n" +
                "                         0. Return to Main Menu                                \n" +

                "------------------------------------------------------------------------------\n" +
                "Enter your choice: "
        );
    }

    // ───────────────────── 1. Manage Reward Points ─────────────────────

    private void manageRewardPointsMenu() {

        boolean running = true;

        while (running) {

            clearScreen();
            printBanner(BANNER_MANAGE_POINTS);

            System.out.print(

                    "\n" +
                    "------------------------------------------------------------------------------\n" +
                    "                           REWARD POINTS                                     \n" +
                    "------------------------------------------------------------------------------\n" +

                    "                         1. Earn Reward Points                               \n" +
                    "                         2. Member Dashboard                                \n" +
                    "                         3. Transaction History                             \n" +
                    "                         4. Tier Benefits & Upgrade Preview                  \n" +
                    "                         0. Back                                              \n" +

                    "------------------------------------------------------------------------------\n" +
                    "Enter your choice: "
            );

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    earnPoints();
                    break;

                case "2":
                    checkPointsBalance();
                    break;

                case "3":
                    transactionHistory();
                    break;

                case "4":
                    tierBenefitsAndPreview();
                    break;

                case "0":
                    running = false;
                    break;

                default:
                    System.out.println("\nInvalid option. Please try again.\n");
            }
        }
    }

    private void earnPoints() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                         EARN REWARD POINTS                                  \n" +
                "------------------------------------------------------------------------------\n"
        );

        String id = readValidId("Member ID                : ");
        int points = readPositiveInt("Points Earned            : ");

        showLoading("Processing...");
        ControllerResult result = loyaltyController.earnPoints(id, points);

        printResult(result);
        pressEnterToContinue();
    }

    private void checkPointsBalance() {

        System.out.print(

                "\n" +
                "==================================================\n" +
                "             LOYALTY MEMBER DASHBOARD              \n" +
                "==================================================\n"
        );

        String id = readValidId("Member ID                : ");

        showLoading("Loading dashboard...");
        Member member = loyaltyController.searchMemberById(id);

        if (member == null) {
            System.out.println("\nMember not found.");
            pressEnterToContinue();
            return;
        }

        int rank = loyaltyController.getMemberRank(id);
        int totalMembers = loyaltyController.getTotalMemberCount();
        int percentile = (totalMembers == 0) ? 0 : (int) Math.round((double) rank / totalMembers * 100);

        System.out.println();
        System.out.println("Member ID   : " + member.getMemberId());
        System.out.println("Name        : " + member.getName());
        System.out.println("Tier        : " + member.getTier());
        System.out.println("Points      : " + member.getPoints());

        System.out.println();
        System.out.println(loyaltyController.getTierProgressDisplay(member.getTier(), member.getPoints()));

        System.out.println();
        System.out.println("Rank        : #" + rank + " of " + totalMembers + " Members (Top " + percentile + "%)");
        System.out.println("Rewards Available (in stock) : " + loyaltyController.getAvailableRewardCount());

        RewardRedemption recent = loyaltyController.getMostRecentRedemptionForMember(id);
        System.out.println();
        System.out.println("Recent Redemption");
        if (recent == null) {
            System.out.println("- No redemptions yet.");
        } else {
            Reward recentReward = loyaltyController.findRewardById(recent.getRewardId());
            String rewardName = (recentReward != null) ? recentReward.getRewardName() : recent.getRewardId();
            System.out.println("- " + rewardName + " (" + recent.getRedeemDate() + ")");
        }

        ListInterface<Reward> affordable = loyaltyController.getAffordableRewards(member.getPoints());
        System.out.println();
        System.out.println("Recommended Rewards");
        System.out.println("-----------------------------------------");
        if (affordable.isEmpty()) {
            System.out.println("No rewards available at your current point balance.");
        } else {
            for (int i = 1; i <= affordable.size(); i++) {
                Reward r = affordable.getEntry(i);
                System.out.printf("%-6s %-25s %d pts%n", r.getRewardId(), r.getRewardName(), r.getPointsRequired());
            }
        }

        System.out.println("==================================================");

        pressEnterToContinue();
    }

    private void transactionHistory() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                          TRANSACTION HISTORY                                \n" +
                "------------------------------------------------------------------------------\n"
        );

        String id = readOptionalValidId("Member ID (blank = all members): ");

        showLoading("Loading transaction history...");

        if (id.isBlank()) {
            ListInterface<PointsTransaction> all = loyaltyController.getAllTransactions();

            System.out.println();
            System.out.println("Showing full transaction ledger (" + all.size() + " total)");

            if (all.isEmpty()) {
                System.out.println("\nNo transactions yet.");
            } else {
                for (int i = 1; i <= all.size(); i++) {
                    PointsTransaction t = all.getEntry(i);
                    System.out.println();
                    System.out.println("Member : " + t.getMemberId());
                    System.out.println(t);
                }
            }
        } else {
            Member member = loyaltyController.searchMemberById(id);
            if (member == null) {
                System.out.println("\nMember not found.");
                pressEnterToContinue();
                return;
            }

            ListInterface<PointsTransaction> history = loyaltyController.getTransactionHistoryByMember(id);

            System.out.println();
            System.out.println("Member : " + member.getName());

            if (history.isEmpty()) {
                System.out.println("\nNo transactions yet.");
            } else {
                for (int i = 1; i <= history.size(); i++) {
                    System.out.println();
                    System.out.println(history.getEntry(i));
                }
            }
        }

        pressEnterToContinue();
    }

    private void tierBenefitsAndPreview() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                   TIER BENEFITS & UPGRADE PREVIEW                           \n" +
                "------------------------------------------------------------------------------\n"
        );

        String[] tiers = {"Silver", "Gold", "Elite", "Diamond", "Platinum"};

        System.out.println("Membership Benefits (points earned multiplier)\n");
        for (String tier : tiers) {
            double multiplier = loyaltyController.getTierMultiplier(tier);
            System.out.println(tier);
            System.out.println("  " + multiplier + "x Points");
        }

        String id = readValidId("\nMember ID                : ");
        Member member = loyaltyController.searchMemberById(id);
        if (member == null) {
            System.out.println("\nMember not found.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.println("Current Tier   : " + member.getTier());
        System.out.println("Current Points : " + member.getPoints());
        System.out.println();
        System.out.println(loyaltyController.getTierProgressDisplay(member.getTier(), member.getPoints()));

        int hypothetical = readNonNegativeInt("\nSimulate earning how many points (0 to skip): ");

        if (hypothetical > 0) {
            int simulatedPoints = member.getPoints() + hypothetical;
            String simulatedTier = loyaltyController.calculateTier(simulatedPoints);

            System.out.println();
            System.out.println("If you earn " + hypothetical + " more points:");
            System.out.println("  New Points : " + simulatedPoints);
            System.out.println("  New Tier   : " + simulatedTier
                    + (simulatedTier.equals(member.getTier()) ? " (no change)" : " (upgrade!)"));
        }

        pressEnterToContinue();
    }

    // ───────────────────── 2. Register ─────────────────────

    private void registerLoyaltyMember() {

        clearScreen();
        printBanner(BANNER_REGISTER);

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                       REGISTER LOYALTY MEMBER                               \n" +
                "------------------------------------------------------------------------------\n"
        );

        String id = readValidId("Member ID                : ");
        String name = readValidName("Member Name              : ");
        int points = readNonNegativeInt("Starting Points          : ");

        showLoading("Registering member...");
        ControllerResult result = loyaltyController.registerMember(id, name, points);

        printResult(result);
        pressEnterToContinue();
    }

    // ───────────────────── 3. Redeem Reward ─────────────────────

    private void redeemRewardMenu() {

        boolean running = true;

        while (running) {

            clearScreen();
            printBanner(BANNER_REDEEM);
            printLowStockWarning();

            System.out.print(

                    "\n" +
                    "------------------------------------------------------------------------------\n" +
                    "                            REDEEM REWARD                                    \n" +
                    "------------------------------------------------------------------------------\n" +

                    "                         1. View Rewards                                     \n" +
                    "                         2. Redeem Reward                                    \n" +
                    "                         3. View Redemption History                          \n" +
                    "                         4. Add Reward (Admin)                               \n" +
                    "                         5. Undo Last Redemption                             \n" +
                    "                         6. Search Reward                                    \n" +
                    "                         7. Sort Rewards                                     \n" +
                    "                         0. Back                                             \n" +

                    "------------------------------------------------------------------------------\n" +
                    "Enter your choice: "
            );

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    viewRewards();
                    break;

                case "2":
                    redeemReward();
                    break;

                case "3":
                    viewRedemptionHistory();
                    break;

                case "4":
                    addReward();
                    break;

                case "5":
                    undoLastRedemption();
                    break;

                case "6":
                    searchReward();
                    break;

                case "7":
                    sortRewardsMenu();
                    break;

                case "0":
                    running = false;
                    break;

                default:
                    System.out.println("\nInvalid option. Please try again.\n");
            }
        }
    }

    private void printRewardTable(ListInterface<Reward> rewards) {
        if (rewards.isEmpty()) {
            System.out.println("No rewards found.");
            return;
        }

        System.out.printf("%-8s %-25s %-15s %-10s %-8s%n", "ID", "Reward", "Category", "Points", "Qty");
        System.out.println("--------------------------------------------------------------------------");

        for (int i = 1; i <= rewards.size(); i++) {
            Reward r = rewards.getEntry(i);
            System.out.printf("%-8s %-25s %-15s %-10d %-8d%n",
                    r.getRewardId(), r.getRewardName(), r.getCategory(),
                    r.getPointsRequired(), r.getQuantity());
        }
    }

    /**
     * Shows a warning banner for any rewards running low on stock.
     * Called automatically whenever the Redeem Reward submenu opens.
     */
    private void printLowStockWarning() {
        ListInterface<Reward> lowStock = loyaltyController.getLowStockRewards();
        if (lowStock.isEmpty()) {
            return;
        }

        System.out.println("\n\u26A0 WARNING - LOW STOCK REWARDS \u26A0");
        for (int i = 1; i <= lowStock.size(); i++) {
            Reward r = lowStock.getEntry(i);
            System.out.println("  " + r.getRewardName() + " - Only " + r.getQuantity()
                    + (r.getQuantity() == 1 ? " left!" : " left!"));
        }
    }

    /**
     * Shown once when the Loyalty module opens - a quick real-time summary
     * rather than history claims we can't actually verify (e.g. "members
     * upgraded today" isn't shown since tier upgrades aren't logged with
     * a timestamp anywhere - only real, currently-computable facts are).
     */
    private void printNotificationCentre() {
        int lowStockCount = loyaltyController.getLowStockRewards().size();
        int platinumCount = loyaltyController.membershipTierDistribution()[4];
        int transactionsToday = loyaltyController.getTransactionsTodayCount();

        System.out.println();
        System.out.println("Notifications");
        System.out.println("  - " + lowStockCount + " Reward" + (lowStockCount == 1 ? "" : "s") + " Running Low");
        System.out.println("  - " + platinumCount + " Platinum Member" + (platinumCount == 1 ? "" : "s"));
        System.out.println("  - " + transactionsToday + " Transaction" + (transactionsToday == 1 ? "" : "s") + " Today");
    }

    private void viewRewards() {

        ListInterface<Reward> rewards = loyaltyController.viewRewards();

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                            REWARD LIST                                      \n" +
                "------------------------------------------------------------------------------\n"
        );

        printRewardTable(rewards);

        pressEnterToContinue();
    }

    private void searchReward() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                            SEARCH REWARD                                    \n" +
                "------------------------------------------------------------------------------\n"
        );

        String keyword = readNonEmptyString("Enter keyword           : ");

        showLoading("Searching...");
        ListInterface<Reward> results = loyaltyController.searchRewardsByKeyword(keyword);

        System.out.println();
        printRewardTable(results);

        pressEnterToContinue();
    }

    private void sortRewardsMenu() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                            SORT REWARDS                                     \n" +
                "------------------------------------------------------------------------------\n" +
                "Sort By:\n" +
                "  1. Price\n" +
                "  2. Name\n" +
                "  3. Stock\n"
        );

        int fieldChoice = readInt("Enter choice: ");
        String sortBy = switch (fieldChoice) {
            case 2 -> "name";
            case 3 -> "stock";
            default -> "price";
        };

        System.out.print(
                "\n" +
                "Sort Using:\n" +
                "  1. Selection Sort\n" +
                "  2. Bubble Sort\n"
        );

        int algoChoice = readInt("Enter choice: ");

        showLoading("Sorting rewards...");
        ListInterface<Reward> sorted = (algoChoice == 2)
                ? loyaltyController.sortRewardsBubbleSort(sortBy)
                : loyaltyController.sortRewardsSelectionSort(sortBy);

        System.out.println();
        printRewardTable(sorted);

        pressEnterToContinue();
    }

    private void redeemReward() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                           REDEEM REWARD                                    \n" +
                "------------------------------------------------------------------------------\n"
        );

        System.out.println("Available Rewards:\n");
        printRewardTable(loyaltyController.viewRewards());
        System.out.println();

        String memberId = readValidId("Member ID               : ");
        String rewardId = readValidId("Reward ID               : ");

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
        System.out.println("Member       : " + member.getName() + " (" + member.getPoints() + " pts)");
        System.out.println("Reward       : " + reward.getRewardName() + " (" + reward.getPointsRequired() + " pts required)");
        System.out.println("Stock Left   : " + reward.getQuantity());

        if (!confirmAction("\nConfirm this redemption?")) {
            System.out.println("Redemption cancelled.");
            pressEnterToContinue();
            return;
        }

        String redemptionId = loyaltyController.generateNextRedemptionId();

        showLoading("Processing redemption...");
        ControllerResult result = loyaltyController.redeemReward(redemptionId, memberId, rewardId);

        printResult(result);

        if (result.isOk()) {
            printRedemptionReceipt(redemptionId, member, reward);
        }

        pressEnterToContinue();
    }

    /**
     * Prints a formatted receipt after a successful redemption - re-fetches
     * the member so the remaining balance shown is accurate post-deduction.
     */
    private void printRedemptionReceipt(String redemptionId, Member memberBeforeRedemption, Reward reward) {
        Member updatedMember = loyaltyController.searchMemberById(memberBeforeRedemption.getMemberId());
        int remaining = (updatedMember != null) ? updatedMember.getPoints()
                : memberBeforeRedemption.getPoints() - reward.getPointsRequired();

        System.out.println();
        System.out.println("==============================");
        System.out.println("      REDEMPTION RECEIPT       ");
        System.out.println("==============================");
        System.out.println("Receipt No  : " + redemptionId);
        System.out.println("Member      : " + memberBeforeRedemption.getName());
        System.out.println("Reward      : " + reward.getRewardName());
        System.out.println("Cost        : " + reward.getPointsRequired() + " Points");
        System.out.println("Remaining   : " + remaining + " Points");
        System.out.println();
        System.out.println("         Thank You!");
        System.out.println("==============================");
    }

    private void undoLastRedemption() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                        UNDO LAST REDEMPTION                                 \n" +
                "------------------------------------------------------------------------------\n"
        );

        RewardRedemption last = loyaltyController.getLastRedemption();
        if (last == null) {
            System.out.println("\nNo redemptions to undo.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.println("Most recent redemption:");
        System.out.println(last);

        if (!confirmAction("\nUndo this redemption and refund the points?")) {
            System.out.println("Undo cancelled.");
            pressEnterToContinue();
            return;
        }

        showLoading("Reversing redemption...");
        ControllerResult result = loyaltyController.undoLastRedemption();

        printResult(result);
        pressEnterToContinue();
    }

    private void viewRedemptionHistory() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                       REDEMPTION HISTORY                                    \n" +
                "------------------------------------------------------------------------------\n"
        );

        String memberId = readOptionalValidId("Member ID (blank = all): ");

        showLoading("Loading redemption history...");
        ListInterface<RewardRedemption> history = memberId.isBlank()
                ? loyaltyController.viewRedemptionHistory()
                : loyaltyController.viewRedemptionHistoryByMember(memberId);

        if (history.isEmpty()) {
            System.out.println("\nNo redemption records found.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.printf("%-10s %-10s %-25s %-8s %-12s%n", "Redemp ID", "Member", "Reward", "Points", "Date");
        System.out.println("--------------------------------------------------------------------------");

        for (int i = 1; i <= history.size(); i++) {
            RewardRedemption r = history.getEntry(i);
            Reward reward = loyaltyController.findRewardById(r.getRewardId());
            String rewardName = (reward != null) ? reward.getRewardName() : r.getRewardId();

            System.out.printf("%-10s %-10s %-25s %-8d %-12s%n",
                    r.getRedemptionId(), r.getMemberId(), rewardName, r.getRedeemedPoints(), r.getRedeemDate());
        }

        pressEnterToContinue();
    }

    private void addReward() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                             ADD REWARD                                     \n" +
                "------------------------------------------------------------------------------\n"
        );

        String id = readValidId("Reward ID                : ");
        String name = readValidText("Reward Name              : ");
        String category = readCategoryChoice();
        int points = readPositiveInt("Points Required          : ");
        int qty = readNonNegativeInt("Quantity                 : ");

        showLoading("Adding reward...");
        ControllerResult result = loyaltyController.addReward(id, name, category, points, qty);

        printResult(result);
        pressEnterToContinue();
    }

    // ───────────────────── 4. Generate Reports ─────────────────────

    private void generateReportsMenu() {

        boolean running = true;

        while (running) {

            clearScreen();
            printBanner(BANNER_REPORTS);

            System.out.print(

                    "\n" +
                    "------------------------------------------------------------------------------\n" +
                    "                               REPORTS                                       \n" +
                    "------------------------------------------------------------------------------\n" +

                    "                         1. Top Members Report                               \n" +
                    "                         2. Membership Tier Report                           \n" +
                    "                         3. View All Members                                 \n" +
                    "                         4. Search Member                                    \n" +
                    "                         5. Redemption History Statistics                    \n" +
                    "                         6. Reward Popularity Chart                          \n" +
                    "                         7. Member Ranking                                   \n" +
                    "                         8. System Statistics Dashboard                      \n" +
                    "                         0. Back                                              \n" +

                    "------------------------------------------------------------------------------\n" +
                    "Enter your choice: "
            );

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    topMembersReport();
                    break;

                case "2":
                    tierDistributionReport();
                    break;

                case "3":
                    viewAllMembers();
                    break;

                case "4":
                    searchMember();
                    break;

                case "5":
                    redemptionStatisticsReport();
                    break;

                case "6":
                    rewardPopularityReport();
                    break;

                case "7":
                    memberRankingReport();
                    break;

                case "8":
                    systemStatisticsDashboard();
                    break;

                case "0":
                    running = false;
                    break;

                default:
                    System.out.println("\nInvalid option. Please try again.\n");
            }
        }
    }

    private void topMembersReport() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                           TOP MEMBERS REPORT                                \n" +
                "------------------------------------------------------------------------------\n"
        );

        String input = readString("Number of Top Members (blank = show all) : ");

        int topN;
        if (input.isBlank()) {
            topN = loyaltyController.getTotalMemberCount();
        } else {
            try {
                topN = Integer.parseInt(input);
                if (topN <= 0) {
                    System.out.println("Must be greater than 0 - showing all instead.");
                    topN = loyaltyController.getTotalMemberCount();
                }
            } catch (NumberFormatException e) {
                System.out.println("Not a valid number - showing all instead.");
                topN = loyaltyController.getTotalMemberCount();
            }
        }

        showLoading("Sorting members by points...");
        ListInterface<Member> topMembers = loyaltyController.topMembersByPoints(topN);

        if (topMembers.isEmpty()) {
            System.out.println("\nNo member records found.");
        } else {
            System.out.printf("%-5s %-10s %-25s %-15s %-10s%n", "No.", "ID", "Name", "Tier", "Points");
            System.out.println("--------------------------------------------------------------------------");

            for (int i = 1; i <= topMembers.size(); i++) {
                Member m = topMembers.getEntry(i);
                System.out.printf("%-5d %-10s %-25s %-15s %-10d%n",
                        i, m.getMemberId(), m.getName(), m.getTier(), m.getPoints());
            }
        }

        pressEnterToContinue();
    }

    private void redemptionStatisticsReport() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                     REDEMPTION HISTORY STATISTICS                           \n" +
                "------------------------------------------------------------------------------\n"
        );

        showLoading("Counting redemptions by category...");

        int total = loyaltyController.getTotalRedemptions();
        ListInterface<CategoryCount> stats = loyaltyController.getRedemptionStatsByCategory();

        if (stats.isEmpty() || total == 0) {
            System.out.println("\nNo redemption records yet.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.println("Total Redeemed : " + total);
        System.out.println();
        System.out.printf("%-12s %-7s %-6s  %s%n", "Category", "Count", "%", "Chart");
        System.out.println("--------------------------------------------------------------------------");

        int barMaxWidth = 20;

        for (int i = 1; i <= stats.size(); i++) {
            CategoryCount c = stats.getEntry(i);
            int percent = (int) Math.round((double) c.count / total * 100);
            int barLength = (int) Math.round((double) c.count / total * barMaxWidth);

            String bar = "#".repeat(Math.max(barLength, 0)) + "-".repeat(barMaxWidth - barLength);

            System.out.printf("%-12s %-7d %-6s  %s%n", c.category, c.count, percent + "%", bar);
        }

        pressEnterToContinue();
    }

    private void rewardPopularityReport() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                        REWARD POPULARITY CHART                             \n" +
                "------------------------------------------------------------------------------\n"
        );

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
        System.out.println("--------------------------------------------------------------------------");

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

    private void memberRankingReport() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                            MEMBER RANKING                                   \n" +
                "------------------------------------------------------------------------------\n"
        );

        showLoading("Calculating rankings...");

        int total = loyaltyController.getTotalMemberCount();
        ListInterface<Member> ranked = loyaltyController.topMembersByPoints(total);

        if (ranked.isEmpty()) {
            System.out.println("\nNo members found.");
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.printf("%-5s %-10s %-25s %-15s %-10s%n", "Rank", "ID", "Name", "Tier", "Points");
        System.out.println("--------------------------------------------------------------------------");

        for (int i = 1; i <= ranked.size(); i++) {
            Member m = ranked.getEntry(i);
            System.out.printf("#%-4d %-10s %-25s %-15s %-10d%n",
                    i, m.getMemberId(), m.getName(), m.getTier(), m.getPoints());
        }

        pressEnterToContinue();
    }

    private void systemStatisticsDashboard() {

        System.out.print(

                "\n" +
                "==================================================\n" +
                "                SYSTEM STATISTICS                  \n" +
                "==================================================\n"
        );

        showLoading("Gathering system statistics...");

        int[] tierCounts = loyaltyController.membershipTierDistribution();

        System.out.println();
        System.out.println("Members");
        System.out.println(loyaltyController.getTotalMemberCount());
        System.out.println();
        System.out.println("Rewards");
        System.out.println(loyaltyController.getTotalRewardTypes());
        System.out.println();
        System.out.println("Total Points (all members)");
        System.out.println(loyaltyController.getTotalPointsAcrossMembers());
        System.out.println();
        System.out.println("Rewards Redeemed");
        System.out.println(loyaltyController.getTotalRedemptions());
        System.out.println();
        System.out.println("Most Popular Category");
        System.out.println(loyaltyController.getMostPopularCategory());
        System.out.println();
        System.out.println("Platinum Members");
        System.out.println(tierCounts[4]);

        System.out.println("==================================================");

        pressEnterToContinue();
    }

    private void tierDistributionReport() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                      MEMBERSHIP TIER REPORT                                 \n" +
                "------------------------------------------------------------------------------\n"
        );

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
        System.out.println("--------------------------------------------------------------------------");

        int barMaxWidth = 20;

        for (int i = 0; i < tiers.length; i++) {
            int percent = (int) Math.round((double) counts[i] / totalMembers * 100);
            int barLength = (int) Math.round((double) counts[i] / totalMembers * barMaxWidth);

            String bar = "#".repeat(Math.max(barLength, 0)) + "-".repeat(barMaxWidth - barLength);

            System.out.printf("%-10s %-9d %-6s  %s%n", tiers[i], counts[i], percent + "%", bar);
        }

        pressEnterToContinue();
    }

    private void viewAllMembers() {

        ListInterface<Member> members = loyaltyController.viewAllMembers();

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                           MEMBER LIST                                       \n" +
                "------------------------------------------------------------------------------\n"
        );

        if (members.isEmpty()) {
            System.out.println("No members found.");
        } else {
            System.out.printf("%-10s %-25s %-15s %-10s%n", "ID", "NAME", "TIER", "POINTS");
            System.out.println("---------------------------------------------------------------");

            for (int i = 1; i <= members.size(); i++) {
                Member m = members.getEntry(i);
                System.out.printf("%-10s %-25s %-15s %-10d%n",
                        m.getMemberId(), m.getName(), m.getTier(), m.getPoints());
            }
        }

        pressEnterToContinue();
    }

    private void searchMember() {

        System.out.print(

                "\n" +
                "------------------------------------------------------------------------------\n" +
                "                            SEARCH MEMBER                                    \n" +
                "------------------------------------------------------------------------------\n"
        );

        String keyword = readValidName("Member Name              : ");

        showLoading("Searching...");
        ListInterface<Member> results = loyaltyController.searchMemberByName(keyword);

        if (results.isEmpty()) {
            System.out.println("\nNo matching member found.");
        } else {
            System.out.println();
            System.out.printf("%-10s %-25s %-15s %-10s%n", "ID", "NAME", "TIER", "POINTS");
            System.out.println("---------------------------------------------------------------");

            for (int i = 1; i <= results.size(); i++) {
                Member m = results.getEntry(i);
                System.out.printf("%-10s %-25s %-15s %-10d%n",
                        m.getMemberId(), m.getName(), m.getTier(), m.getPoints());
            }
        }

        pressEnterToContinue();
    }

    // ───────────────────── Input Helpers ─────────────────────

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
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

    private void pressEnterToContinue() {
        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }
}