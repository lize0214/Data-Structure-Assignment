package Entity;

import java.time.LocalDateTime;

/**
 * Represents a VIP member waiting in the priority room allocation queue.
 * <p>
 * Implements {@link Comparable} to define the sorting contract for the
 * max-heap priority queue:
 * <ol>
 *   <li>Higher tier priority first (Platinum=4, Diamond=3, Elite=2)</li>
 *   <li>Within the same tier, earlier registration time wins</li>
 * </ol>
 * </p>
 */
public class VIPQueueEntry implements Comparable<VIPQueueEntry> {

    // Tier weight constants — aligned with LoyaltyController point thresholds
    public static final int PRIORITY_ELITE = 2;
    public static final int PRIORITY_DIAMOND = 3;
    public static final int PRIORITY_PLATINUM = 4;

    private String memberId;
    private String memberTier;        // Elite, Diamond, or Platinum
    private int tierPriority;         // 2, 3, or 4 — higher = more priority
    private String preferredRoomType; // optional; null or empty means "any"
    private LocalDateTime registrationTime;

    /**
     * Constructs a VIP queue entry.
     *
     * @param memberId         the ID of the loyalty member (must exist in MemberController)
     * @param memberTier       the member's current tier (Elite/Diamond/Platinum)
     * @param tierPriority     numerical priority weight (2/3/4)
     * @param preferredRoomType optional preferred room type; null or empty for any
     * @param registrationTime the timestamp when the member joined the queue
     */
    public VIPQueueEntry(String memberId, String memberTier,
                         int tierPriority, String preferredRoomType, LocalDateTime registrationTime) {
        this.memberId = memberId;
        this.memberTier = memberTier;
        this.tierPriority = tierPriority;
        this.preferredRoomType = (preferredRoomType != null && !preferredRoomType.trim().isEmpty())
                ? preferredRoomType.trim() : null;
        this.registrationTime = registrationTime;
    }

    /**
     * Compares two VIP queue entries for priority ordering.
     * <p>
     * Comparison rules:
     * <ol>
     *   <li>Higher {@code tierPriority} first (Platinum > Diamond > Elite)</li>
     *   <li>If same tier, earlier {@code registrationTime} first (FIFO within tier)</li>
     * </ol>
     * </p>
     * <p>
     * Note: this uses natural ordering where "greater" = "higher priority".
     * The MaxHeap will place the "greatest" (highest-priority) element at the root.
     * </p>
     *
     * @param other the other entry to compare against
     * @return positive if this entry has higher priority, negative if lower, 0 if equal
     */
    @Override
    public int compareTo(VIPQueueEntry other) {
        // Primary: higher tierPriority = higher priority
        if (this.tierPriority != other.tierPriority) {
            return Integer.compare(this.tierPriority, other.tierPriority);
        }
        // Secondary: earlier registration = higher priority (reverse chronological)
        return other.registrationTime.compareTo(this.registrationTime);
    }

    // ───────────────────── Getters / Setters ─────────────────────

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberTier() {
        return memberTier;
    }

    public void setMemberTier(String memberTier) {
        this.memberTier = memberTier;
    }

    public int getTierPriority() {
        return tierPriority;
    }

    public void setTierPriority(int tierPriority) {
        this.tierPriority = tierPriority;
    }

    public String getPreferredRoomType() {
        return preferredRoomType;
    }

    public void setPreferredRoomType(String preferredRoomType) {
        this.preferredRoomType = (preferredRoomType != null && !preferredRoomType.trim().isEmpty())
                ? preferredRoomType.trim() : null;
    }

    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(LocalDateTime registrationTime) {
        this.registrationTime = registrationTime;
    }

    // ───────────────────── CSV Serialization ─────────────────────

    /**
     * Parses a CSV line into a VIPQueueEntry.
     * Format: memberId,tier,preferredRoomType,registrationTimestamp
     *
     * @param line the CSV line to parse
     * @return a new VIPQueueEntry instance
     * @throws IllegalArgumentException if the line format is invalid
     */
    public static VIPQueueEntry fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid VIPQueueEntry data format: " + line);
        }
        String tier = parts[1].trim();
        String roomType = parts[2].trim();
        return new VIPQueueEntry(
                parts[0].trim(),
                tier,
                tierToPriority(tier),
                roomType.isEmpty() ? null : roomType,
                LocalDateTime.parse(parts[3].trim())
        );
    }

    /**
     * Converts this entry to a CSV line.
     * Format: memberId,tier,preferredRoomType,registrationTimestamp
     *
     * @return the CSV representation
     */
    public String toCsvLine() {
        return memberId + ","
                + memberTier + ","
                + (preferredRoomType != null ? preferredRoomType : "") + ","
                + registrationTime;
    }

    // ───────────────────── Tier Helpers ─────────────────────

    /**
     * Maps a tier name to its numerical priority weight.
     *
     * @param tier the tier name (Elite/Diamond/Platinum)
     * @return the corresponding priority weight (2/3/4), or 0 for unknown tiers
     */
    public static int tierToPriority(String tier) {
        if (tier == null) return 0;
        return switch (tier) {
            case "Elite"    -> PRIORITY_ELITE;
            case "Diamond"  -> PRIORITY_DIAMOND;
            case "Platinum" -> PRIORITY_PLATINUM;
            default         -> 0;
        };
    }

    /**
     * Checks whether a tier is eligible for VIP priority allocation.
     *
     * @param tier the tier name to check
     * @return true if the tier is Elite, Diamond, or Platinum
     */
    public static boolean isVIPTier(String tier) {
        return "Elite".equals(tier) || "Diamond".equals(tier) || "Platinum".equals(tier);
    }

    @Override
    public String toString() {
        return "VIPQueueEntry{Member:" + memberId
                + ", " + memberTier + " (priority=" + tierPriority + ")"
                + (preferredRoomType != null ? ", prefers=" + preferredRoomType : "")
                + ", registered=" + registrationTime + "}";
    }
}
