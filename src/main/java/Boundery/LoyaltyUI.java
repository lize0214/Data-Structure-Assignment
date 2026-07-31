package Boundery;

import ADT.ListInterface;
import Control.LoyaltyController;
import Control.MemberController;
import Entity.Member;
import Entity.Reward;
import Entity.RewardRedemption;
import Utility.ControllerResult;

import java.util.Scanner;

/**
 * LoyaltyUI.java
 * Console menu for the Loyalty & Rewards Service.
 */
public class LoyaltyUI {

    private final Scanner scanner;
    private final LoyaltyController loyaltyController;

    public LoyaltyUI() {
        this.scanner = new Scanner(System.in);
        MemberController memberController = new MemberController();
        this.loyaltyController = new LoyaltyController(memberController);
    }

    // Allows Main.java (or a shared menu) to pass in an existing MemberController
    // instead of this class creating its own, so all modules share one instance.
    public LoyaltyUI(MemberController sharedMemberController) {
        this.scanner = new Scanner(System.in);
        this.loyaltyController = new LoyaltyController(sharedMemberController);
    }

    // Diet Cola-style ASCII banner for "REWARDS" (matches the team's POPCART title style)
    private static final String[] BANNER = {
        "      .-.                 .-                      /\\       .-.           .-.                .-. ",
        "     (_) )-.      .---;`-'   ..-.     .-.      _  / |      (_) )-.       (_) )-.       .--.-'    ",
        "        /   \\    (   (_)        )   (        (  /  |  .      /   \\         /   \\     (  (_)     ",
        "       /     )    )--          /     \\        `/.__|_.'     /     )       /     \\     `-.       ",
        "    .-/  `--'    (      /     (   .   )   .:' /    |     .-/  `--'     .-/.      )  _    )      ",
        "   (_/     `-._) `\\___.'       `-' `-'   (__.'     `-'  (_/     `-._) (_/  `----'  (_.--'       "
    };

    public void run() {
        boolean exit = false;
        while (!exit) {
            printMenu();
            int choice = readInt("\u25C7 Enter your choice: ");

            switch (choice) {
                case 1 -> registerMember();
                case 2 -> updateMember();
                case 3 -> deleteMember();
                case 4 -> searchMember();
                case 5 -> viewAllMembers();
                case 6 -> earnPoints();
                case 7 -> redeemReward();
                case 8 -> checkPointsBalance();
                case 9 -> viewMembershipTier();
                case 10 -> viewRewards();
                case 11 -> addReward();
                case 12 -> viewRedemptionHistory();
                case 13 -> topMembersReport();
                case 14 -> tierDistributionReport();
                case 0 -> exit = true;
                default -> System.out.println("\u274C Invalid choice, please try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        for (String line : BANNER) {
            System.out.println(line);
        }
        System.out.println();
        System.out.println("|========================================|");
        System.out.println("|      \u2728 WELCOME TO LOYALTY & REWARDS \u2728    |");
        System.out.println("|========================================|");
        System.out.println("|  \uD83D\uDC64 Member Management                     |");
        System.out.println("|   [1]  Register Member                  |");
        System.out.println("|   [2]  Update Member                    |");
        System.out.println("|   [3]  Delete Member                    |");
        System.out.println("|   [4]  Search Member (by name)          |");
        System.out.println("|   [5]  View All Members                 |");
        System.out.println("|----------------------------------------|");
        System.out.println("|  \u2B50 Reward Points                       |");
        System.out.println("|   [6]  Earn Reward Points                |");
        System.out.println("|   [7]  Redeem Reward                    |");
        System.out.println("|   [8]  Check Points Balance              |");
        System.out.println("|----------------------------------------|");
        System.out.println("|  \uD83C\uDFC6 Membership                          |");
        System.out.println("|   [9]  View Membership Tier             |");
        System.out.println("|----------------------------------------|");
        System.out.println("|  \uD83C\uDF81 Rewards                              |");
        System.out.println("|   [10] View Rewards                     |");
        System.out.println("|   [11] Add Reward                       |");
        System.out.println("|   [12] View Redemption History          |");
        System.out.println("|----------------------------------------|");
        System.out.println("|  \uD83D\uDCCA Reports                              |");
        System.out.println("|   [13] Top Members Report               |");
        System.out.println("|   [14] Membership Tier Report           |");
        System.out.println("|----------------------------------------|");
        System.out.println("|   [0]  \uD83D\uDEAA Return                          |");
        System.out.println("|========================================|");
    }

    // ───────────────────── Member Management ─────────────────────

    private void registerMember() {
        String id = readString("Member ID: ");
        String name = readString("Name: ");
        String tier = readString("Tier (Silver/Gold/Elite/Diamond/Platinum): ");
        int points = readInt("Starting points: ");

        ControllerResult result = loyaltyController.registerMember(id, name, tier, points);
        printResult(result);
    }

    private void updateMember() {
        String id = readString("Member ID to update: ");
        String name = readString("New name: ");
        String tier = readString("New tier: ");
        int points = readInt("New points: ");

        ControllerResult result = loyaltyController.updateMember(id, name, tier, points);
        printResult(result);
    }

    private void deleteMember() {
        String id = readString("Member ID to delete: ");
        ControllerResult result = loyaltyController.deleteMember(id);
        printResult(result);
    }

    private void searchMember() {
        String namePart = readString("Enter name (or part of name): ");
        ListInterface<Member> results = loyaltyController.searchMemberByName(namePart);

        if (results.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        for (int i = 1; i <= results.size(); i++) {
            System.out.println(results.getEntry(i));
        }
    }

    private void viewAllMembers() {
        ListInterface<Member> members = loyaltyController.viewAllMembers();
        if (members.isEmpty()) {
            System.out.println("No members registered yet.");
            return;
        }
        for (int i = 1; i <= members.size(); i++) {
            System.out.println(members.getEntry(i));
        }
    }

    // ───────────────────── Reward Points ─────────────────────

    private void earnPoints() {
        String id = readString("Member ID: ");
        int points = readInt("Points earned: ");
        ControllerResult result = loyaltyController.earnPoints(id, points);
        printResult(result);
    }

    private void checkPointsBalance() {
        String id = readString("Member ID: ");
        int balance = loyaltyController.checkPointsBalance(id);
        if (balance == -1) {
            System.out.println("Member not found.");
        } else {
            System.out.println("Current points balance: " + balance);
        }
    }

    // ───────────────────── Membership Tier ─────────────────────

    private void viewMembershipTier() {
        String id = readString("Member ID: ");
        Member member = loyaltyController.searchMemberById(id);
        if (member == null) {
            System.out.println("Member not found.");
            return;
        }
        System.out.println("Member: " + member.getName() + " | Current tier: " + member.getTier()
                + " | Points: " + member.getPoints());
    }

    // ───────────────────── Rewards ─────────────────────

    private void viewRewards() {
        ListInterface<Reward> rewards = loyaltyController.viewRewards();
        if (rewards.isEmpty()) {
            System.out.println("No rewards available.");
            return;
        }
        for (int i = 1; i <= rewards.size(); i++) {
            System.out.println(rewards.getEntry(i));
        }
    }

    private void addReward() {
        String id = readString("Reward ID: ");
        String name = readString("Reward name: ");
        String category = readString("Category: ");
        int pointsRequired = readInt("Points required: ");
        int quantity = readInt("Quantity: ");

        ControllerResult result = loyaltyController.addReward(id, name, category, pointsRequired, quantity);
        printResult(result);
    }

    private void redeemReward() {
        String redemptionId = readString("Redemption ID: ");
        String memberId = readString("Member ID: ");
        String rewardId = readString("Reward ID: ");

        ControllerResult result = loyaltyController.redeemReward(redemptionId, memberId, rewardId);
        printResult(result);
    }

    private void viewRedemptionHistory() {
        String memberId = readString("Filter by Member ID (leave blank for all): ");

        ListInterface<RewardRedemption> history = memberId.isBlank()
                ? loyaltyController.viewRedemptionHistory()
                : loyaltyController.viewRedemptionHistoryByMember(memberId);

        if (history.isEmpty()) {
            System.out.println("No redemption records found.");
            return;
        }

        for (int i = 1; i <= history.size(); i++) {
            System.out.println(history.getEntry(i));
        }
    }

    // ───────────────────── Reports ─────────────────────

    private void topMembersReport() {
        int topN = readInt("Show top how many members? ");
        ListInterface<Member> topMembers = loyaltyController.topMembersByPoints(topN);

        System.out.println("\uD83C\uDFC6 ========== TOP MEMBERS ========== \uD83C\uDFC6");
        for (int i = 1; i <= topMembers.size(); i++) {
            Member m = topMembers.getEntry(i);
            System.out.printf("%d. %s (%s) - %s - %d pts%n",
                    i, m.getMemberId(), m.getName(), m.getTier(), m.getPoints());
        }
    }

    private void tierDistributionReport() {
        int[] counts = loyaltyController.membershipTierDistribution();
        String[] tierNames = {"Silver", "Gold", "Elite", "Diamond", "Platinum"};

        System.out.println("\uD83D\uDCCA ===== MEMBERSHIP TIER REPORT ===== \uD83D\uDCCA");
        for (int i = 0; i < tierNames.length; i++) {
            System.out.println(tierNames[i] + " : " + counts[i]);
        }
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
        if (result.isOk()) {
            System.out.println("\u2705 Success" + (result.getMessage() != null ? ": " + result.getMessage() : "."));
        } else {
            System.out.println("\u274C Failed: " + result.getMessage());
        }
    }
}
