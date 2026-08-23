package Entity;

import ADT.ArrayList;
import ADT.HashTable;
import ADT.HashTableInterface;
import ADT.ListInterface;
import java.time.LocalDate;

/**
 * @author Chua Li Ze
 */
public class VIPAllocationPerformanceReport {

    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final ListInterface<VIPAllocationRecord> records;
    private final HashTableInterface<String, Integer> tierCounts;
    private final HashTableInterface<String, Integer> roomTypeCounts;
    private final double averageWaitingMinutes;
    private final long longestWaitingMinutes;
    private final int preferenceRequestCount;
    private final int preferenceMatchCount;

    public VIPAllocationPerformanceReport(LocalDate fromDate, LocalDate toDate,
            ListInterface<VIPAllocationRecord> records,
            HashTableInterface<String, Integer> tierCounts,
            HashTableInterface<String, Integer> roomTypeCounts,
            double averageWaitingMinutes,
            long longestWaitingMinutes, int preferenceRequestCount,
            int preferenceMatchCount) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.records = copyList(records);
        this.tierCounts = copyTable(tierCounts);
        this.roomTypeCounts = copyTable(roomTypeCounts);
        this.averageWaitingMinutes = averageWaitingMinutes;
        this.longestWaitingMinutes = longestWaitingMinutes;
        this.preferenceRequestCount = preferenceRequestCount;
        this.preferenceMatchCount = preferenceMatchCount;
    }

    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public ListInterface<VIPAllocationRecord> getRecords() { return copyList(records); }
    public HashTableInterface<String, Integer> getTierCounts() { return copyTable(tierCounts); }
    public HashTableInterface<String, Integer> getRoomTypeCounts() { return copyTable(roomTypeCounts); }
    public int getTotalAllocations() { return records.size(); }
    public double getAverageWaitingMinutes() { return averageWaitingMinutes; }
    public long getLongestWaitingMinutes() { return longestWaitingMinutes; }
    public int getPreferenceRequestCount() { return preferenceRequestCount; }
    public int getPreferenceMatchCount() { return preferenceMatchCount; }

    public double getPreferenceMatchRate() {
        return preferenceRequestCount == 0
                ? 0.0
                : preferenceMatchCount * 100.0 / preferenceRequestCount;
    }

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
