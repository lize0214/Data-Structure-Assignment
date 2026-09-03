package Control;

import ADT.ListInterface;
import Entity.Booking;
import Entity.BookingType;
import Entity.Room;
import Entity.VIPAllocationPerformanceReport;
import Entity.VIPQueueDemandReport;
import Entity.VIPQueueEntry;
import Utility.ControllerResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Repeatable test cases for every public VIP allocation operation.
 * The real data files are backed up before each test and restored afterwards.
 */
class VIPAllocationControllerTest {

    private static final Path DATA_DIRECTORY = Path.of("data");
    private static final String[] DATA_FILES = {
        "members.txt", "guests.txt", "rooms.txt", "bookings.txt",
        "vip_queue.txt", "vip_allocation_history.txt"
    };

    private final Map<Path, byte[]> backups = new LinkedHashMap<>();
    private MemberController members;
    private RoomController rooms;
    private BookingController bookings;
    private VIPAllocationController vip;

    @BeforeEach
    void setUp() throws IOException {
        for (String file : DATA_FILES) {
            Path path = DATA_DIRECTORY.resolve(file);
            backups.put(path, Files.exists(path) ? Files.readAllBytes(path) : null);
        }

        writeFixtureData();
        members = new MemberController();
        rooms = new RoomController();
        GuestController guests = new GuestController();
        bookings = new BookingController(guests, members, rooms);
        vip = new VIPAllocationController(members, rooms, bookings);
    }

    @AfterEach
    void restoreData() throws IOException {
        for (Map.Entry<Path, byte[]> entry : backups.entrySet()) {
            if (entry.getValue() == null) {
                Files.deleteIfExists(entry.getKey());
            } else {
                Files.write(entry.getKey(), entry.getValue());
            }
        }
    }

    @Test
    void enqueueRejectsEmptyUnknownNonVipAndInvalidRoomType() {
        assertFailure(vip.enqueueVIPMember("", "Suite"), "Member ID cannot be empty");
        assertFailure(vip.enqueueVIPMember("M999", "Suite"), "Member not found");
        assertFailure(vip.enqueueVIPMember("M002", "Suite"), "not eligible");
        assertFailure(vip.enqueueVIPMember("M001", "Villa"), "Invalid room type");
        assertTrue(vip.isQueueEmpty());
    }

    @Test
    void enqueueAcceptsVipAndPreventsDuplicate() {
        assertSuccess(vip.enqueueVIPMember(" M001 ", "Suite"));
        assertEquals(1, vip.getQueueSize());
        assertEquals("M001", vip.viewQueue().getEntry(1).getMemberId());
        assertFailure(vip.enqueueVIPMember("M001", "Suite"), "already in the VIP queue");
    }

    @Test
    void queueOrdersByTierThenFifoAndSurvivesReload() {
        assertSuccess(vip.enqueueVIPMember("M003", "Deluxe"));
        assertSuccess(vip.enqueueVIPMember("M001", "Suite"));
        assertSuccess(vip.enqueueVIPMember("M008", "Presidential"));

        assertQueueMembers(vip.viewQueue(), "M001", "M008", "M003");

        VIPAllocationController reloaded = new VIPAllocationController(members, rooms, bookings);
        assertQueueMembers(reloaded.viewQueue(), "M001", "M008", "M003");
    }

    @Test
    void dequeueHandlesInvalidMissingAndExistingMembers() {
        assertFailure(vip.dequeueVIPMember(" "), "Member ID cannot be empty");
        assertFailure(vip.dequeueVIPMember("M001"), "is not in the VIP queue");
        assertSuccess(vip.enqueueVIPMember("M001", null));
        assertSuccess(vip.dequeueVIPMember("M001"));
        assertTrue(vip.isQueueEmpty());
    }

    @Test
    void recalculatePriorityUsesMemberLatestTier() {
        assertSuccess(vip.enqueueVIPMember("M003", null));
        assertSuccess(vip.enqueueVIPMember("M008", null));
        assertQueueMembers(vip.viewQueue(), "M008", "M003");

        assertSuccess(members.updateTier("M003", "Platinum"));
        assertSuccess(vip.recalculatePriority("M003"));
        assertQueueMembers(vip.viewQueue(), "M003", "M008");

        assertSuccess(members.updateTier("M003", "Gold"));
        assertFailure(vip.recalculatePriority("M003"), "not eligible");
        assertFailure(vip.recalculatePriority("M999"), "is not in the VIP queue");
    }

    @Test
    void viewAvailableRoomsIncludesAllAllocatableStatusesOnly() {
        ListInterface<Room> available = vip.viewAvailableRooms();
        assertEquals(4, available.size());
        assertEquals("101", available.getEntry(1).getRoomNo());
        assertEquals("104", available.getEntry(4).getRoomNo());
    }

    @Test
    void allocationRejectsAnEmptyQueue() {
        assertFailure(vip.allocateNextVIPRoom(), "No VIP members");
    }

    @Test
    void allocationUsesHighestPriorityAndPreferredRoomAndCreatesVipBooking() {
        assertSuccess(vip.enqueueVIPMember("M003", "Deluxe"));
        assertSuccess(vip.enqueueVIPMember("M001", "Suite"));
        assertSuccess(vip.enqueueVIPMember("M008", "Presidential"));

        ControllerResult result = vip.allocateNextVIPRoom();
        assertSuccess(result);
        assertTrue(result.getMessage().contains("Room 103 (Suite)"));
        assertEquals("Occupied", rooms.findByKey("103").getStatus());
        assertQueueMembers(vip.viewQueue(), "M008", "M003");
        assertEquals(1, bookings.getAll().size());
        Booking booking = bookings.getAll().getEntry(1);
        assertEquals(BookingType.VIP_ALLOCATION, booking.getBookingType());
        assertEquals("M001", booking.getMember().getMemberId());
    }

    @Test
    void allocationFallsBackWhenPreferredRoomTypeIsUnavailable() {
        assertSuccess(rooms.updateStatus("103", "Occupied"));
        assertSuccess(vip.enqueueVIPMember("M001", "Suite"));

        ControllerResult result = vip.allocateNextVIPRoom();
        assertSuccess(result);
        assertTrue(result.getMessage().contains("Room 101 (Single)"));
        assertTrue(vip.isQueueEmpty());
    }

    @Test
    void allocationKeepsVipInQueueWhenNoRoomIsAllocatable() {
        for (int i = 1; i <= rooms.getAll().size(); i++) {
            assertSuccess(rooms.updateStatus(rooms.getAll().getEntry(i).getRoomNo(), "Maintenance"));
        }
        assertSuccess(vip.enqueueVIPMember("M001", null));

        assertFailure(vip.allocateNextVIPRoom(), "No available rooms");
        assertQueueMembers(vip.viewQueue(), "M001");
    }

    @Test
    void queueDemandReportFiltersAndSortsQueueData() {
        assertSuccess(vip.enqueueVIPMember("M003", "Deluxe"));
        assertSuccess(vip.enqueueVIPMember("M001", "Suite"));
        assertSuccess(vip.enqueueVIPMember("M008", "Presidential"));

        VIPQueueDemandReport report = vip.getQueueDemandReport(
                "", "", "", 0, "Priority");
        assertEquals(3, report.getTotalWaiting());
        assertEquals("M001", report.getQueueRows().getEntry(1).memberId());

        VIPQueueDemandReport filtered = vip.getQueueDemandReport(
                "Karina", "Diamond", "Presidential", 0, "Member name");
        assertEquals(1, filtered.getTotalWaiting());
        assertEquals("M008", filtered.getQueueRows().getEntry(1).memberId());
    }

    @Test
    void allocationPerformanceReportFiltersAndSortsAllocationHistory() {
        assertSuccess(vip.enqueueVIPMember("M003", "Deluxe"));
        assertSuccess(vip.enqueueVIPMember("M001", "Suite"));
        assertSuccess(vip.allocateNextVIPRoom()); // M001 gets Suite 103
        assertSuccess(vip.allocateNextVIPRoom()); // M003 gets Deluxe 102

        VIPAllocationPerformanceReport all = vip.getAllocationPerformanceReport(null, null);
        assertEquals(2, all.getTotalAllocations());
        assertEquals(100.0, all.getPreferenceMatchRate());

        VIPAllocationPerformanceReport filtered = vip.getAllocationPerformanceReport(
                null, null, "M003", "Elite", "Deluxe", true, 0, "Member ID");
        assertEquals(1, filtered.getTotalAllocations());
        assertEquals("M003", filtered.getRecords().getEntry(1).getMemberId());
        assertThrows(IllegalArgumentException.class,
                () -> vip.getAllocationPerformanceReport(
                        java.time.LocalDate.now(), java.time.LocalDate.now().minusDays(1)));
    }

    private void writeFixtureData() throws IOException {
        Files.writeString(DATA_DIRECTORY.resolve("members.txt"), """
                M001,Jisoo,Platinum,10000
                M002,Minji,Gold,2000
                M003,Sana,Elite,4000
                M008,Karina,Diamond,6500
                """);
        Files.writeString(DATA_DIRECTORY.resolve("guests.txt"), "G001,Test Guest,0123456789\n");
        Files.writeString(DATA_DIRECTORY.resolve("rooms.txt"), """
                101,Single,120.0,Available
                102,Deluxe,250.0,Available
                103,Suite,480.0,ReadyForCheckIn
                104,Presidential,900.0,Inspected
                105,Deluxe,250.0,Occupied
                """);
        Files.writeString(DATA_DIRECTORY.resolve("bookings.txt"), "");
        Files.writeString(DATA_DIRECTORY.resolve("vip_queue.txt"), "");
        Files.writeString(DATA_DIRECTORY.resolve("vip_allocation_history.txt"), "");
    }

    private void assertQueueMembers(ListInterface<VIPQueueEntry> entries, String... memberIds) {
        assertEquals(memberIds.length, entries.size());
        for (int i = 0; i < memberIds.length; i++) {
            assertEquals(memberIds[i], entries.getEntry(i + 1).getMemberId());
        }
    }

    private void assertSuccess(ControllerResult result) {
        assertTrue(result.isOk(), result.getMessage());
    }

    private void assertFailure(ControllerResult result, String expectedMessage) {
        assertFalse(result.isOk());
        assertTrue(result.getMessage().contains(expectedMessage), result.getMessage());
    }
}
