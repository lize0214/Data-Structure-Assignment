package Entity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Chua Li Ze
 */
public class VIPQueueDemandReport {

    public record QueueRow(int rank, String memberId, String memberName,
            String memberTier, int priority, String preferredRoomType,
            LocalDateTime registrationTime, long waitingMinutes) { }

    public record RoomDemandRow(String roomType, int demand,
            int available, int shortage) { }

    private final LocalDateTime generatedAt;
    private final List<QueueRow> queueRows;
    private final List<RoomDemandRow> roomDemandRows;
    private final Map<String, Integer> tierCounts;
    private final double averageWaitingMinutes;
    private final long longestWaitingMinutes;

    public VIPQueueDemandReport(LocalDateTime generatedAt, List<QueueRow> queueRows,
            List<RoomDemandRow> roomDemandRows, Map<String, Integer> tierCounts,
            double averageWaitingMinutes, long longestWaitingMinutes) {
        this.generatedAt = generatedAt;
        this.queueRows = List.copyOf(queueRows);
        this.roomDemandRows = List.copyOf(roomDemandRows);
        this.tierCounts = Collections.unmodifiableMap(tierCounts);
        this.averageWaitingMinutes = averageWaitingMinutes;
        this.longestWaitingMinutes = longestWaitingMinutes;
    }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public List<QueueRow> getQueueRows() { return queueRows; }
    public List<RoomDemandRow> getRoomDemandRows() { return roomDemandRows; }
    public Map<String, Integer> getTierCounts() { return tierCounts; }
    public int getTotalWaiting() { return queueRows.size(); }
    public double getAverageWaitingMinutes() { return averageWaitingMinutes; }
    public long getLongestWaitingMinutes() { return longestWaitingMinutes; }
}
