package Control;

import ADT.QueueInterface;
import ADT.QueueIterator;
import ADT.CircularArrayQueue;
import ADT.ListInterface;
import Entity.Guest;
import Entity.Room;
import Entity.Booking;
import Entity.BookingType;
import Utility.ControllerResult;
import Utility.ValidationUtility;
import Utility.FileUtility;

import java.time.LocalDate;
/**
 * @author Chin Yik Heng
 */
public class WalkInRegistrationController {

    private static final String ROOM_AVAILABLE_STATUS = "Available";
    private static final String ROOM_READY_STATUS = "ReadyForCheckIn";
    private static final String ROOM_OCCUPIED_STATUS = "Occupied";
    private static final String BOOKING_STATUS_CONFIRMED = "Confirmed";
    private static final String QUEUE_DATA_FILE = "data/walkins.txt";

    private final QueueInterface<Guest> waitingQueue;
    private final GuestController guestController;
    private final RoomController roomController;
    private final BookingController bookingController;

    public WalkInRegistrationController(
            GuestController guestController,
            RoomController roomController,
            BookingController bookingController) {

        this.waitingQueue = new CircularArrayQueue<>();
        this.guestController = guestController;
        this.roomController = roomController;
        this.bookingController = bookingController;

        loadQueueFromFile();
    }

    /**
     * Registers a walk-in guest and adds the guest
     * to the back of the waiting queue.
     */
    public ControllerResult registerWalkIn(Guest guest) {

        String error =
                ValidationUtility.validateNotNull(guest, "Guest");

        if (error != null) {
            return ControllerResult.fail(error);
        }

        QueueIterator<Guest> duplicateCheck = waitingQueue.getIterator();
        while (duplicateCheck.hasNext()) {
            if (duplicateCheck.next().getGuestId().equalsIgnoreCase(guest.getGuestId())) {
                return ControllerResult.fail("Guest " + guest.getGuestId()
                        + " is already in the walk-in queue.");
            }
        }

        Guest existingGuest = guestController.findByKey(guest.getGuestId());
        if (existingGuest == null) {

            ControllerResult addResult =
                    guestController.add(guest);

            if (!addResult.isOk()) {
                return addResult;
            }
        } else {
            if (!existingGuest.getName().equalsIgnoreCase(guest.getName())
                    || !existingGuest.getContact().equals(guest.getContact())) {
                return ControllerResult.fail("Guest ID " + guest.getGuestId()
                        + " already belongs to another guest record.");
            }
            guest = existingGuest;
        }

        waitingQueue.enqueue(guest);
        saveQueueToFile();

        return ControllerResult.success(
                "Guest " + guest.getName()
                + " added to walk-in queue."
        );
    }

    public ControllerResult registerWalkIn(String guestId, String name, String contact) {
        return registerWalkIn(new Guest(guestId, name, contact));
    }

    /**
     * Processes the guest at the front of the queue.
     */
    public ControllerResult processNextGuest(
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String preferredRoomType,
            String selectedRoomNo) {

        if (waitingQueue.isEmpty()) {
            return ControllerResult.fail(
                    "Walk-in queue is empty. No guest to process."
            );
        }

        String dateError =
                ValidationUtility.validateDateRange(
                        checkInDate,
                        checkOutDate
                );

        if (dateError != null) {
            return ControllerResult.fail(dateError);
        }
        if (!checkInDate.equals(LocalDate.now())) {
            return ControllerResult.fail("A walk-in check-in date must be today.");
        }

        Guest guest = waitingQueue.getFront();

        Room availableRoom = roomController.findByKey(selectedRoomNo);

        if (!isRoomAvailable(selectedRoomNo, preferredRoomType,
                checkInDate, checkOutDate)) {

            return ControllerResult.fail(
                    "The selected " + preferredRoomType + " room is no longer available. "
                    + guest.getName()
                    + " remains in queue."
            );
        }

        String confirmationNo = generateConfirmationNo();

        Booking booking = new Booking(
                confirmationNo,
                guest,
                availableRoom,
                checkInDate,
                checkOutDate,
                BOOKING_STATUS_CONFIRMED,
                BookingType.WALK_IN
        );

        String previousRoomStatus = availableRoom.getStatus();
        ControllerResult roomResult = roomController.updateStatus(
                availableRoom.getRoomNo(), ROOM_OCCUPIED_STATUS);

        if (!roomResult.isOk()) return roomResult;

        ControllerResult bookingResult = bookingController.add(booking);

        if (!bookingResult.isOk()) {
            roomController.updateStatus(availableRoom.getRoomNo(), previousRoomStatus);
            return bookingResult;
        }

        waitingQueue.dequeue();
        saveQueueToFile();

        return ControllerResult.success(
                "Booking " + confirmationNo
                + " created for " + guest.getName()
                + " in Room " + availableRoom.getRoomNo()
                + "."
        );
    }

    /**
     * Cancels a guest's walk-in registration.
     */
    public ControllerResult cancelWalkIn(String guestId) {

        String error =
                ValidationUtility.validateRequired(
                        guestId,
                        "Guest ID"
                );

        if (error != null) {
            return ControllerResult.fail(error);
        }

        if (waitingQueue.isEmpty()) {
            return ControllerResult.fail(
                    "Walk-in queue is empty."
            );
        }

        QueueInterface<Guest> rebuiltQueue =
                new CircularArrayQueue<>();

        boolean found = false;

        while (!waitingQueue.isEmpty()) {

            Guest guest = waitingQueue.dequeue();

            if (!found
                    && guest.getGuestId().equalsIgnoreCase(guestId)) {

                found = true;

            } else {
                rebuiltQueue.enqueue(guest);
            }
        }

        // Move guests back into the original queue
        while (!rebuiltQueue.isEmpty()) {
            waitingQueue.enqueue(
                    rebuiltQueue.dequeue()
            );
        }

        saveQueueToFile();

        if (!found) {
            return ControllerResult.fail(
                    "Guest not found in walk-in queue: "
                    + guestId
            );
        }

        return ControllerResult.success(
                "Walk-in registration cancelled for guest: "
                + guestId
        );
    }

    /**
     * Returns the guest at the front of the queue
     * without removing the guest.
     */
    public Guest peekNextGuest() {

        if (waitingQueue.isEmpty()) {
            return null;
        }

        return waitingQueue.getFront();
    }

    public String getNextGuestName() {
        Guest guest = peekNextGuest();
        return guest == null ? null : guest.getName();
    }

    public String[] getQueueDisplayRows() {
        ListInterface<Guest> snapshot = viewQueue();
        String[] rows = new String[snapshot.size()];
        for (int i = 1; i <= snapshot.size(); i++) {
            Guest guest = snapshot.getEntry(i);
            rows[i - 1] = String.format("%-5d %-25s %-10s %-15s",
                    i, guest.getName(), guest.getGuestId(), guest.getContact());
        }
        return rows;
    }

    /** Lists rooms matching the guest's preference and requested dates. */
    public String[] getAvailableRoomRows(String preferredRoomType,
            LocalDate checkInDate, LocalDate checkOutDate) {
        ListInterface<Room> rooms = roomController.getAll();
        int count = 0;
        for (int i = 1; i <= rooms.size(); i++) {
            Room room = rooms.getEntry(i);
            if (isRoomAvailable(room.getRoomNo(), preferredRoomType,
                    checkInDate, checkOutDate)) count++;
        }

        String[] rows = new String[count];
        int outputIndex = 0;
        for (int i = 1; i <= rooms.size(); i++) {
            Room room = rooms.getEntry(i);
            if (isRoomAvailable(room.getRoomNo(), preferredRoomType,
                    checkInDate, checkOutDate)) {
                rows[outputIndex++] = String.format("%-10s %-12s RM%-9.2f %-18s",
                        room.getRoomNo(), room.getRoomType(), room.getPrice(), room.getStatus());
            }
        }
        return rows;
    }

    /** Checks that a selected room still matches all walk-in requirements. */
    public boolean isRoomAvailable(String roomNo, String preferredRoomType,
            LocalDate checkInDate, LocalDate checkOutDate) {
        Room room = roomController.findByKey(roomNo);
        if (room == null || preferredRoomType == null
                || !preferredRoomType.equalsIgnoreCase(room.getRoomType())) return false;
        boolean ready = ROOM_AVAILABLE_STATUS.equalsIgnoreCase(room.getStatus())
                || ROOM_READY_STATUS.equalsIgnoreCase(room.getStatus());
        return ready && !bookingController.hasRoomConflict(room.getRoomNo(),
                checkInDate, checkOutDate, null);
    }

    /**
     * Checks whether the waiting queue is empty.
     */
    public boolean isQueueEmpty() {
        return waitingQueue.isEmpty();
    }

    /**
     * Returns a copy of the waiting queue.
     */
    public ListInterface<Guest> viewQueue() {

        ListInterface<Guest> snapshot =
                new ADT.ArrayList<>();

        QueueIterator<Guest> iterator =
                waitingQueue.getIterator();

        while (iterator.hasNext()) {
            snapshot.add(iterator.next());
        }

        return snapshot;
    }

    /**
     * Returns all waiting guests as an array,
     * sorted alphabetically by guest name.
     */
    public Guest[] getWaitingListReport() {
        return getWaitingListReport("", "");
    }

    /** Linear search by name/ID and contact, followed by selection sort by name. */
    public Guest[] getWaitingListReport(String guestKeyword, String contactKeyword) {

        ListInterface<Guest> queueSnapshot =
                viewQueue();

        int matchCount = 0;
        for (int i = 1; i <= queueSnapshot.size(); i++) {
            if (matchesWaitingFilters(queueSnapshot.getEntry(i), guestKeyword, contactKeyword)) {
                matchCount++;
            }
        }

        Guest[] guests = new Guest[matchCount];

        int resultIndex = 0;
        for (int i = 1; i <= queueSnapshot.size(); i++) {
            Guest guest = queueSnapshot.getEntry(i);
            if (matchesWaitingFilters(guest, guestKeyword, contactKeyword)) {
                guests[resultIndex++] = guest;
            }
        }

        // Selection sort by guest name
        for (int i = 0;
                i < guests.length - 1;
                i++) {

            int minIndex = i;

            for (int j = i + 1;
                    j < guests.length;
                    j++) {

                String currentName =
                        guests[j].getName();

                String minimumName =
                        guests[minIndex].getName();

                if (currentName.compareToIgnoreCase(
                        minimumName) < 0) {

                    minIndex = j;
                }
            }

            if (minIndex != i) {

                Guest temp = guests[i];
                guests[i] = guests[minIndex];
                guests[minIndex] = temp;
            }
        }

        return guests;
    }

    public String[] getWaitingListReportRows(String guestKeyword, String contactKeyword) {
        Guest[] guests = getWaitingListReport(guestKeyword, contactKeyword);
        String[] rows = new String[guests.length];
        for (int i = 0; i < guests.length; i++) {
            rows[i] = (i + 1) + ". " + guests[i].getName() + " (ID: "
                    + guests[i].getGuestId() + ", Contact: " + guests[i].getContact() + ")";
        }
        return rows;
    }

    private boolean matchesWaitingFilters(Guest guest, String guestKeyword,
            String contactKeyword) {
        boolean correctGuest = guestKeyword == null || guestKeyword.isEmpty()
                || guest.getName().toLowerCase().contains(guestKeyword.toLowerCase())
                || guest.getGuestId().equalsIgnoreCase(guestKeyword);
        boolean correctContact = contactKeyword == null || contactKeyword.isEmpty()
                || guest.getContact().contains(contactKeyword);
        return correctGuest && correctContact;
    }

    /**
     * Generates a walk-in confirmation number.
     */
    private String generateConfirmationNo() {

        return bookingController.nextNumericConfirmationNo();
    }

    /**
     * Loads the waiting queue from the file.
     */
    private void loadQueueFromFile() {

        String[] lines =
                FileUtility.readLines(
                        QUEUE_DATA_FILE
                );

        for (String line : lines) {

            String guestId = line.trim();

            if (guestId.isEmpty()) {
                continue;
            }

            Guest guest =
                    guestController.findByKey(
                            guestId
                    );

            if (guest != null) {
                waitingQueue.enqueue(guest);
            }
        }
    }

    /**
     * Saves the waiting queue into the file.
     */
    private void saveQueueToFile() {

        QueueIterator<Guest> iterator =
                waitingQueue.getIterator();

        ListInterface<String> lines = new ADT.ArrayList<>();

        while (iterator.hasNext()) {

            Guest guest = iterator.next();
            lines.add(guest.getGuestId());
        }

        String[] output = new String[lines.size()];
        for (int i = 1; i <= lines.size(); i++) output[i - 1] = lines.getEntry(i);
        FileUtility.writeAllLines(QUEUE_DATA_FILE, output);
    }
}
