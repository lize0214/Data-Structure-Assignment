package Entity;

/**
 * Reward.java
 * Module-specific entity for the Loyalty & Rewards Service.
 * Represents an item a Member can redeem using reward points.
 */
public class Reward {

    private String rewardId;
    private String rewardName;
    private String category;
    private int pointsRequired;
    private int quantity;

    public Reward() {
    }

    public Reward(String rewardId, String rewardName, String category, int pointsRequired, int quantity) {
        this.rewardId = rewardId;
        this.rewardName = rewardName;
        this.category = category;
        this.pointsRequired = pointsRequired;
        this.quantity = quantity;
    }

    public String getRewardId() {
        return rewardId;
    }

    public void setRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Format: rewardId,rewardName,category,pointsRequired,quantity
    public static Reward fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid Reward data format: " + line);
        }
        return new Reward(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                Integer.parseInt(parts[3].trim()),
                Integer.parseInt(parts[4].trim())
        );
    }

    public String toCsvLine() {
        return rewardId + "," + rewardName + "," + category + "," + pointsRequired + "," + quantity;
    }

    @Override
    public String toString() {
        return "Reward{" + rewardId + ", " + rewardName + ", " + category
                + ", pts:" + pointsRequired + ", qty:" + quantity + "}";
    }
}