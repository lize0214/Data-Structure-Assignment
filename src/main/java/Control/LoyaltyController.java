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
import java.time.temporal.ChronoUnit;

/*
 * Author: Tan Pei Xing
 */
public class LoyaltyController {

    private static final String REWARD_FILE = "data/rewards.txt";
    private static final String REDEMPTION_FILE = "data/redemptions.txt";
    private static final String TRANSACTION_FILE = "data/transactions.txt";
    private static final int GOLD_MIN = 1000;
    private static final int ELITE_MIN = 3000;
    private static final int DIAMOND_MIN = 6000;
    private static final int PLATINUM_MIN = 10000;
    private static final int LOW_STOCK_THRESHOLD = 3;
    private static final int POINTS_VALID_MONTHS = 12;
    private static final int EXPIRY_WARNING_DAYS = 30;

    private final ListInterface<Reward> rewardList;
    private final ListInterface<RewardRedemption> redemptionList;
    private final ListInterface<PointsTransaction> transactionList;
    private final MemberController memberController;

    // Initializes the loyalty controller.
    public LoyaltyController(MemberController memberController) {
        this.memberController = memberController;
        this.rewardList = new DoublyLinkedList<>();
        this.redemptionList = new DoublyLinkedList<>();
        this.transactionList = new DoublyLinkedList<>();
        loadRewardsFromFile();
        loadRedemptionsFromFile();
        loadTransactionsFromFile();
        processExpiredPoints();
    }

    // Loads rewards from the file.
    private void loadRewardsFromFile() {
        String[] lines = FileUtility.readLines(REWARD_FILE);
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                rewardList.add(Reward.fromCsvLine(line));
            }
        }
    }

    // Saves rewards to the file.
    private void saveRewardsToFile() {
        String[] lines = new String[rewardList.size()];
        for (int i = 0; i < rewardList.size(); i++) {
            lines[i] = rewardList.getEntry(i + 1).toCsvLine();
        }
        FileUtility.writeAllLines(REWARD_FILE, lines);
    }

    // Loads redemption records.
    private void loadRedemptionsFromFile() {
        String[] lines = FileUtility.readLines(REDEMPTION_FILE);
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                redemptionList.add(RewardRedemption.fromCsvLine(line));
            }
        }
    }

    // Saves redemption records.
    private void saveRedemptionsToFile() {
        String[] lines = new String[redemptionList.size()];
        for (int i = 0; i < redemptionList.size(); i++) {
            lines[i] = redemptionList.getEntry(i + 1).toCsvLine();
        }
        FileUtility.writeAllLines(REDEMPTION_FILE, lines);
    }

    // Loads point transactions.
    private void loadTransactionsFromFile() {
        String[] lines = FileUtility.readLines(TRANSACTION_FILE);
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                transactionList.add(PointsTransaction.fromCsvLine(line));
            }
        }
    }

    // Saves point transactions.
    private void saveTransactionsToFile() {
        String[] lines = new String[transactionList.size()];
        for (int i = 0; i < transactionList.size(); i++) {
            lines[i] = transactionList.getEntry(i + 1).toCsvLine();
        }
        FileUtility.writeAllLines(TRANSACTION_FILE, lines);
    }

    // Generates the next transaction ID.
    private String generateNextTransactionId() {
        return String.format("TXN%04d", transactionList.size() + 1);
    }

    // Records a points transaction.
    private void logTransaction(String memberId, int pointsChange, String type, String note) {
        PointsTransaction transaction = new PointsTransaction(
                generateNextTransactionId(), memberId, pointsChange, type, note, LocalDate.now());
        transactionList.add(transaction);
        saveTransactionsToFile();
    }

    // Records a transaction with an expiry date.
    private void logTransactionWithExpiry(String memberId, int pointsChange, String type,
            String note, LocalDate expiryDate) {
        PointsTransaction transaction = new PointsTransaction(
                generateNextTransactionId(), memberId, pointsChange, type, note, LocalDate.now(), expiryDate);
        transactionList.add(transaction);
        saveTransactionsToFile();
    }

    // Gets a member transaction history.
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

    // Gets all transactions.
    public ListInterface<PointsTransaction> getAllTransactions() {
        return transactionList;
    }

    // Counts today's transactions.
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

    // Gets points expiring soon.
    public ListInterface<PointsTransaction> getExpiringSoonTransactions(String memberId) {
        ListInterface<PointsTransaction> result = new DoublyLinkedList<>();
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(EXPIRY_WARNING_DAYS);

        for (int i = 1; i <= transactionList.size(); i++) {
            PointsTransaction t = transactionList.getEntry(i);

            if (!"EARN".equals(t.getType()) || t.getExpiryDate() == null) {
                continue;
            }
            if (memberId != null && !t.getMemberId().equals(memberId)) {
                continue;
            }

            LocalDate expiry = t.getExpiryDate();
            boolean alreadyExpired = expiry.isBefore(today);
            boolean withinWarningWindow = !expiry.isAfter(cutoff);

            if (!alreadyExpired && withinWarningWindow) {
                result.add(t);
            }
        }
        return result;
    }

    // Gets days until expiry.
    public long getDaysUntilExpiry(PointsTransaction transaction) {
        return ChronoUnit.DAYS.between(LocalDate.now(), transaction.getExpiryDate());
    }

    // Gets the expiry warning period.
    public int getExpiryWarningDays() {
        return EXPIRY_WARNING_DAYS;
    }

    // Gets recently expired transactions.
    public ListInterface<PointsTransaction> getRecentlyExpiredTransactions(String memberId) {
        ListInterface<PointsTransaction> result = new DoublyLinkedList<>();
        LocalDate cutoff = LocalDate.now().minusDays(EXPIRY_WARNING_DAYS);

        for (int i = 1; i <= transactionList.size(); i++) {
            PointsTransaction t = transactionList.getEntry(i);

            if (!"EXPIRE".equals(t.getType())) {
                continue;
            }
            if (memberId != null && !t.getMemberId().equals(memberId)) {
                continue;
            }
            if (t.getTransactionDate().isBefore(cutoff)) {
                continue;
            }

            result.add(t);
        }
        return result;
    }

    // Stores expired points for a member.
    public static class ExpiredPointsResult {

        public final String memberId;
        public int expiredPoints;

        public ExpiredPointsResult(String memberId, int expiredPoints) {
            this.memberId = memberId;
            this.expiredPoints = expiredPoints;
        }
    }

    // Processes expired points.
    public ListInterface<ExpiredPointsResult> processExpiredPoints() {
        ListInterface<ExpiredPointsResult> results = new DoublyLinkedList<>();
        LocalDate today = LocalDate.now();
        boolean anyTransactionFlagged = false;

        for (int i = 1; i <= transactionList.size(); i++) {
            PointsTransaction t = transactionList.getEntry(i);

            if (!"EARN".equals(t.getType())) {
                continue;
            }
            if (t.getExpiryDate() == null || t.isExpiryProcessed()) {
                continue;
            }
            if (!t.getExpiryDate().isBefore(today)) {
                continue; // not expired yet
            }
            t.setExpiryProcessed(true);
            anyTransactionFlagged = true;

            int expiredAmount = t.getPointsChange();
            if (expiredAmount <= 0) {
                continue;
            }

            ExpiredPointsResult existing = null;
            for (int j = 1; j <= results.size(); j++) {
                ExpiredPointsResult r = results.getEntry(j);
                if (r.memberId.equals(t.getMemberId())) {
                    existing = r;
                    break;
                }
            }

            if (existing != null) {
                existing.expiredPoints += expiredAmount;
            } else {
                results.add(new ExpiredPointsResult(t.getMemberId(), expiredAmount));
            }
        }

        if (anyTransactionFlagged) {
            saveTransactionsToFile();
        }

        for (int i = 1; i <= results.size(); i++) {
            ExpiredPointsResult r = results.getEntry(i);
            Member member = memberController.findByKey(r.memberId);
            if (member == null) {
                continue;
            }

            int newBalance = Math.max(0, member.getPoints() - r.expiredPoints);
            int actuallyDeducted = member.getPoints() - newBalance;

            memberController.updatePoints(r.memberId, newBalance);
            checkAndApplyTierUpgrade(r.memberId);

            logTransaction(r.memberId, -actuallyDeducted, "EXPIRE", actuallyDeducted + " Points Expired");
            r.expiredPoints = actuallyDeducted;
        }

        return results;
    }

    // Generates the next member ID.
    public String generateNextMemberId() {
        return String.format("M%03d", memberController.getAll().size() + 1);
    }

    // Registers a new member.
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
            LocalDate expiry = LocalDate.now().plusMonths(POINTS_VALID_MONTHS);
            logTransactionWithExpiry(memberId, points, "EARN", "Registration Starting Points", expiry);
        }

        return ControllerResult.success(memberId + " - " + name + " - " + assignedTier
                + " tier - " + points + " starting points");
    }

    // Searches for a member by ID.
    public Member searchMemberById(String memberId) {
        return memberController.findByKey(memberId);
    }

    // Searches members by name.
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

    // Returns all members.
    public ListInterface<Member> viewAllMembers() {
        return memberController.getAll();
    }

    // Adds points to a member.
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

        long awardedPointsLong
                = Math.round(pointsEarned * multiplier);

        if (awardedPointsLong > Integer.MAX_VALUE) {
            return ControllerResult.fail(
                    "Calculated points exceed the maximum allowed value.");
        }

        long newPointsLong
                = (long) member.getPoints() + awardedPointsLong;

        if (newPointsLong > Integer.MAX_VALUE) {
            return ControllerResult.fail(
                    "Points balance would exceed the maximum allowed value.");
        }

        int awardedPoints = (int) awardedPointsLong;
        int newPoints = (int) newPointsLong;
        ControllerResult result = memberController.updatePoints(memberId, newPoints);
        if (!result.isOk()) {
            return result;
        }

        String tierBeforeUpgradeCheck = member.getTier();
        checkAndApplyTierUpgrade(memberId);

        String bonusNote = (multiplier > 1.00)
                ? " (" + tierBeforeUpgradeCheck + " tier bonus applied: x" + multiplier + ")"
                : "";

        String transactionNote = (multiplier > 1.00)
                ? "Earned Points (" + tierBeforeUpgradeCheck + " Bonus)"
                : "Earned Points";

        LocalDate expiry = LocalDate.now().plusMonths(POINTS_VALID_MONTHS);
        logTransactionWithExpiry(memberId, awardedPoints, "EARN", transactionNote, expiry);

        return ControllerResult.success("Earned " + awardedPoints + " points" + bonusNote + ". New balance: " + newPoints);
    }

    // Gets a member rank.
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

    // Gets the total member count.
    public int getTotalMemberCount() {
        return memberController.getAll().size();
    }

    // Gets the latest member redemption.
    public RewardRedemption getMostRecentRedemptionForMember(String memberId) {
        ListInterface<RewardRedemption> memberHistory = viewRedemptionHistoryByMember(memberId);
        if (memberHistory.isEmpty()) {
            return null;
        }
        return memberHistory.getEntry(memberHistory.size());
    }

    // Counts rewards in stock.
    public int getAvailableRewardCount() {
        int count = 0;
        for (int i = 1; i <= rewardList.size(); i++) {
            if (rewardList.getEntry(i).getQuantity() > 0) {
                count++;
            }
        }
        return count;
    }

    // Gets the low-stock threshold.
    public int getLowStockThreshold() {
        return LOW_STOCK_THRESHOLD;
    }

    // Gets the tier points multiplier.
    public double getTierMultiplier(String tier) {
        return switch (tier) {
            case "Gold" ->
                1.10;
            case "Elite" ->
                1.20;
            case "Diamond" ->
                1.35;
            case "Platinum" ->
                1.50;
            default ->
                1.00; // Silver / unknown
        };
    }

    // Calculates a member tier.
    public String calculateTier(int points) {
        if (points >= PLATINUM_MIN) {
            return "Platinum";
        }
        if (points >= DIAMOND_MIN) {
            return "Diamond";
        }
        if (points >= ELITE_MIN) {
            return "Elite";
        }
        if (points >= GOLD_MIN) {
            return "Gold";
        }
        return "Silver";
    }

    // Gets the points required for a tier.
    public int getTierThreshold(String tier) {
        return switch (tier) {
            case "Gold" ->
                GOLD_MIN;
            case "Elite" ->
                ELITE_MIN;
            case "Diamond" ->
                DIAMOND_MIN;
            case "Platinum" ->
                PLATINUM_MIN;
            default ->
                0; // Silver
        };
    }

    // Updates the member tier.
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
        return ControllerResult.success("Tier changed to " + correctTier);
    }

    // Gets affordable rewards.
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

    // Creates the tier progress display.
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

    // Generates the next reward ID.
    public String generateNextRewardId() {
        return String.format("R%03d", rewardList.size() + 1);
    }

    // Adds a new reward.
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
        return ControllerResult.success(rewardId + " - " + rewardName + " (" + category + ") - "
                + pointsRequired + " pts - Qty: " + quantity);
    }

    // Restocks a reward.
    public ControllerResult restockReward(String rewardId, int quantity) {

        String error
                = ValidationUtility.validatePositive(quantity, "Restock quantity");

        if (error != null) {
            return ControllerResult.fail(error);
        }

        Reward reward = findRewardById(rewardId);

        if (reward == null) {
            return ControllerResult.fail(
                    "Reward not found: " + rewardId);
        }

        long newQuantity
                = (long) reward.getQuantity() + quantity;

        if (newQuantity > Integer.MAX_VALUE) {
            return ControllerResult.fail(
                    "Stock quantity exceeds the maximum allowed value.");
        }

        reward.setQuantity((int) newQuantity);

        saveRewardsToFile();

        return ControllerResult.success(
                "Restocked " + reward.getRewardName()
                + ". Added " + quantity
                + " units. New stock: " + reward.getQuantity());
    }

    // Searches for a reward by ID.
    public Reward findRewardById(String rewardId) {
        int position = findRewardPosition(rewardId);
        return (position == -1) ? null : rewardList.getEntry(position);
    }

    // Finds a reward position.
    private int findRewardPosition(String rewardId) {
        for (int i = 1; i <= rewardList.size(); i++) {
            if (rewardList.getEntry(i).getRewardId().equals(rewardId)) {
                return i;
            }
        }
        return -1;
    }

    // Returns all rewards.
    public ListInterface<Reward> viewRewards() {
        return rewardList;
    }

    // Searches rewards by keyword.
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

    // Gets low-stock rewards.
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

    // Converts rewards to an array.
    private Reward[] toRewardArray() {
        Reward[] array = new Reward[rewardList.size()];
        for (int i = 1; i <= rewardList.size(); i++) {
            array[i - 1] = rewardList.getEntry(i);
        }
        return array;
    }

    // Compares rewards for sorting.
    private int compareRewards(Reward a, Reward b, String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" ->
                a.getRewardName().compareToIgnoreCase(b.getRewardName());
            case "stock" ->
                Integer.compare(a.getQuantity(), b.getQuantity());
            default ->
                Integer.compare(a.getPointsRequired(), b.getPointsRequired()); // "price"
        };
    }

    // Sorts the rewards.
    public ListInterface<Reward> sortRewards(String sortBy) {
        Reward[] array = toRewardArray();

        for (int i = 1; i < array.length; i++) {
            Reward current = array[i];
            int j = i - 1;

            while (j >= 0 && shouldComeAfter(array[j], current, sortBy)) {
                array[j + 1] = array[j];
                j--;
            }

            array[j + 1] = current;
        }

        ListInterface<Reward> sorted = new DoublyLinkedList<>();

        for (Reward reward : array) {
            sorted.add(reward);
        }

        return sorted;
    }

    // Checks the sorting order.
    private boolean shouldComeAfter(Reward existing, Reward current, String sortBy) {

        switch (sortBy.toLowerCase()) {

            case "price":
                return existing.getPointsRequired()
                        < current.getPointsRequired();

            case "name":
                return existing.getRewardName()
                        .compareToIgnoreCase(current.getRewardName()) > 0;

            case "stock":
                return existing.getQuantity()
                        > current.getQuantity();

            default:
                return false;
        }
    }

    // Redeems a reward.
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
        int newPoints = member.getPoints() - reward.getPointsRequired();
        ControllerResult pointsResult = memberController.updatePoints(memberId, newPoints);
        if (!pointsResult.isOk()) {
            return pointsResult;
        }
        reward.setQuantity(reward.getQuantity() - 1);
        saveRewardsToFile();
        RewardRedemption redemption = new RewardRedemption(
                redemptionId, memberId, rewardId, reward.getPointsRequired(), LocalDate.now());
        redemptionList.add(redemption);
        saveRedemptionsToFile();
        checkAndApplyTierUpgrade(memberId);

        logTransaction(memberId, -reward.getPointsRequired(), "REDEEM", "Redeemed " + reward.getRewardName());

        return ControllerResult.success("Redeemed " + reward.getRewardName()
                + " for " + reward.getPointsRequired() + " points. Remaining balance: " + newPoints);
    }

    // Returns redemption history.
    public ListInterface<RewardRedemption> viewRedemptionHistory() {
        return redemptionList;
    }

    // Gets a member redemption history.
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

    // Gets the total redemptions.
    public int getTotalRedemptions() {
        return redemptionList.size();
    }

    // Stores a redemption count by category.
    public static class CategoryCount {

        public final String category;
        public int count;

        public CategoryCount(String category) {
            this.category = category;
            this.count = 0;
        }
    }

    // Gets redemption statistics.
    public ListInterface<CategoryCount> getRedemptionStatsByCategory() {
        return getRedemptionStatsByCategory(null);
    }

    public ListInterface<CategoryCount> getRedemptionStatsByCategory(String categoryFilter) {
        ListInterface<CategoryCount> stats = new DoublyLinkedList<>();
        boolean hasFilter = categoryFilter != null && !categoryFilter.isBlank();

        for (int i = 1; i <= redemptionList.size(); i++) {
            RewardRedemption redemption = redemptionList.getEntry(i);
            Reward reward = findRewardById(redemption.getRewardId());
            String category = (reward != null) ? reward.getCategory() : "Unknown";

            if (hasFilter && !category.equalsIgnoreCase(categoryFilter)) {
                continue;
            }

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

    // Generates the next redemption ID.
    public String generateNextRedemptionId() {
        return String.format("RDM%04d", redemptionList.size() + 1);
    }

    // Gets the latest redemption.
    public RewardRedemption getLastRedemption() {
        if (redemptionList.isEmpty()) {
            return null;
        }
        return redemptionList.getEntry(redemptionList.size());
    }

    // Undoes the latest redemption.
    public ControllerResult undoLastRedemption() {
        if (redemptionList.isEmpty()) {
            return ControllerResult.fail("No redemptions to undo.");
        }

        int lastPosition = redemptionList.size();
        RewardRedemption last = redemptionList.getEntry(lastPosition);

        Member member = memberController.findByKey(last.getMemberId());

        if (member != null) {

            long refundedPointsLong
                    = (long) member.getPoints() + last.getRedeemedPoints();

            if (refundedPointsLong > Integer.MAX_VALUE) {
                return ControllerResult.fail(
                        "Refund cannot be completed because the points balance would exceed the maximum allowed value.");
            }

            int refundedPoints = (int) refundedPointsLong;

            ControllerResult pointsResult
                    = memberController.updatePoints(
                            last.getMemberId(),
                            refundedPoints);

            if (!pointsResult.isOk()) {
                return pointsResult;
            }

            checkAndApplyTierUpgrade(last.getMemberId());

            logTransaction(
                    last.getMemberId(),
                    last.getRedeemedPoints(),
                    "UNDO",
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

    // Gets the top members by points.
    public ListInterface<Member> topMembersByPoints(int topN) {
        return topMembersByPoints(topN, memberController.getAll());
    }

    // Sorts and returns top members.
    private ListInterface<Member> topMembersByPoints(int topN, ListInterface<Member> source) {
        int total = source.size();

        Member[] snapshot = new Member[total];
        for (int i = 1; i <= total; i++) {
            snapshot[i - 1] = source.getEntry(i);
        }
        for (int i = 1; i < snapshot.length; i++) {

            Member current = snapshot[i];
            int j = i - 1;

            while (j >= 0
                    && snapshot[j].getPoints() < current.getPoints()) {

                snapshot[j + 1] = snapshot[j];
                j--;
            }

            snapshot[j + 1] = current;
        }

        ListInterface<Member> topMembers = new DoublyLinkedList<>();
        int limit = Math.min(topN, snapshot.length);
        for (int i = 0; i < limit; i++) {
            topMembers.add(snapshot[i]);
        }
        return topMembers;
    }

    // Filters members by tier and points.
    public ListInterface<Member> getMembersFiltered(String tierFilter, int minPoints, int maxPoints) {
        ListInterface<Member> all = memberController.getAll();
        ListInterface<Member> filtered = new DoublyLinkedList<>();
        boolean hasTierFilter = tierFilter != null && !tierFilter.isBlank();

        for (int i = 1; i <= all.size(); i++) {
            Member m = all.getEntry(i);
            boolean tierOk = !hasTierFilter || m.getTier().equalsIgnoreCase(tierFilter);
            boolean pointsOk = m.getPoints() >= minPoints && m.getPoints() <= maxPoints;
            if (tierOk && pointsOk) {
                filtered.add(m);
            }
        }
        return filtered;
    }

    // Creates the member loyalty report.
    public ListInterface<Member> getMemberLoyaltyReport(String tierFilter, int minPoints, int maxPoints) {
        ListInterface<Member> filtered = getMembersFiltered(tierFilter, minPoints, maxPoints);
        return topMembersByPoints(filtered.size(), filtered);
    }

    // Counts members in each tier.
    public int[] membershipTierDistribution() {
        int[] counts = new int[5]; // Silver, Gold, Elite, Diamond, Platinum
        ListInterface<Member> allMembers = memberController.getAll();

        for (int i = 1; i <= allMembers.size(); i++) {
            String tier = allMembers.getEntry(i).getTier();
            switch (tier) {
                case "Silver" ->
                    counts[0]++;
                case "Gold" ->
                    counts[1]++;
                case "Elite" ->
                    counts[2]++;
                case "Diamond" ->
                    counts[3]++;
                case "Platinum" ->
                    counts[4]++;
                default -> {
                }
            }
        }
        return counts;
    }

    // Stores a reward redemption count.
    public static class RewardPopularity {

        public final String rewardName;
        public int count;

        public RewardPopularity(String rewardName) {
            this.rewardName = rewardName;
            this.count = 0;
        }
    }

    // Gets reward popularity statistics.
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

    // Gets the total member count.
    public int getTotalMembers() {
        return memberController.getAll().size();
    }

    // Gets the total reward types.
    public int getTotalRewardTypes() {
        return rewardList.size();
    }

    // Gets total points across members.
    public int getTotalPointsAcrossMembers() {
        int total = 0;
        ListInterface<Member> all = memberController.getAll();
        for (int i = 1; i <= all.size(); i++) {
            total += all.getEntry(i).getPoints();
        }
        return total;
    }

    // Gets the most popular category.
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
