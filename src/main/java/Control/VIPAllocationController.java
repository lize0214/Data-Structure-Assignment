package Control;

import ADT.HeapPriorityQueue;
import ADT.HashTable;
import ADT.HashTableInterface;
import ADT.ListInterface;
import Entity.*;
import Utility.ControllerResult;
import Utility.FileUtility;
import Utility.ValidationUtility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
/**
 * @author Chua Li Ze
 */

/**
 * Controller for VIP priority room allocation using a max-heap-based
 * priority queue (non-linear ADT).
 * High-tier members (Elite, Diamond, Platinum) bypass the standard
 * FIFO allocation queue. The highest-tier member always receives
 * the next available room. Within the same tier, earlier registration
 * time takes precedence.
 */
public class VIPAllocationController {

    @FunctionalInterface
    private interface ItemComparator<T> {
        int compare(T first, T second);
    }

    private static final String VIP_QUEUE_FILE = "data/vip_queue.txt";
    private static final String VIP_ALLOCATION_HISTORY_FILE = "data/vip_allocation_history.txt";
    private static final String[] ROOM_TYPES = {"Single", "Deluxe", "Suite", "Presidential"};

    // Room statuses considered allocatable
    private static final String[] ALLOCATABLE_STATUSES = {"Available", "ReadyForCheckIn", "Inspected"};

    private final HeapPriorityQueue<VIPQueueEntry> vipQueue;
    private final MemberController memberController;
    private final RoomController roomController;
    private final BookingController bookingController;
    private final ListInterface<VIPAllocationRecord> allocationHistory;

    /**
     * Constructs the VIP allocation controller with all required dependencies.
     *
     * @param memberController  controller for member lookups
     * @param roomController    controller for room queries and status updates
     * @param guestController   retained for compatibility; VIP bookings now use Member directly
     * @param bookingController controller for creating booking records
     */
    public VIPAllocationController(MemberController memberController,
                                   RoomController roomController,
                                   GuestController guestController,
                                   BookingController bookingController) {
        this(memberController, roomController, bookingController);
    }

    public VIPAllocationController(MemberController memberController,
                                   RoomController roomController,
                                   BookingController bookingController) {
        this.memberController = memberController;
        this.roomController = roomController;
        this.bookingController = bookingController;
        this.vipQueue = new HeapPriorityQueue<>();
        this.allocationHistory = new ADT.ArrayList<>();
        loadQueueFromFile();
        loadAllocationHistoryFromFile();
    }

    // ───────────────────── Enqueue ─────────────────────

    /**
     * Adds a VIP member to the priority allocation queue.
     * <p>
     * Validates that the member exists and has a VIP tier
     * (Elite/Diamond/Platinum), and the preferred room type (if provided) is valid.
     * </p>
     *
     * @param memberId         the ID of the loyalty member (must exist in MemberController)
     * @param preferredRoomType optional preferred room type (null or empty for any)
     * @return ControllerResult indicating success or failure
     */
    public ControllerResult enqueueVIPMember(String memberId, String preferredRoomType) {
        ValidationUtility.ValidationAccumulator acc = new ValidationUtility.ValidationAccumulator();
        acc.check(ValidationUtility.validateRequired(memberId, "Member ID"));

        // Validate member exists and has VIP tier
        Member member = null;
        if (memberId != null && !memberId.trim().isEmpty()) {
            member = memberController.findByKey(memberId.trim());
            if (member == null) {
                acc.check("Member not found: " + memberId);
            }
        }

        // Validate preferred room type (optional — null/empty is valid)
        acc.check(ValidationUtility.validateRoomType(preferredRoomType));

        if (acc.hasErrors()) {
            return ControllerResult.fail(acc.getErrorMessage());
        }

        // Validate VIP tier eligibility
        String tier = member.getTier();
        String vipTierError = ValidationUtility.validateVIPAllocationTier(tier);
        if (vipTierError != null) {
            return ControllerResult.fail(vipTierError);
        }

        // Check for duplicate — same member already in queue
        if (findEntryByMemberId(memberId.trim()) != null) {
            return ControllerResult.fail("Member " + memberId + " is already in the VIP queue");
        }

        int tierPriority = VIPQueueEntry.tierToPriority(tier);
        VIPQueueEntry entry = new VIPQueueEntry(
                memberId.trim(), tier, tierPriority, preferredRoomType, LocalDateTime.now());

        vipQueue.enqueue(entry);
        saveQueueToFile();
        return ControllerResult.success("VIP member " + memberId + " (" + tier + ") added to priority queue");
    }

    // ───────────────────── Allocation ─────────────────────

    /**
     * Allocates a room to the highest-priority VIP member in the queue.
     * <p>
     * Flow:
     * <ol>
     *   <li>Check queue is not empty</li>
     *   <li>Dequeue the highest-priority entry</li>
     *   <li>Find an available room (prefer matching preferredRoomType)</li>
     *   <li>Resolve the Member that owns the VIP queue entry</li>
     *   <li>Update room status to Occupied</li>
     *   <li>Create a booking record</li>
     *   <li>Persist queue to file</li>
     * </ol>
     * </p>
     *
     * @return ControllerResult with allocation details on success
     */
    public ControllerResult allocateNextVIPRoom() {
        if (vipQueue.isEmpty()) {
            return ControllerResult.fail("No VIP members in the priority queue");
        }

        // Find an allocatable room
        Room availableRoom = findAvailableRoom(null);
        if (availableRoom == null) {
            return ControllerResult.fail("No available rooms for allocation");
        }

        // Dequeue highest-priority member
        VIPQueueEntry entry = vipQueue.dequeue();

        // If member has a preferred room type, try to find a matching room
        if (entry.getPreferredRoomType() != null) {
            Room preferredRoom = findAvailableRoom(entry.getPreferredRoomType());
            if (preferredRoom != null) {
                availableRoom = preferredRoom;
            }
            // If no matching room, fall back to the already-found room
        }

        // A VIP booking is owned directly by the loyalty Member.
        Member member = memberController.findByKey(entry.getMemberId());
        if (member == null) {
            vipQueue.enqueue(entry);
            return ControllerResult.fail("Member no longer exists: " + entry.getMemberId());
        }

        // Update room status to Occupied
        ControllerResult roomResult = roomController.updateStatus(availableRoom.getRoomNo(), "Occupied");
        if (!roomResult.isOk()) {
            // Re-enqueue the member since allocation failed
            vipQueue.enqueue(entry);
            return ControllerResult.fail("Failed to update room status: " + roomResult.getMessage());
        }

        // Create booking record
        String confirmationNo = generateConfirmationNo();
        LocalDate today = LocalDate.now();
        Booking booking = new Booking(confirmationNo, member, availableRoom,
                today, today.plusDays(1), "CheckedIn", BookingType.VIP_ALLOCATION);
        ControllerResult bookingResult = bookingController.add(booking);
        if (!bookingResult.isOk()) {
            // Rollback room status
            roomController.updateStatus(availableRoom.getRoomNo(), "Available");
            vipQueue.enqueue(entry);
            return ControllerResult.fail("Failed to create booking: " + bookingResult.getMessage());
        }

        LocalDateTime allocationTime = LocalDateTime.now();
        boolean preferenceMatched = entry.getPreferredRoomType() != null
                && entry.getPreferredRoomType().equalsIgnoreCase(availableRoom.getRoomType());
        VIPAllocationRecord historyRecord = new VIPAllocationRecord(
                confirmationNo,
                entry.getMemberId(),
                entry.getMemberTier(),
                entry.getPreferredRoomType(),
                availableRoom.getRoomNo(),
                availableRoom.getRoomType(),
                entry.getRegistrationTime(),
                allocationTime,
                waitingMinutesBetween(entry.getRegistrationTime(), allocationTime),
                preferenceMatched);
        allocationHistory.add(historyRecord);
        FileUtility.appendLine(VIP_ALLOCATION_HISTORY_FILE, historyRecord.toCsvLine());

        saveQueueToFile();

        String message = String.format(
                "Allocated Room %s (%s) to %s [Member:%s, %s, Priority:%s] | Confirmation: %s",
                availableRoom.getRoomNo(),
                availableRoom.getRoomType(),
                member.getName(),
                entry.getMemberId(),
                entry.getMemberTier(),
                entry.getMemberTier(),
                confirmationNo
        );
        return ControllerResult.success(message);
    }

    // ───────────────────── Queue View ─────────────────────

    /**
     * Returns a snapshot of the VIP queue sorted by priority (highest first).
     * This does not modify the queue — entries are dequeued, collected,
     * and re-enqueued.
     *
     * @return a list of VIPQueueEntry in priority order (highest first)
     */
    public ListInterface<VIPQueueEntry> viewQueue() {
        ListInterface<VIPQueueEntry> snapshot = new ADT.ArrayList<>();
        ListInterface<VIPQueueEntry> temp = new ADT.ArrayList<>();

        // Dequeue all into temp (comes out highest-priority first)
        while (!vipQueue.isEmpty()) {
            VIPQueueEntry entry = vipQueue.dequeue();
            snapshot.add(entry);
            temp.add(entry);
        }

        // Re-enqueue all entries
        for (int i = 1; i <= temp.size(); i++) vipQueue.enqueue(temp.getEntry(i));

        return snapshot; // already in priority order from dequeue sequence
    }

    // ───────────────────── Dequeue / Cancel ─────────────────────

    /**
     * Removes a specific VIP member from the priority queue (cancellation).
     *
     * @param memberId the member ID to remove
     * @return ControllerResult indicating success or failure
     */
    public ControllerResult dequeueVIPMember(String memberId) {
        String error = ValidationUtility.validateRequired(memberId, "Member ID");
        if (error != null) {
            return ControllerResult.fail(error);
        }

        VIPQueueEntry target = findEntryByMemberId(memberId.trim());
        if (target == null) {
            return ControllerResult.fail("Member " + memberId + " is not in the VIP queue");
        }

        // Rebuild heap without the target entry
        ListInterface<VIPQueueEntry> allEntries = new ADT.ArrayList<>();
        while (!vipQueue.isEmpty()) {
            VIPQueueEntry entry = vipQueue.dequeue();
            if (!entry.getMemberId().equals(memberId.trim())) {
                allEntries.add(entry);
            }
        }
        for (int i = 1; i <= allEntries.size(); i++) vipQueue.enqueue(allEntries.getEntry(i));

        saveQueueToFile();
        return ControllerResult.success("Member " + memberId + " removed from VIP queue");
    }

    /**
     * Recalculates the priority of a VIP member after a tier change.
     * The entry is removed and re-enqueued with the updated tier and priority.
     *
     * @param memberId the member ID to recalculate
     * @return ControllerResult indicating success or failure
     */
    public ControllerResult recalculatePriority(String memberId) {
        String error = ValidationUtility.validateRequired(memberId, "Member ID");
        if (error != null) {
            return ControllerResult.fail(error);
        }

        VIPQueueEntry target = findEntryByMemberId(memberId.trim());
        if (target == null) {
            return ControllerResult.fail("Member " + memberId + " is not in the VIP queue");
        }

        Member member = memberController.findByKey(target.getMemberId());
        if (member == null) {
            return ControllerResult.fail("Member not found: " + target.getMemberId());
        }

        String newTier = member.getTier();
        String vipTierError = ValidationUtility.validateVIPAllocationTier(newTier);
        if (vipTierError != null) {
            return ControllerResult.fail(vipTierError);
        }

        // Rebuild heap with updated entry
        ListInterface<VIPQueueEntry> allEntries = new ADT.ArrayList<>();
        while (!vipQueue.isEmpty()) {
            VIPQueueEntry entry = vipQueue.dequeue();
            if (entry.getMemberId().equals(memberId.trim())) {
                // Update the target entry with new tier info
                entry.setMemberTier(newTier);
                entry.setTierPriority(VIPQueueEntry.tierToPriority(newTier));
            }
            allEntries.add(entry);
        }
        for (int i = 1; i <= allEntries.size(); i++) vipQueue.enqueue(allEntries.getEntry(i));

        saveQueueToFile();
        return ControllerResult.success("Priority recalculated for " + memberId
                + " — new tier: " + newTier);
    }

    // ───────────────────── Room Queries ─────────────────────

    /**
     * Returns all rooms that are currently available for allocation.
     *
     * @return list of allocatable rooms
     */
    public ListInterface<Room> viewAvailableRooms() {
        ListInterface<Room> available = new ADT.ArrayList<>();
        ListInterface<Room> allRooms = roomController.getAll();
        for (int i = 1; i <= allRooms.size(); i++) {
            Room room = allRooms.getEntry(i);
            if (isAllocatable(room.getStatus())) {
                available.add(room);
            }
        }
        return available;
    }

    public int getQueueSize() {
        return vipQueue.size();
    }

    public boolean isQueueEmpty() {
        return vipQueue.isEmpty();
    }

    // ───────────────────── Reports ─────────────────────

    /** Builds an unfiltered point-in-time report of VIP demand and room supply. */
    public VIPQueueDemandReport getQueueDemandReport() {
        return getQueueDemandReport(null, null, null, 0, "Priority");
    }

    /**
     * Builds a point-in-time demand report using a linear search/filter pass,
     * followed by merge sort. Blank criteria mean "all".
     */
    public VIPQueueDemandReport getQueueDemandReport(String memberKeyword,
            String memberTier, String preferredRoomType, long minimumWaitingMinutes,
            String sortBy) {
        LocalDateTime generatedAt = LocalDateTime.now();
        ListInterface<VIPQueueEntry> queue = viewQueue();
        ListInterface<VIPQueueDemandReport.QueueRow> rows = new ADT.ArrayList<>();
        HashTableInterface<String, Integer> tierCounts = createTierCountMap();
        HashTableInterface<String, Integer> demandCounts = createRoomTypeCountMap(true);
        HashTableInterface<String, Integer> availableCounts = createRoomTypeCountMap(true);

        long totalWaitingMinutes = 0;
        long longestWaitingMinutes = 0;
        String keyword = normalizeFilter(memberKeyword);
        String tierFilter = normalizeFilter(memberTier);
        String roomFilter = normalizeFilter(preferredRoomType);
        long safeMinimumWaiting = Math.max(0, minimumWaitingMinutes);

        // Linear search: inspect every queue entry while applying all criteria.
        for (int queueIndex = 1; queueIndex <= queue.size(); queueIndex++) {
            VIPQueueEntry entry = queue.getEntry(queueIndex);
            Member member = memberController.findByKey(entry.getMemberId());
            String memberName = member == null ? "Unknown" : member.getName();
            String preferredType = canonicalRoomType(entry.getPreferredRoomType());
            long waitingMinutes = waitingMinutesBetween(entry.getRegistrationTime(), generatedAt);

            if (!containsIgnoreCase(entry.getMemberId(), keyword)
                    && !containsIgnoreCase(memberName, keyword)) continue;
            if (tierFilter != null
                    && !entry.getMemberTier().equalsIgnoreCase(tierFilter)) continue;
            if (roomFilter != null
                    && !preferredType.equalsIgnoreCase(roomFilter)) continue;
            if (waitingMinutes < safeMinimumWaiting) continue;

            rows.add(new VIPQueueDemandReport.QueueRow(
                    0, entry.getMemberId(), memberName, entry.getMemberTier(),
                    entry.getTierPriority(), preferredType, entry.getRegistrationTime(), waitingMinutes));
            incrementCount(tierCounts, entry.getMemberTier());
            incrementCount(demandCounts, preferredType);
            totalWaitingMinutes += waitingMinutes;
            longestWaitingMinutes = Math.max(longestWaitingMinutes, waitingMinutes);
        }

        ItemComparator<VIPQueueDemandReport.QueueRow> queueComparator =
                queueReportComparator(sortBy);
        mergeSort(rows, queueComparator);
        ListInterface<VIPQueueDemandReport.QueueRow> rankedRows = new ADT.ArrayList<>();
        for (int i = 1; i <= rows.size(); i++) {
            VIPQueueDemandReport.QueueRow row = rows.getEntry(i);
            rankedRows.add(new VIPQueueDemandReport.QueueRow(
                    i, row.memberId(), row.memberName(), row.memberTier(), row.priority(),
                    row.preferredRoomType(), row.registrationTime(), row.waitingMinutes()));
        }
        rows = rankedRows;

        int totalAvailable = 0;
        ListInterface<Room> availableRooms = viewAvailableRooms();
        for (int i = 1; i <= availableRooms.size(); i++) {
            Room room = availableRooms.getEntry(i);
            incrementCount(availableCounts, canonicalRoomType(room.getRoomType()));
            totalAvailable++;
        }
        availableCounts.insert("Any", totalAvailable);

        ListInterface<VIPQueueDemandReport.RoomDemandRow> demandRows = new ADT.ArrayList<>();
        for (String roomType : ROOM_TYPES) {
            int demand = getCount(demandCounts, roomType);
            int available = getCount(availableCounts, roomType);
            demandRows.add(new VIPQueueDemandReport.RoomDemandRow(
                    roomType, demand, available, Math.max(0, demand - available)));
        }
        int anyDemand = getCount(demandCounts, "Any");
        demandRows.add(new VIPQueueDemandReport.RoomDemandRow(
                "Any", anyDemand, totalAvailable, Math.max(0, anyDemand - totalAvailable)));

        double averageWaitingMinutes = rows.isEmpty()
                ? 0.0 : totalWaitingMinutes / (double) rows.size();
        return new VIPQueueDemandReport(generatedAt, rows, demandRows, tierCounts,
                averageWaitingMinutes, longestWaitingMinutes);
    }

    /**
     * Builds a historical allocation report. Both date boundaries are inclusive;
     * either may be null to leave that side of the range open.
     */
    public VIPAllocationPerformanceReport getAllocationPerformanceReport(
            LocalDate fromDate, LocalDate toDate) {
        return getAllocationPerformanceReport(fromDate, toDate, null, null,
                null, null, 0, "Latest allocation");
    }

    /**
     * Builds a historical performance report with combined date, keyword, tier,
     * room, preference-match and waiting-time criteria. A linear search/filter
     * pass is followed by merge sort using the management-selected ordering.
     */
    public VIPAllocationPerformanceReport getAllocationPerformanceReport(
            LocalDate fromDate, LocalDate toDate, String allocationKeyword,
            String memberTier, String allocatedRoomType, Boolean preferenceMatched,
            long minimumWaitingMinutes, String sortBy) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Report start date cannot be after the end date.");
        }

        ListInterface<VIPAllocationRecord> filtered = new ADT.ArrayList<>();
        HashTableInterface<String, Integer> tierCounts = createTierCountMap();
        HashTableInterface<String, Integer> roomTypeCounts = createRoomTypeCountMap(false);
        long totalWaitingMinutes = 0;
        long longestWaitingMinutes = 0;
        int preferenceRequestCount = 0;
        int preferenceMatchCount = 0;
        String keyword = normalizeFilter(allocationKeyword);
        String tierFilter = normalizeFilter(memberTier);
        String roomFilter = normalizeFilter(allocatedRoomType);
        long safeMinimumWaiting = Math.max(0, minimumWaitingMinutes);

        // Linear search: scan history once and apply every selected criterion.
        for (int historyIndex = 1; historyIndex <= allocationHistory.size(); historyIndex++) {
            VIPAllocationRecord record = allocationHistory.getEntry(historyIndex);
            LocalDate allocationDate = record.getAllocationTime().toLocalDate();
            if (fromDate != null && allocationDate.isBefore(fromDate)) continue;
            if (toDate != null && allocationDate.isAfter(toDate)) continue;
            if (!containsIgnoreCase(record.getConfirmationNo(), keyword)
                    && !containsIgnoreCase(record.getMemberId(), keyword)) continue;
            if (tierFilter != null
                    && !record.getMemberTier().equalsIgnoreCase(tierFilter)) continue;
            if (roomFilter != null
                    && !record.getAllocatedRoomType().equalsIgnoreCase(roomFilter)) continue;
            if (Boolean.TRUE.equals(preferenceMatched) && !record.isPreferenceMatched()) continue;
            if (Boolean.FALSE.equals(preferenceMatched)
                    && (!record.hasRoomPreference() || record.isPreferenceMatched())) continue;
            if (record.getWaitingMinutes() < safeMinimumWaiting) continue;

            filtered.add(record);
            incrementCount(tierCounts, record.getMemberTier());
            incrementCount(roomTypeCounts, canonicalRoomType(record.getAllocatedRoomType()));
            totalWaitingMinutes += record.getWaitingMinutes();
            longestWaitingMinutes = Math.max(longestWaitingMinutes, record.getWaitingMinutes());
            if (record.hasRoomPreference()) {
                preferenceRequestCount++;
                if (record.isPreferenceMatched()) preferenceMatchCount++;
            }
        }

        mergeSort(filtered, allocationReportComparator(sortBy));
        double averageWaitingMinutes = filtered.isEmpty()
                ? 0.0 : totalWaitingMinutes / (double) filtered.size();
        return new VIPAllocationPerformanceReport(fromDate, toDate, filtered,
                tierCounts, roomTypeCounts, averageWaitingMinutes, longestWaitingMinutes,
                preferenceRequestCount, preferenceMatchCount);
    }

    // ───────────────────── File I/O ─────────────────────

    /**
     * Loads the VIP queue from the persistence file.
     * Called once during construction.
     */
    private void loadQueueFromFile() {
        if (!FileUtility.fileExists(VIP_QUEUE_FILE)) {
            return; // no saved queue — start empty
        }
        String[] lines = FileUtility.readLines(VIP_QUEUE_FILE);
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                try {
                    VIPQueueEntry entry = VIPQueueEntry.fromCsvLine(line);
                    vipQueue.enqueue(entry);
                } catch (Exception e) {
                    System.out.println("Warning: skipping invalid VIP queue line: " + line);
                }
            }
        }
    }

    /** Loads successful VIP allocation audit records, skipping malformed rows. */
    private void loadAllocationHistoryFromFile() {
        if (!FileUtility.fileExists(VIP_ALLOCATION_HISTORY_FILE)) return;
        for (String line : FileUtility.readLines(VIP_ALLOCATION_HISTORY_FILE)) {
            try {
                allocationHistory.add(VIPAllocationRecord.fromCsvLine(line));
            } catch (RuntimeException e) {
                System.out.println("Warning: skipping invalid VIP allocation history line: " + line);
            }
        }
    }

    /**
     * Persists the current VIP queue state to file.
     * Called after every mutation (enqueue, dequeue, cancel, recalculate).
     */
    private void saveQueueToFile() {
        // Drain the heap into a list, then rebuild and write
        ListInterface<VIPQueueEntry> entries = new ADT.ArrayList<>();
        while (!vipQueue.isEmpty()) {
            entries.add(vipQueue.dequeue());
        }

        String[] lines = new String[entries.size()];
        for (int i = 1; i <= entries.size(); i++) {
            lines[i - 1] = entries.getEntry(i).toCsvLine();
        }
        FileUtility.writeAllLines(VIP_QUEUE_FILE, lines);

        // Rebuild the heap from the saved entries
        for (int i = 1; i <= entries.size(); i++) vipQueue.enqueue(entries.getEntry(i));
    }

    // ───────────────────── Private Helpers ─────────────────────

    /**
     * Finds the first room matching the given type that is allocatable.
     * If roomType is null, returns the first allocatable room of any type.
     *
     * @param roomType the preferred room type, or null for any
     * @return the first matching allocatable room, or null if none
     */
    private Room findAvailableRoom(String roomType) {
        ListInterface<Room> allRooms = roomController.getAll();
        for (int i = 1; i <= allRooms.size(); i++) {
            Room room = allRooms.getEntry(i);
            if (!isAllocatable(room.getStatus())) {
                continue;
            }
            if (roomType == null || roomType.trim().isEmpty()) {
                return room;
            }
            if (room.getRoomType().equalsIgnoreCase(roomType.trim())) {
                return room;
            }
        }
        return null;
    }

    /**
     * Checks whether a room status is considered allocatable.
     */
    private boolean isAllocatable(String status) {
        if (status == null) return false;
        for (String s : ALLOCATABLE_STATUSES) {
            if (s.equalsIgnoreCase(status)) return true;
        }
        return false;
    }

    /**
     * Finds a VIPQueueEntry in the heap by member ID.
     * Since a heap doesn't support O(1) lookup, we drain, search, and rebuild.
     *
     * @param memberId the member ID to search for
     * @return the matching entry, or null if not found
     */
    private VIPQueueEntry findEntryByMemberId(String memberId) {
        VIPQueueEntry found = null;
        ListInterface<VIPQueueEntry> temp = new ADT.ArrayList<>();

        while (!vipQueue.isEmpty()) {
            VIPQueueEntry entry = vipQueue.dequeue();
            if (entry.getMemberId().equals(memberId)) {
                found = entry;
            }
            temp.add(entry);
        }

        // Rebuild heap
        for (int i = 1; i <= temp.size(); i++) vipQueue.enqueue(temp.getEntry(i));

        return found;
    }

    /**
     * Generates a unique 8-digit confirmation number for a booking.
     */
    private String generateConfirmationNo() {
        return bookingController.nextNumericConfirmationNo();
    }

    private long waitingMinutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || start.isAfter(end)) return 0;
        return Duration.between(start, end).toMinutes();
    }

    private String normalizeFilter(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    /** Null/blank keyword matches every value; otherwise performs substring search. */
    private boolean containsIgnoreCase(String value, String keyword) {
        return keyword == null || (value != null
                && value.toLowerCase().contains(keyword.toLowerCase()));
    }

    private ItemComparator<VIPQueueDemandReport.QueueRow> queueReportComparator(String sortBy) {
        String option = sortBy == null ? "" : sortBy.trim().toLowerCase();
        return switch (option) {
            case "longest waiting" -> (first, second) -> {
                int result = Long.compare(second.waitingMinutes(), first.waitingMinutes());
                return result != 0 ? result : first.memberId().compareTo(second.memberId());
            };
            case "member name" -> (first, second) -> {
                int result = first.memberName().compareToIgnoreCase(second.memberName());
                return result != 0 ? result : first.memberId().compareTo(second.memberId());
            };
            case "room preference" -> (first, second) -> {
                int result = first.preferredRoomType()
                        .compareToIgnoreCase(second.preferredRoomType());
                return result != 0 ? result : Integer.compare(second.priority(), first.priority());
            };
            default -> (first, second) -> {
                int result = Integer.compare(second.priority(), first.priority());
                return result != 0 ? result
                        : first.registrationTime().compareTo(second.registrationTime());
            };
        };
    }

    private ItemComparator<VIPAllocationRecord> allocationReportComparator(String sortBy) {
        String option = sortBy == null ? "" : sortBy.trim().toLowerCase();
        return switch (option) {
            case "longest waiting" -> (first, second) -> {
                int result = Long.compare(second.getWaitingMinutes(), first.getWaitingMinutes());
                return result != 0 ? result
                        : second.getAllocationTime().compareTo(first.getAllocationTime());
            };
            case "member id" -> (first, second) -> {
                int result = first.getMemberId().compareToIgnoreCase(second.getMemberId());
                return result != 0 ? result
                        : second.getAllocationTime().compareTo(first.getAllocationTime());
            };
            case "tier priority" -> (first, second) -> {
                int result = Integer.compare(
                        VIPQueueEntry.tierToPriority(second.getMemberTier()),
                        VIPQueueEntry.tierToPriority(first.getMemberTier()));
                return result != 0 ? result
                        : second.getAllocationTime().compareTo(first.getAllocationTime());
            };
            default -> (first, second) ->
                    second.getAllocationTime().compareTo(first.getAllocationTime());
        };
    }

    /**
     * Stable O(n log n) merge sort used by both reports. Keeping this algorithm
     * explicit makes the searching + sorting requirement visible and testable.
     */
    private <T> void mergeSort(ListInterface<T> items, ItemComparator<T> comparator) {
        if (items.size() < 2) return;
        ListInterface<T> buffer = new ADT.ArrayList<>(items.size());
        for (int i = 1; i <= items.size(); i++) buffer.add(items.getEntry(i));
        mergeSort(items, buffer, 1, items.size() + 1, comparator);
    }

    private <T> void mergeSort(ListInterface<T> items, ListInterface<T> buffer,
            int start, int end,
            ItemComparator<T> comparator) {
        if (end - start < 2) return;
        int middle = (start + end) / 2;
        mergeSort(items, buffer, start, middle, comparator);
        mergeSort(items, buffer, middle, end, comparator);

        int left = start;
        int right = middle;
        int target = start;
        while (left < middle && right < end) {
            if (comparator.compare(items.getEntry(left), items.getEntry(right)) <= 0) {
                buffer.replace(target++, items.getEntry(left++));
            } else {
                buffer.replace(target++, items.getEntry(right++));
            }
        }
        while (left < middle) buffer.replace(target++, items.getEntry(left++));
        while (right < end) buffer.replace(target++, items.getEntry(right++));
        for (int i = start; i < end; i++) items.replace(i, buffer.getEntry(i));
    }

    private HashTableInterface<String, Integer> createTierCountMap() {
        HashTableInterface<String, Integer> counts = new HashTable<>();
        counts.insert("Platinum", 0);
        counts.insert("Diamond", 0);
        counts.insert("Elite", 0);
        return counts;
    }

    private HashTableInterface<String, Integer> createRoomTypeCountMap(boolean includeAny) {
        HashTableInterface<String, Integer> counts = new HashTable<>();
        for (String roomType : ROOM_TYPES) counts.insert(roomType, 0);
        if (includeAny) counts.insert("Any", 0);
        return counts;
    }

    private String canonicalRoomType(String roomType) {
        if (roomType == null || roomType.trim().isEmpty()) return "Any";
        for (String validType : ROOM_TYPES) {
            if (validType.equalsIgnoreCase(roomType.trim())) return validType;
        }
        return roomType.trim();
    }

    private int getCount(HashTableInterface<String, Integer> counts, String key) {
        Integer count = counts.search(key);
        return count == null ? 0 : count;
    }

    private void incrementCount(HashTableInterface<String, Integer> counts, String key) {
        counts.insert(key, getCount(counts, key) + 1);
    }
}
