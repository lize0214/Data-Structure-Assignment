package Entity;

import java.time.LocalDateTime;

/**
 * @author Chua Li Ze
 */
public class VIPAllocationRecord {

    private final String confirmationNo;
    private final String memberId;
    private final String memberTier;
    private final String preferredRoomType;
    private final String allocatedRoomNo;
    private final String allocatedRoomType;
    private final LocalDateTime queueRegistrationTime;
    private final LocalDateTime allocationTime;
    private final long waitingMinutes;
    private final boolean preferenceMatched;

    public VIPAllocationRecord(String confirmationNo, String memberId, String memberTier,
            String preferredRoomType, String allocatedRoomNo, String allocatedRoomType,
            LocalDateTime queueRegistrationTime, LocalDateTime allocationTime,
            long waitingMinutes, boolean preferenceMatched) {
        this.confirmationNo = confirmationNo;
        this.memberId = memberId;
        this.memberTier = memberTier;
        this.preferredRoomType = normalizeOptional(preferredRoomType);
        this.allocatedRoomNo = allocatedRoomNo;
        this.allocatedRoomType = allocatedRoomType;
        this.queueRegistrationTime = queueRegistrationTime;
        this.allocationTime = allocationTime;
        this.waitingMinutes = Math.max(0, waitingMinutes);
        this.preferenceMatched = preferenceMatched;
    }

    public String getConfirmationNo() { return confirmationNo; }
    public String getMemberId() { return memberId; }
    public String getMemberTier() { return memberTier; }
    public String getPreferredRoomType() { return preferredRoomType; }
    public String getAllocatedRoomNo() { return allocatedRoomNo; }
    public String getAllocatedRoomType() { return allocatedRoomType; }
    public LocalDateTime getQueueRegistrationTime() { return queueRegistrationTime; }
    public LocalDateTime getAllocationTime() { return allocationTime; }
    public long getWaitingMinutes() { return waitingMinutes; }
    public boolean isPreferenceMatched() { return preferenceMatched; }
    public boolean hasRoomPreference() { return preferredRoomType != null; }

    /**
     * CSV format: confirmationNo,memberId,tier,preferredRoomType,roomNo,roomType,
     * queueRegistrationTime,allocationTime,waitingMinutes,preferenceMatched
     */
    public String toCsvLine() {
        return confirmationNo + "," + memberId + "," + memberTier + ","
                + (preferredRoomType == null ? "" : preferredRoomType) + ","
                + allocatedRoomNo + "," + allocatedRoomType + ","
                + queueRegistrationTime + "," + allocationTime + ","
                + waitingMinutes + "," + preferenceMatched;
    }

    public static VIPAllocationRecord fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != 10) {
            throw new IllegalArgumentException("Invalid VIP allocation history format: " + line);
        }
        return new VIPAllocationRecord(
                parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(),
                parts[4].trim(), parts[5].trim(),
                LocalDateTime.parse(parts[6].trim()),
                LocalDateTime.parse(parts[7].trim()),
                Long.parseLong(parts[8].trim()),
                Boolean.parseBoolean(parts[9].trim()));
    }

    private static String normalizeOptional(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
