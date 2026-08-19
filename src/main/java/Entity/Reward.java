package Entity;

/**
 * Represents a reward that a member can redeem using points.
 *
 * @author Tan Pei Xing
 */
public class Reward {

    private String rewardId;
    private String rewardName;
    private String category;
    private int pointsRequired;
    private int quantity;

    // Creates an empty reward.
    public Reward() {
    }

    // Creates a reward with the given details.
    public Reward(String rewardId, String rewardName, String category, int pointsRequired, int quantity) {
        this.rewardId = rewardId;
        this.rewardName = rewardName;
        this.category = category;
        this.pointsRequired = pointsRequired;
        this.quantity = quantity;
    }

    // Returns the reward ID.
    public String getRewardId() {
        return rewardId;
    }

    // Sets the reward ID.
    public void setRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    // Returns the reward name.
    public String getRewardName() {
        return rewardName;
    }

    // Sets the reward name.
    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    // Returns the reward category.
    public String getCategory() {
        return category;
    }

    // Sets the reward category.
    public void setCategory(String category) {
        this.category = category;
    }

    // Returns the points required for redemption.
    public int getPointsRequired() {
        return pointsRequired;
    }

    // Sets the points required for redemption.
    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    // Returns the available quantity.
    public int getQuantity() {
        return quantity;
    }

    // Sets the available quantity.
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Creates a Reward object from a CSV line.
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

    // Converts the reward to CSV format.
    public String toCsvLine() {
        return rewardId + "," + rewardName + "," + category + "," + pointsRequired + "," + quantity;
    }

    // Returns the reward details as a string.
    @Override
    public String toString() {
        return "Reward{" + rewardId + ", " + rewardName + ", " + category
                + ", pts:" + pointsRequired + ", qty:" + quantity + "}";
    }
}
