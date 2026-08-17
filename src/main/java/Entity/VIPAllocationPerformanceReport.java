package Entity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Chua Li Ze
 */
public class VIPAllocationPerformanceReport {

    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final List<VIPAllocationRecord> records;
    private final Map<String, Integer> tierCounts;
    private final Map<String, Integer> roomTypeCounts;
    private final double averageWaitingMinutes;
    private final long longestWaitingMinutes;
    private final int preferenceRequestCount;
    private final int preferenceMatchCount;

    public VIPAllocationPerformanceReport(LocalDate fromDate, LocalDate toDate,
            List<VIPAllocationRecord> records, Map<String, Integer> tierCounts,
            Map<String, Integer> roomTypeCounts, double averageWaitingMinutes,
            long longestWaitingMinutes, int preferenceRequestCount,
            int preferenceMatchCount) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.records = List.copyOf(records);
        this.tierCounts = Collections.unmodifiableMap(tierCounts);
        this.roomTypeCounts = Collections.unmodifiableMap(roomTypeCounts);
        this.averageWaitingMinutes = averageWaitingMinutes;
        this.longestWaitingMinutes = longestWaitingMinutes;
        this.preferenceRequestCount = preferenceRequestCount;
        this.preferenceMatchCount = preferenceMatchCount;
    }

    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public List<VIPAllocationRecord> getRecords() { return records; }
    public Map<String, Integer> getTierCounts() { return tierCounts; }
    public Map<String, Integer> getRoomTypeCounts() { return roomTypeCounts; }
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
}
