package Entity;

import java.time.LocalDate;

/**
 * Represents a member's reward redemption.
 *
 * @author Tan Pei Xing
 */
public class RewardRedemption {

    private String redemptionId;
    private String memberId;
    private String rewardId;
    private int redeemedPoints;
    private LocalDate redeemDate;

    // Creates an empty redemption.
    public RewardRedemption() {
    }

    // Creates a redemption with the given details.
    public RewardRedemption(String redemptionId, String memberId, String rewardId,
            int redeemedPoints, LocalDate redeemDate) {
        this.redemptionId = redemptionId;
        this.memberId = memberId;
        this.rewardId = rewardId;
        this.redeemedPoints = redeemedPoints;
        this.redeemDate = redeemDate;
    }

    // Returns the redemption ID.
    public String getRedemptionId() {
        return redemptionId;
    }

    // Sets the redemption ID.
    public void setRedemptionId(String redemptionId) {
        this.redemptionId = redemptionId;
    }

    // Returns the member ID.
    public String getMemberId() {
        return memberId;
    }

    // Sets the member ID.
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    // Returns the reward ID.
    public String getRewardId() {
        return rewardId;
    }

    // Sets the reward ID.
    public void setRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    // Returns the redeemed points.
    public int getRedeemedPoints() {
        return redeemedPoints;
    }

    // Sets the redeemed points.
    public void setRedeemedPoints(int redeemedPoints) {
        this.redeemedPoints = redeemedPoints;
    }

    // Returns the redemption date.
    public LocalDate getRedeemDate() {
        return redeemDate;
    }

    // Sets the redemption date.
    public void setRedeemDate(LocalDate redeemDate) {
        this.redeemDate = redeemDate;
    }

    // Creates a redemption object from a CSV line.
    public static RewardRedemption fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid RewardRedemption data format: " + line);
        }
        return new RewardRedemption(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                Integer.parseInt(parts[3].trim()),
                LocalDate.parse(parts[4].trim())
        );
    }

    // Converts the redemption to CSV format.
    public String toCsvLine() {
        return redemptionId + "," + memberId + "," + rewardId + "," + redeemedPoints + "," + redeemDate;
    }

    // Returns the redemption details as a string.
    @Override
    public String toString() {
        return "RewardRedemption{" + redemptionId + ", member:" + memberId + ", reward:" + rewardId
                + ", pts:" + redeemedPoints + ", date:" + redeemDate + "}";
    }
}
