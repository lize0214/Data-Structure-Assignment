package Control;

import ADT.HeapPriorityQueue;
import ADT.ListInterface;
import Entity.*;
import Utility.ControllerResult;
import Utility.FileUtility;
import Utility.ValidationUtility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final String VIP_QUEUE_FILE = "data/vip_queue.txt";
    private static final String VIP_ALLOCATION_HISTORY_FILE = "data/vip_allocation_history.txt";
    private static final String[] ROOM_TYPES = {"Single", "Deluxe", "Suite", "Presidential"};

    // Room statuses considered allocatable
    private static final String[] ALLOCATABLE_STATUSES = {"Available", "ReadyForCheckIn", "Inspected"};

    private final HeapPriorityQueue<VIPQueueEntry> vipQueue;
    private final MemberController memberController;
    private final RoomController roomController;
    private final GuestController guestController;
    private final BookingController bookingController;
    private final List<VIPAllocationRecord> allocationHistory;

    /**
     * Constructs the VIP allocation controller with all required dependencies.
     *
     * @param memberController  controller for member lookups
     * @param roomController    controller for room queries and status updates
     * @param guestController   controller for guest lookups
     * @param bookingController controller for creating booking records
     */
    public VIPAllocationController(MemberController memberController,
                                   RoomController roomController,
                                   GuestController guestController,
                                   BookingController bookingController) {
        this.memberController = memberController;
        this.roomController = roomController;
        this.guestController = guestController;
        this.bookingController = bookingController;
        this.vipQueue = new HeapPriorityQueue<>();
        this.allocationHistory = new ArrayList<>();
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
     *   <li>Find a guest matching the member (by name)</li>
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

        // Find a guest matching this member (by name)
        Member member = memberController.findByKey(entry.getMemberId());
        Guest guest = findGuestByMember(member);
        if (guest == null) {
            vipQueue.enqueue(entry);
            return ControllerResult.fail("No guest record found for member " + entry.getMemberId()
                    + " (" + member.getName() + "). Please register the guest first.");
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
        Booking booking = new Booking(confirmationNo, guest, availableRoom,
                today, today.plusDays(1), "CheckedIn", BookingType.STANDARD);
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
                guest.getName(),
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
    public List<VIPQueueEntry> viewQueue() {
        List<VIPQueueEntry> snapshot = new ArrayList<>();
        List<VIPQueueEntry> temp = new ArrayList<>();

        // Dequeue all into temp (comes out highest-priority first)
        while (!vipQueue.isEmpty()) {
            VIPQueueEntry entry = vipQueue.dequeue();
            snapshot.add(entry);
            temp.add(entry);
        }

        // Re-enqueue all entries
        for (VIPQueueEntry entry : temp) {
            vipQueue.enqueue(entry);
        }

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
        List<VIPQueueEntry> allEntries = new ArrayList<>();
        while (!vipQueue.isEmpty()) {
            VIPQueueEntry entry = vipQueue.dequeue();
            if (!entry.getMemberId().equals(memberId.trim())) {
                allEntries.add(entry);
            }
        }
        for (VIPQueueEntry entry : allEntries) {
            vipQueue.enqueue(entry);
        }

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
        List<VIPQueueEntry> allEntries = new ArrayList<>();
        while (!vipQueue.isEmpty()) {
            VIPQueueEntry entry = vipQueue.dequeue();
            if (entry.getMemberId().equals(memberId.trim())) {
                // Update the target entry with new tier info
                entry.setMemberTier(newTier);
                entry.setTierPriority(VIPQueueEntry.tierToPriority(newTier));
            }
            allEntries.add(entry);
        }
        for (VIPQueueEntry entry : allEntries) {
            vipQueue.enqueue(entry);
        }

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
    public List<Room> viewAvailableRooms() {
        List<Room> available = new ArrayList<>();
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

    /** Builds a point-in-time report of VIP demand and allocatable room supply. */
    public VIPQueueDemandReport getQueueDemandReport() {
        LocalDateTime generatedAt = LocalDateTime.now();
        List<VIPQueueEntry> queue = viewQueue();
        List<VIPQueueDemandReport.QueueRow> rows = new ArrayList<>();
        Map<String, Integer> tierCounts = createTierCountMap();
        Map<String, Integer> demandCounts = createRoomTypeCountMap(true);
        Map<String, Integer> availableCounts = createRoomTypeCountMap(true);

        long totalWaitingMinutes = 0;
        long longestWaitingMinutes = 0;
        int rank = 1;
        for (VIPQueueEntry entry : queue) {
            Member member = memberController.findByKey(entry.getMemberId());
            String memberName = member == null ? "Unknown" : member.getName();
            String preferredType = canonicalRoomType(entry.getPreferredRoomType());
            long waitingMinutes = waitingMinutesBetween(entry.getRegistrationTime(), generatedAt);

            rows.add(new VIPQueueDemandReport.QueueRow(
                    rank++, entry.getMemberId(), memberName, entry.getMemberTier(),
                    entry.getTierPriority(), preferredType, entry.getRegistrationTime(), waitingMinutes));
            incrementCount(tierCounts, entry.getMemberTier());
            incrementCount(demandCounts, preferredType);
            totalWaitingMinutes += waitingMinutes;
            longestWaitingMinutes = Math.max(longestWaitingMinutes, waitingMinutes);
        }

        int totalAvailable = 0;
        for (Room room : viewAvailableRooms()) {
            incrementCount(availableCounts, canonicalRoomType(room.getRoomType()));
            totalAvailable++;
        }
        availableCounts.put("Any", totalAvailable);

        List<VIPQueueDemandReport.RoomDemandRow> demandRows = new ArrayList<>();
        for (String roomType : demandCounts.keySet()) {
            int demand = demandCounts.get(roomType);
            int available = availableCounts.getOrDefault(roomType, 0);
            demandRows.add(new VIPQueueDemandReport.RoomDemandRow(
                    roomType, demand, available, Math.max(0, demand - available)));
        }

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
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Report start date cannot be after the end date.");
        }

        List<VIPAllocationRecord> filtered = new ArrayList<>();
        Map<String, Integer> tierCounts = createTierCountMap();
        Map<String, Integer> roomTypeCounts = createRoomTypeCountMap(false);
        long totalWaitingMinutes = 0;
        long longestWaitingMinutes = 0;
        int preferenceRequestCount = 0;
        int preferenceMatchCount = 0;

        for (VIPAllocationRecord record : allocationHistory) {
            LocalDate allocationDate = record.getAllocationTime().toLocalDate();
            if (fromDate != null && allocationDate.isBefore(fromDate)) continue;
            if (toDate != null && allocationDate.isAfter(toDate)) continue;

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

        filtered.sort(Comparator.comparing(VIPAllocationRecord::getAllocationTime).reversed());
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
        List<VIPQueueEntry> entries = new ArrayList<>();
        while (!vipQueue.isEmpty()) {
            entries.add(vipQueue.dequeue());
        }

        String[] lines = new String[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            lines[i] = entries.get(i).toCsvLine();
        }
        FileUtility.writeAllLines(VIP_QUEUE_FILE, lines);

        // Rebuild the heap from the saved entries
        for (VIPQueueEntry entry : entries) {
            vipQueue.enqueue(entry);
        }
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
     * Finds a Guest record that matches the given Member by name.
     * Iterates all guests and returns the first one with a matching name.
     *
     * @param member the member to find a guest for
     * @return the matching guest, or null if none found
     */
    private Guest findGuestByMember(Member member) {
        if (member == null) return null;
        ListInterface<Guest> allGuests = guestController.getAll();
        for (int i = 1; i <= allGuests.size(); i++) {
            Guest guest = allGuests.getEntry(i);
            if (guest.getName().equalsIgnoreCase(member.getName())) {
                return guest;
            }
        }
        return null;
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
        List<VIPQueueEntry> temp = new ArrayList<>();

        while (!vipQueue.isEmpty()) {
            VIPQueueEntry entry = vipQueue.dequeue();
            if (entry.getMemberId().equals(memberId)) {
                found = entry;
            }
            temp.add(entry);
        }

        // Rebuild heap
        for (VIPQueueEntry entry : temp) {
            vipQueue.enqueue(entry);
        }

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

    private Map<String, Integer> createTierCountMap() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("Platinum", 0);
        counts.put("Diamond", 0);
        counts.put("Elite", 0);
        return counts;
    }

    private Map<String, Integer> createRoomTypeCountMap(boolean includeAny) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String roomType : ROOM_TYPES) counts.put(roomType, 0);
        if (includeAny) counts.put("Any", 0);
        return counts;
    }

    private String canonicalRoomType(String roomType) {
        if (roomType == null || roomType.trim().isEmpty()) return "Any";
        for (String validType : ROOM_TYPES) {
            if (validType.equalsIgnoreCase(roomType.trim())) return validType;
        }
        return roomType.trim();
    }

    private void incrementCount(Map<String, Integer> counts, String key) {
        counts.put(key, counts.getOrDefault(key, 0) + 1);
    }
}
