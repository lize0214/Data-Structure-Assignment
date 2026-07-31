package Control;

import ADT.DoublyLinkedList;
import ADT.ListInterface;
import Entity.Member;
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

    // Tier point thresholds (inclusive lower bound). Adjust freely — kept
    // in one place so the whole tier system can be tuned from here.
    private static final int GOLD_MIN = 1000;
    private static final int ELITE_MIN = 3000;
    private static final int DIAMOND_MIN = 6000;
    private static final int PLATINUM_MIN = 10000;

    private final ListInterface<Reward> rewardList;
    private final ListInterface<RewardRedemption> redemptionList;
    private final MemberController memberController;

    public LoyaltyController(MemberController memberController) {
        this.memberController = memberController;
        this.rewardList = new DoublyLinkedList<>();
        this.redemptionList = new DoublyLinkedList<>();
        loadRewardsFromFile();
        loadRedemptionsFromFile();
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

    // ───────────────────── Member Management (delegates to shared MemberController) ─────────────────────

    public ControllerResult registerMember(String memberId, String name, String tier, int points) {
        ValidationUtility.ValidationAccumulator acc = new ValidationUtility.ValidationAccumulator();
        acc.check(ValidationUtility.validateRequired(memberId, "Member ID"));
        acc.check(ValidationUtility.validateRequired(name, "Name"));
        acc.check(ValidationUtility.validateMemberTier(tier));
        acc.check(ValidationUtility.validateNonNegative(points, "Points"));
        if (acc.hasErrors()) {
            return ControllerResult.fail(acc.getErrorMessage());
        }

        Member newMember = new Member(memberId, name, tier, points);
        return memberController.add(newMember);
    }

    public ControllerResult updateMember(String memberId, String name, String tier, int points) {
        return memberController.update(memberId, name, tier, points);
    }

    public ControllerResult deleteMember(String memberId) {
        return memberController.delete(memberId);
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

        int newPoints = member.getPoints() + pointsEarned;
        ControllerResult result = memberController.updatePoints(memberId, newPoints);
        if (!result.isOk()) {
            return result;
        }

        checkAndApplyTierUpgrade(memberId);
        return ControllerResult.success("Earned " + pointsEarned + " points. New balance: " + newPoints);
    }

    public int checkPointsBalance(String memberId) {
        Member member = memberController.findByKey(memberId);
        return (member == null) ? -1 : member.getPoints();
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

    public ControllerResult deleteReward(String rewardId) {
        int position = findRewardPosition(rewardId);
        if (position == -1) {
            return ControllerResult.fail("Reward not found: " + rewardId);
        }
        rewardList.remove(position);
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
}