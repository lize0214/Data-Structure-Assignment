package Entity;

import ADT.ArrayList;
import ADT.HashTable;
import ADT.HashTableInterface;
import ADT.ListInterface;
import java.time.LocalDateTime;

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
    private final ListInterface<QueueRow> queueRows;
    private final ListInterface<RoomDemandRow> roomDemandRows;
    private final HashTableInterface<String, Integer> tierCounts;
    private final double averageWaitingMinutes;
    private final long longestWaitingMinutes;

    public VIPQueueDemandReport(LocalDateTime generatedAt, ListInterface<QueueRow> queueRows,
            ListInterface<RoomDemandRow> roomDemandRows,
            HashTableInterface<String, Integer> tierCounts,
            double averageWaitingMinutes, long longestWaitingMinutes) {
        this.generatedAt = generatedAt;
        this.queueRows = copyList(queueRows);
        this.roomDemandRows = copyList(roomDemandRows);
        this.tierCounts = copyTable(tierCounts);
        this.averageWaitingMinutes = averageWaitingMinutes;
        this.longestWaitingMinutes = longestWaitingMinutes;
    }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public ListInterface<QueueRow> getQueueRows() { return copyList(queueRows); }
    public ListInterface<RoomDemandRow> getRoomDemandRows() { return copyList(roomDemandRows); }
    public HashTableInterface<String, Integer> getTierCounts() { return copyTable(tierCounts); }
    public int getTotalWaiting() { return queueRows.size(); }
    public double getAverageWaitingMinutes() { return averageWaitingMinutes; }
    public long getLongestWaitingMinutes() { return longestWaitingMinutes; }

    private static <T> ListInterface<T> copyList(ListInterface<T> source) {
        ListInterface<T> copy = new ArrayList<>(source.size());
        for (int i = 1; i <= source.size(); i++) copy.add(source.getEntry(i));
        return copy;
    }

    private static <K, V> HashTableInterface<K, V> copyTable(
            HashTableInterface<K, V> source) {
        HashTableInterface<K, V> copy = new HashTable<>();
        ListInterface<K> keys = source.keys();
        for (int i = 1; i <= keys.size(); i++) {
            K key = keys.getEntry(i);
            copy.insert(key, source.search(key));
        }
        return copy;
    }
}
