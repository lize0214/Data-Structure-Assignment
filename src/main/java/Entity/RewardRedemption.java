package Entity;

import java.time.LocalDate;

/**
 * RewardRedemption.java
 * Module-specific entity for the Loyalty & Rewards Service.
 * Records a single instance of a Member redeeming a Reward.
 */
public class RewardRedemption {

    private String redemptionId;
    private String memberId;
    private String rewardId;
    private int redeemedPoints;
    private LocalDate redeemDate;

    public RewardRedemption() {
    }

    public RewardRedemption(String redemptionId, String memberId, String rewardId,
                             int redeemedPoints, LocalDate redeemDate) {
        this.redemptionId = redemptionId;
        this.memberId = memberId;
        this.rewardId = rewardId;
        this.redeemedPoints = redeemedPoints;
        this.redeemDate = redeemDate;
    }

    public String getRedemptionId() {
        return redemptionId;
    }

    public void setRedemptionId(String redemptionId) {
        this.redemptionId = redemptionId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getRewardId() {
        return rewardId;
    }

    public void setRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    public int getRedeemedPoints() {
        return redeemedPoints;
    }

    public void setRedeemedPoints(int redeemedPoints) {
        this.redeemedPoints = redeemedPoints;
    }

    public LocalDate getRedeemDate() {
        return redeemDate;
    }

    public void setRedeemDate(LocalDate redeemDate) {
        this.redeemDate = redeemDate;
    }

    // Format: redemptionId,memberId,rewardId,redeemedPoints,redeemDate(ISO yyyy-MM-dd)
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

    public String toCsvLine() {
        return redemptionId + "," + memberId + "," + rewardId + "," + redeemedPoints + "," + redeemDate;
    }

    @Override
    public String toString() {
        return "RewardRedemption{" + redemptionId + ", member:" + memberId + ", reward:" + rewardId
                + ", pts:" + redeemedPoints + ", date:" + redeemDate + "}";
    }
}