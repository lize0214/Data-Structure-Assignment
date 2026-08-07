package Control;

import ADT.DoublyLinkedList;
import ADT.ListInterface;
import Entity.Member;
import Entity.PointsTransaction;
import Entity.Reward;
import Entity.RewardRedemption;
import Utility.ControllerResult;
import Utility.FileUtility;
import Utility.ValidationUtility;

import java.time.LocalDate;

/**
 * LoyaltyController.java
 *
 * Control class for the Loyalty & Rewards Service.
 *
 * NOTE: This class intentionally does NOT extend AbstractEntityController.
 * AbstractEntityController's constructor hardcodes `new ArrayList<>()`,
 * so extending it would mean this module's chosen ADT (DoublyLinkedList)
 * never actually gets used. Instead, this controller manages its own
 * DoublyLinkedList<Reward> and DoublyLinkedList<RewardRedemption>, and
 * depends on the shared MemberController for reading/updating Members
 * (which is owned by the team and must not be duplicated).
 */
public class LoyaltyController {

    private static final String REWARD_FILE = "data/rewards.txt";
    private static final String REDEMPTION_FILE = "data/redemptions.txt";
    private static final String TRANSACTION_FILE = "data/transactions.txt";

    // Tier point thresholds (inclusive lower bound). Adjust freely — kept
    // in one place so the whole tier system can be tuned from here.
    private static final int GOLD_MIN = 1000;
    private static final int ELITE_MIN = 3000;
    private static final int DIAMOND_MIN = 6000;
    private static final int PLATINUM_MIN = 10000;

    // A reward at or below this quantity triggers a low-stock warning.
    private static final int LOW_STOCK_THRESHOLD = 3;

    private final ListInterface<Reward> rewardList;
    private final ListInterface<RewardRedemption> redemptionList;
    private final ListInterface<PointsTransaction> transactionList;
    private final MemberController memberController;

    public LoyaltyController(MemberController memberController) {
        this.memberController = memberController;
        this.rewardList = new DoublyLinkedList<>();
        this.redemptionList = new DoublyLinkedList<>();
        this.transactionList = new DoublyLinkedList<>();
        loadRewardsFromFile();
        loadRedemptionsFromFile();
        loadTransactionsFromFile();
    }

    // ───────────────────── File I/O ─────────────────────

    private void loadRewardsFromFile() {
        String[] lines = FileUtility.readLines(REWARD_FILE);
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                rewardList.add(Reward.fromCsvLine(line));
            }
        }
    }

    private void saveRewardsToFile() {
        String[] lines = new String[rewardList.size()];
        for (int i = 0; i < rewardList.size(); i++) {
            lines[i] = rewardList.getEntry(i + 1).toCsvLine();
        }
        FileUtility.writeAllLines(REWARD_FILE, lines);
    }

    private void loadRedemptionsFromFile() {
        String[] lines = FileUtility.readLines(REDEMPTION_FILE);
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                redemptionList.add(RewardRedemption.fromCsvLine(line));
            }
        }
    }

    private void saveRedemptionsToFile() {
        String[] lines = new String[redemptionList.size()];
        for (int i = 0; i < redemptionList.size(); i++) {
            lines[i] = redemptionList.getEntry(i + 1).toCsvLine();
        }
        FileUtility.writeAllLines(REDEMPTION_FILE, lines);
    }

    private void loadTransactionsFromFile() {
        String[] lines = FileUtility.readLines(TRANSACTION_FILE);
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                transactionList.add(PointsTransaction.fromCsvLine(line));
            }
        }
    }

    private void saveTransactionsToFile() {
        String[] lines = new String[transactionList.size()];
        for (int i = 0; i < transactionList.size(); i++) {
            lines[i] = transactionList.getEntry(i + 1).toCsvLine();
        }
        FileUtility.writeAllLines(TRANSACTION_FILE, lines);
    }

    private String generateNextTransactionId() {
        return String.format("TXN%04d", transactionList.size() + 1);
    }

    /**
     * Appends one entry to the points transaction log and persists it.
     * Called internally whenever a member's points change for any reason,
     * so Transaction History always reflects every earn/redeem/undo/
     * registration event, not just redemptions.
     */
    private void logTransaction(String memberId, int pointsChange, String type, String note) {
        PointsTransaction transaction = new PointsTransaction(
                generateNextTransactionId(), memberId, pointsChange, type, note, LocalDate.now());
        transactionList.add(transaction);
        saveTransactionsToFile();
    }

    public ListInterface<PointsTransaction> getTransactionHistoryByMember(String memberId) {
        ListInterface<PointsTransaction> results = new DoublyLinkedList<>();
        for (int i = 1; i <= transactionList.size(); i++) {
            PointsTransaction t = transactionList.getEntry(i);
            if (t.getMemberId().equals(memberId)) {
                results.add(t);
            }
        }
        return results;
    }

    /**
     * Full transaction ledger across every member - used when the
     * Transaction History screen is asked to show everything.
     */
    public ListInterface<PointsTransaction> getAllTransactions() {
        return transactionList;
    }

    /**
     * Count of transactions logged today - used by the Notification Centre.
     */
    public int getTransactionsTodayCount() {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (int i = 1; i <= transactionList.size(); i++) {
            if (transactionList.getEntry(i).getTransactionDate().equals(today)) {
                count++;
            }
        }
        return count;
    }

    // ───────────────────── Member Management (delegates to shared MemberController) ─────────────────────

    /**
     * Registers a new Loyalty member. The tier is NOT chosen manually -
     * it's calculated automatically from the starting points, exactly like
     * checkAndApplyTierUpgrade() does later on. This guarantees a member's
     * tier can never be inconsistent with their points (e.g. "Platinum"
     * with 0 points), and matches the flowchart's "Assign Initial
     * Membership Tier" step, which is an automatic assignment, not a
     * manual selection.
     */
    public ControllerResult registerMember(String memberId, String name, int points) {
        ValidationUtility.ValidationAccumulator acc = new ValidationUtility.ValidationAccumulator();
        acc.check(ValidationUtility.validateRequired(memberId, "Member ID"));
        acc.check(ValidationUtility.validateRequired(name, "Name"));
        acc.check(ValidationUtility.validateNonNegative(points, "Points"));
        if (acc.hasErrors()) {
            return ControllerResult.fail(acc.getErrorMessage());
        }

        String assignedTier = calculateTier(points);
        Member newMember = new Member(memberId, name, assignedTier, points);
        ControllerResult result = memberController.add(newMember);

        if (!result.isOk()) {
            return result;
        }

        if (points > 0) {
            logTransaction(memberId, points, "REGISTER", "Registration Starting Points");
        }

        return ControllerResult.success("Assigned tier: " + assignedTier + " (based on " + points + " starting points)");
    }

    public Member searchMemberById(String memberId) {
        return memberController.findByKey(memberId);
    }

    /**
     * Linear search by name (case-insensitive, partial match) since the
     * shared MemberController only supports lookup by ID.
     */
    public ListInterface<Member> searchMemberByName(String namePart) {
        ListInterface<Member> results = new DoublyLinkedList<>();
        ListInterface<Member> allMembers = memberController.getAll();

        for (int i = 1; i <= allMembers.size(); i++) {
            Member member = allMembers.getEntry(i);
            if (member.getName().toLowerCase().contains(namePart.toLowerCase())) {
                results.add(member);
            }
        }
        return results;
    }

    public ListInterface<Member> viewAllMembers() {
        return memberController.getAll();
    }

    // ───────────────────── Reward Points ─────────────────────

    public ControllerResult earnPoints(String memberId, int pointsEarned) {
        String error = ValidationUtility.validatePositive(pointsEarned, "Points earned");
        if (error != null) {
            return ControllerResult.fail(error);
        }

        Member member = memberController.findByKey(memberId);
        if (member == null) {
            return ControllerResult.fail("Member not found: " + memberId);
        }

        double multiplier = getTierMultiplier(member.getTier());
        int awardedPoints = (int) Math.round(pointsEarned * multiplier);

        int newPoints = member.getPoints() + awardedPoints;
        ControllerResult result = memberController.updatePoints(memberId, newPoints);
        if (!result.isOk()) {
            return result;
        }

        checkAndApplyTierUpgrade(memberId);

        String bonusNote = (multiplier > 1.00)
                ? " (" + member.getTier() + " tier bonus applied: x" + multiplier + ")"
                : "";

        String transactionNote = (multiplier > 1.00)
                ? "Earned Points (" + member.getTier() + " Bonus)"
                : "Earned Points";
        logTransaction(memberId, awardedPoints, "EARN", transactionNote);

        return ControllerResult.success("Earned " + awardedPoints + " points" + bonusNote + ". New balance: " + newPoints);
    }

    /**
     * Returns the member's rank by points (1 = highest). Counts how many
     * members have strictly more points and adds 1 - an O(n) comparison
     * count rather than a full sort, since only the position is needed.
     */
    public int getMemberRank(String memberId) {
        Member target = memberController.findByKey(memberId);
        if (target == null) {
            return -1;
        }

        ListInterface<Member> all = memberController.getAll();
        int rank = 1;
        for (int i = 1; i <= all.size(); i++) {
            if (all.getEntry(i).getPoints() > target.getPoints()) {
                rank++;
            }
        }
        return rank;
    }

    public int getTotalMemberCount() {
        return memberController.getAll().size();
    }

    /**
     * Most recent redemption for a member (last entry appended for them),
     * or null if they haven't redeemed anything yet.
     */
    public RewardRedemption getMostRecentRedemptionForMember(String memberId) {
        ListInterface<RewardRedemption> memberHistory = viewRedemptionHistoryByMember(memberId);
        if (memberHistory.isEmpty()) {
            return null;
        }
        return memberHistory.getEntry(memberHistory.size());
    }

    /**
     * Count of rewards still in stock (regardless of whether any given
     * member can currently afford them).
     */
    public int getAvailableRewardCount() {
        int count = 0;
        for (int i = 1; i <= rewardList.size(); i++) {
            if (rewardList.getEntry(i).getQuantity() > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Tier bonus multiplier applied automatically when earning points.
     * Higher tiers earn slightly more per action - a small perk that
     * rewards loyalty and gives the tier system real weight beyond
     * just a label.
     */
    public double getTierMultiplier(String tier) {
        return switch (tier) {
            case "Gold" -> 1.10;
            case "Elite" -> 1.20;
            case "Diamond" -> 1.35;
            case "Platinum" -> 1.50;
            default -> 1.00; // Silver / unknown
        };
    }

    // ───────────────────── Membership Tier ─────────────────────

    /**
     * Determines the correct tier for a given points total.
     */
    public String calculateTier(int points) {
        if (points >= PLATINUM_MIN) return "Platinum";
        if (points >= DIAMOND_MIN) return "Diamond";
        if (points >= ELITE_MIN) return "Elite";
        if (points >= GOLD_MIN) return "Gold";
        return "Silver";
    }

    /**
     * Recalculates a member's tier based on their current points and
     * updates it if it has changed. Returns a ControllerResult describing
     * whether an upgrade happened.
     */
    public ControllerResult checkAndApplyTierUpgrade(String memberId) {
        Member member = memberController.findByKey(memberId);
        if (member == null) {
            return ControllerResult.fail("Member not found: " + memberId);
        }

        String correctTier = calculateTier(member.getPoints());
        if (correctTier.equals(member.getTier())) {
            return ControllerResult.success("No tier change (" + correctTier + ")");
        }

        ControllerResult result = memberController.updateTier(memberId, correctTier);
        if (!result.isOk()) {
            return result;
        }
        return ControllerResult.success("Tier upgraded to " + correctTier);
    }

    /**
     * Returns rewards the member can currently afford (points requirement
     * met AND still in stock) - used to show "Recommended Rewards" when
     * a member checks their balance.
     */
    public ListInterface<Reward> getAffordableRewards(int memberPoints) {
        ListInterface<Reward> result = new DoublyLinkedList<>();
        for (int i = 1; i <= rewardList.size(); i++) {
            Reward r = rewardList.getEntry(i);
            if (r.getPointsRequired() <= memberPoints && r.getQuantity() > 0) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Builds a printable progress bar showing how close a member is to
     * the next tier up. Returns a plain message if already at the top tier.
     */
    public String getTierProgressDisplay(String currentTier, int currentPoints) {
        String[] tiers = {"Silver", "Gold", "Elite", "Diamond", "Platinum"};
        int[] thresholds = {0, GOLD_MIN, ELITE_MIN, DIAMOND_MIN, PLATINUM_MIN};

        int index = -1;
        for (int i = 0; i < tiers.length; i++) {
            if (tiers[i].equalsIgnoreCase(currentTier)) {
                index = i;
                break;
            }
        }

        if (index == -1 || index == tiers.length - 1) {
            return "Highest tier reached - no further progress needed.";
        }

        int currentMin = thresholds[index];
        int nextMin = thresholds[index + 1];
        String nextTier = tiers[index + 1];

        double progress = (double) (currentPoints - currentMin) / (nextMin - currentMin);
        progress = Math.max(0.0, Math.min(1.0, progress));

        int barLength = 20;
        int filled = (int) Math.round(progress * barLength);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            bar.append(i < filled ? '#' : '-');
        }

        int pointsNeeded = Math.max(0, nextMin - currentPoints);
        int percent = (int) Math.round(progress * 100);

        return "Progress to " + nextTier + "\n"
                + currentTier + "\n"
                + bar + " " + percent + "%\n"
                + "Need " + pointsNeeded + " more points.";
    }

    // ───────────────────── Reward Management ─────────────────────

    public ControllerResult addReward(String rewardId, String rewardName, String category,
                                       int pointsRequired, int quantity) {
        ValidationUtility.ValidationAccumulator acc = new ValidationUtility.ValidationAccumulator();
        acc.check(ValidationUtility.validateRequired(rewardId, "Reward ID"));
        acc.check(ValidationUtility.validateRequired(rewardName, "Reward name"));
        acc.check(ValidationUtility.validatePositive(pointsRequired, "Points required"));
        acc.check(ValidationUtility.validateNonNegative(quantity, "Quantity"));
        if (acc.hasErrors()) {
            return ControllerResult.fail(acc.getErrorMessage());
        }

        if (findRewardById(rewardId) != null) {
            return ControllerResult.fail("Reward already exists: " + rewardId);
        }

        rewardList.add(new Reward(rewardId, rewardName, category, pointsRequired, quantity));
        saveRewardsToFile();
        return ControllerResult.success();
    }

    public Reward findRewardById(String rewardId) {
        int position = findRewardPosition(rewardId);
        return (position == -1) ? null : rewardList.getEntry(position);
    }

    private int findRewardPosition(String rewardId) {
        for (int i = 1; i <= rewardList.size(); i++) {
            if (rewardList.getEntry(i).getRewardId().equals(rewardId)) {
                return i;
            }
        }
        return -1;
    }

    public ListInterface<Reward> viewRewards() {
        return rewardList;
    }

    /**
     * Linear search across reward name and category (case-insensitive,
     * partial match) - lets a member type "spa" and find "Spa Voucher"
     * instead of scrolling the whole catalog.
     */
    public ListInterface<Reward> searchRewardsByKeyword(String keyword) {
        ListInterface<Reward> results = new DoublyLinkedList<>();
        String lowerKeyword = keyword.toLowerCase();

        for (int i = 1; i <= rewardList.size(); i++) {
            Reward r = rewardList.getEntry(i);
            if (r.getRewardName().toLowerCase().contains(lowerKeyword)
                    || r.getCategory().toLowerCase().contains(lowerKeyword)) {
                results.add(r);
            }
        }
        return results;
    }

    /**
     * Rewards at or below LOW_STOCK_THRESHOLD (and still in stock) -
     * used to automatically warn before someone tries to redeem something
     * that's about to run out.
     */
    public ListInterface<Reward> getLowStockRewards() {
        ListInterface<Reward> lowStock = new DoublyLinkedList<>();
        for (int i = 1; i <= rewardList.size(); i++) {
            Reward r = rewardList.getEntry(i);
            if (r.getQuantity() > 0 && r.getQuantity() <= LOW_STOCK_THRESHOLD) {
                lowStock.add(r);
            }
        }
        return lowStock;
    }

    private Reward[] toRewardArray() {
        Reward[] array = new Reward[rewardList.size()];
        for (int i = 1; i <= rewardList.size(); i++) {
            array[i - 1] = rewardList.getEntry(i);
        }
        return array;
    }

    /**
     * sortBy: "price" (points required), "name" (alphabetical), or "stock" (quantity).
     */
    private int compareRewards(Reward a, Reward b, String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" -> a.getRewardName().compareToIgnoreCase(b.getRewardName());
            case "stock" -> Integer.compare(a.getQuantity(), b.getQuantity());
            default -> Integer.compare(a.getPointsRequired(), b.getPointsRequired()); // "price"
        };
    }

    /**
     * Selection sort - repeatedly picks the smallest remaining element.
     */
    public ListInterface<Reward> sortRewardsSelectionSort(String sortBy) {
        Reward[] array = toRewardArray();

        for (int i = 0; i < array.length - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < array.length; j++) {
                if (compareRewards(array[j], array[minIndex], sortBy) < 0) {
                    minIndex = j;
                }
            }
            Reward temp = array[i];
            array[i] = array[minIndex];
            array[minIndex] = temp;
        }

        ListInterface<Reward> sorted = new DoublyLinkedList<>();
        for (Reward r : array) {
            sorted.add(r);
        }
        return sorted;
    }

    /**
     * Bubble sort - repeatedly swaps adjacent out-of-order pairs.
     */
    public ListInterface<Reward> sortRewardsBubbleSort(String sortBy) {
        Reward[] array = toRewardArray();

        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - 1 - i; j++) {
                if (compareRewards(array[j], array[j + 1], sortBy) > 0) {
                    Reward temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }

        ListInterface<Reward> sorted = new DoublyLinkedList<>();
        for (Reward r : array) {
            sorted.add(r);
        }
        return sorted;
    }

    // ───────────────────── Reward Redemption ─────────────────────

    public ControllerResult redeemReward(String redemptionId, String memberId, String rewardId) {
        Member member = memberController.findByKey(memberId);
        if (member == null) {
            return ControllerResult.fail("Member not found: " + memberId);
        }

        Reward reward = findRewardById(rewardId);
        if (reward == null) {
            return ControllerResult.fail("Reward not found: " + rewardId);
        }

        if (reward.getQuantity() <= 0) {
            return ControllerResult.fail("Reward out of stock: " + reward.getRewardName());
        }

        if (member.getPoints() < reward.getPointsRequired()) {
            return ControllerResult.fail("Insufficient points. Required: "
                    + reward.getPointsRequired() + ", available: " + member.getPoints());
        }

        // Deduct points from member
        int newPoints = member.getPoints() - reward.getPointsRequired();
        ControllerResult pointsResult = memberController.updatePoints(memberId, newPoints);
        if (!pointsResult.isOk()) {
            return pointsResult;
        }

        // Decrement reward stock
        reward.setQuantity(reward.getQuantity() - 1);
        saveRewardsToFile();

        // Record redemption
        RewardRedemption redemption = new RewardRedemption(
                redemptionId, memberId, rewardId, reward.getPointsRequired(), LocalDate.now());
        redemptionList.add(redemption);
        saveRedemptionsToFile();

        // Tier may change after points deduction
        checkAndApplyTierUpgrade(memberId);

        logTransaction(memberId, -reward.getPointsRequired(), "REDEEM", "Redeemed " + reward.getRewardName());

        return ControllerResult.success("Redeemed " + reward.getRewardName()
                + " for " + reward.getPointsRequired() + " points. Remaining balance: " + newPoints);
    }

    public ListInterface<RewardRedemption> viewRedemptionHistory() {
        return redemptionList;
    }

    public ListInterface<RewardRedemption> viewRedemptionHistoryByMember(String memberId) {
        ListInterface<RewardRedemption> results = new DoublyLinkedList<>();
        for (int i = 1; i <= redemptionList.size(); i++) {
            RewardRedemption redemption = redemptionList.getEntry(i);
            if (redemption.getMemberId().equals(memberId)) {
                results.add(redemption);
            }
        }
        return results;
    }

    public int getTotalRedemptions() {
        return redemptionList.size();
    }

    /**
     * One category name paired with how many times it's been redeemed.
     * Mutable count so the simple tally loop below can increment in place.
     */
    public static class CategoryCount {
        public final String category;
        public int count;

        public CategoryCount(String category) {
            this.category = category;
            this.count = 0;
        }
    }

    /**
     * Tallies redemptions by reward category - a simple counting algorithm:
     * one pass through the redemption log, looking up each reward's
     * category and incrementing a running total for that category.
     */
    public ListInterface<CategoryCount> getRedemptionStatsByCategory() {
        ListInterface<CategoryCount> stats = new DoublyLinkedList<>();

        for (int i = 1; i <= redemptionList.size(); i++) {
            RewardRedemption redemption = redemptionList.getEntry(i);
            Reward reward = findRewardById(redemption.getRewardId());
            String category = (reward != null) ? reward.getCategory() : "Unknown";

            CategoryCount existing = null;
            for (int j = 1; j <= stats.size(); j++) {
                CategoryCount c = stats.getEntry(j);
                if (c.category.equalsIgnoreCase(category)) {
                    existing = c;
                    break;
                }
            }

            if (existing != null) {
                existing.count++;
            } else {
                CategoryCount newCount = new CategoryCount(category);
                newCount.count = 1;
                stats.add(newCount);
            }
        }

        return stats;
    }

    /**
     * Generates the next Redemption ID automatically (RDM0001, RDM0002, ...)
     * so the user never has to type - and can't mistype - an ID by hand.
     */
    public String generateNextRedemptionId() {
        return String.format("RDM%04d", redemptionList.size() + 1);
    }

    /**
     * Returns the most recent redemption, or null if there isn't one.
     * Used to show a confirmation preview before undoing.
     */
    public RewardRedemption getLastRedemption() {
        if (redemptionList.isEmpty()) {
            return null;
        }
        return redemptionList.getEntry(redemptionList.size());
    }

    /**
     * Reverses the most recent redemption: refunds the member's points,
     * restocks the reward, re-checks their tier, and removes the record.
     *
     * This specifically removes from the TAIL of the list - the one
     * operation a DoublyLinkedList does in O(1) that a plain ArrayList
     * cannot (an ArrayList remove from the end is also O(1) actually,
     * but removal from an arbitrary position requires shifting; the real
     * win here is that DoublyLinkedList.remove() needs no shifting at all,
     * regardless of where the entry sits - which is the core justification
     * for choosing this ADT for a frequently changing redemption log).
     */
    public ControllerResult undoLastRedemption() {
        if (redemptionList.isEmpty()) {
            return ControllerResult.fail("No redemptions to undo.");
        }

        int lastPosition = redemptionList.size();
        RewardRedemption last = redemptionList.getEntry(lastPosition);

        Member member = memberController.findByKey(last.getMemberId());
        if (member != null) {
            int refundedPoints = member.getPoints() + last.getRedeemedPoints();
            memberController.updatePoints(last.getMemberId(), refundedPoints);
            checkAndApplyTierUpgrade(last.getMemberId());
            logTransaction(last.getMemberId(), last.getRedeemedPoints(), "UNDO",
                    "Refund - Undo " + last.getRedemptionId());
        }

        Reward reward = findRewardById(last.getRewardId());
        if (reward != null) {
            reward.setQuantity(reward.getQuantity() + 1);
            saveRewardsToFile();
        }

        redemptionList.remove(lastPosition);
        saveRedemptionsToFile();

        return ControllerResult.success("Undid redemption " + last.getRedemptionId()
                + ". Refunded " + last.getRedeemedPoints() + " points to " + last.getMemberId() + ".");
    }

    // ───────────────────── Reports ─────────────────────

    /**
     * Returns the top N members ordered by reward points (descending).
     * Uses a simple selection sort over a copy of the member list so the
     * original data (owned by MemberController) is never mutated.
     */
    public ListInterface<Member> topMembersByPoints(int topN) {
        ListInterface<Member> allMembers = memberController.getAll();
        int total = allMembers.size();

        Member[] snapshot = new Member[total];
        for (int i = 1; i <= total; i++) {
            snapshot[i - 1] = allMembers.getEntry(i);
        }

        // Selection sort, descending by points
        for (int i = 0; i < snapshot.length - 1; i++) {
            int maxIndex = i;
            for (int j = i + 1; j < snapshot.length; j++) {
                if (snapshot[j].getPoints() > snapshot[maxIndex].getPoints()) {
                    maxIndex = j;
                }
            }
            Member temp = snapshot[i];
            snapshot[i] = snapshot[maxIndex];
            snapshot[maxIndex] = temp;
        }

        ListInterface<Member> topMembers = new DoublyLinkedList<>();
        int limit = Math.min(topN, snapshot.length);
        for (int i = 0; i < limit; i++) {
            topMembers.add(snapshot[i]);
        }
        return topMembers;
    }

    /**
     * Returns a count of members in each tier as an int[5]:
     * [Silver, Gold, Elite, Diamond, Platinum]
     */
    public int[] membershipTierDistribution() {
        int[] counts = new int[5]; // Silver, Gold, Elite, Diamond, Platinum
        ListInterface<Member> allMembers = memberController.getAll();

        for (int i = 1; i <= allMembers.size(); i++) {
            String tier = allMembers.getEntry(i).getTier();
            switch (tier) {
                case "Silver" -> counts[0]++;
                case "Gold" -> counts[1]++;
                case "Elite" -> counts[2]++;
                case "Diamond" -> counts[3]++;
                case "Platinum" -> counts[4]++;
                default -> { /* unknown tier, ignore */ }
            }
        }
        return counts;
    }

    /**
     * One reward name paired with how many times it's been redeemed -
     * same counting-algorithm pattern as CategoryCount, just grouped by
     * individual reward instead of category.
     */
    public static class RewardPopularity {
        public final String rewardName;
        public int count;

        public RewardPopularity(String rewardName) {
            this.rewardName = rewardName;
            this.count = 0;
        }
    }

    public ListInterface<RewardPopularity> getRewardPopularity() {
        ListInterface<RewardPopularity> stats = new DoublyLinkedList<>();

        for (int i = 1; i <= redemptionList.size(); i++) {
            RewardRedemption redemption = redemptionList.getEntry(i);
            Reward reward = findRewardById(redemption.getRewardId());
            String name = (reward != null) ? reward.getRewardName() : "Unknown Reward";

            RewardPopularity existing = null;
            for (int j = 1; j <= stats.size(); j++) {
                RewardPopularity p = stats.getEntry(j);
                if (p.rewardName.equalsIgnoreCase(name)) {
                    existing = p;
                    break;
                }
            }

            if (existing != null) {
                existing.count++;
            } else {
                RewardPopularity newP = new RewardPopularity(name);
                newP.count = 1;
                stats.add(newP);
            }
        }

        return stats;
    }

    // ───────────────────── System Statistics ─────────────────────

    public int getTotalMembers() {
        return memberController.getAll().size();
    }

    public int getTotalRewardTypes() {
        return rewardList.size();
    }

    public int getTotalPointsAcrossMembers() {
        int total = 0;
        ListInterface<Member> all = memberController.getAll();
        for (int i = 1; i <= all.size(); i++) {
            total += all.getEntry(i).getPoints();
        }
        return total;
    }

    /**
     * The category with the most redemptions, or "N/A" if there aren't any yet.
     */
    public String getMostPopularCategory() {
        ListInterface<CategoryCount> stats = getRedemptionStatsByCategory();
        if (stats.isEmpty()) {
            return "N/A";
        }

        CategoryCount best = stats.getEntry(1);
        for (int i = 2; i <= stats.size(); i++) {
            CategoryCount c = stats.getEntry(i);
            if (c.count > best.count) {
                best = c;
            }
        }
        return best.category;
    }
}