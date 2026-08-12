// Author: [Your Name]
package Control;

import ADT.QueueInterface;
import ADT.CircularArrayQueue;
import ADT.ListInterface;
import Entity.Guest;
import Entity.Room;
import Entity.Booking;
import Utility.ControllerResult;
import Utility.ValidationUtility;
import Utility.FileUtility;

import java.time.LocalDate;
import java.util.Iterator;

public class WalkInRegistrationController {

    private static final String ROOM_AVAILABLE_STATUS = "Available";
    private static final String ROOM_OCCUPIED_STATUS = "Occupied";
    private static final String BOOKING_STATUS_CONFIRMED = "Confirmed";
    private static final String QUEUE_DATA_FILE = "data/walkins.txt";

    private final QueueInterface<Guest> waitingQueue;
    private final GuestController guestController;
    private final RoomController roomController;
    private final BookingController bookingController;

    private int confirmationCounter;

    public WalkInRegistrationController(
            GuestController guestController,
            RoomController roomController,
            BookingController bookingController) {

        this.waitingQueue = new CircularArrayQueue<>();
        this.guestController = guestController;
        this.roomController = roomController;
        this.bookingController = bookingController;

        this.confirmationCounter =
                bookingController.getAll().size() + 1;

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

        // Add guest into guest records if the guest does not exist
        if (guestController.findByKey(guest.getGuestId()) == null) {

            ControllerResult addResult =
                    guestController.add(guest);

            if (!addResult.isOk()) {
                return addResult;
            }
        }

        waitingQueue.enqueue(guest);
        saveQueueToFile();

        return ControllerResult.success(
                "Guest " + guest.getName()
                + " added to walk-in queue."
        );
    }

    /**
     * Processes the guest at the front of the queue.
     */
    public ControllerResult processNextGuest(
            LocalDate checkInDate,
            LocalDate checkOutDate) {

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

        Guest guest = waitingQueue.dequeue();

        Room availableRoom = findFirstAvailableRoom();

        if (availableRoom == null) {

            // Put the guest back into the queue
            waitingQueue.enqueue(guest);
            saveQueueToFile();

            return ControllerResult.fail(
                    "No room currently available. "
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
                BOOKING_STATUS_CONFIRMED
        );

        ControllerResult bookingResult =
                bookingController.add(booking);

        if (!bookingResult.isOk()) {

            // Put the guest back if booking fails
            waitingQueue.enqueue(guest);
            saveQueueToFile();

            return bookingResult;
        }

        saveQueueToFile();

        ControllerResult roomResult =
                roomController.updateStatus(
                        availableRoom.getRoomNo(),
                        ROOM_OCCUPIED_STATUS
                );

        if (!roomResult.isOk()) {
            return roomResult;
        }

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
                    && guest.getGuestId().equals(guestId)) {

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

        Iterator<Guest> iterator =
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

        ListInterface<Guest> queueSnapshot =
                viewQueue();

        Guest[] guests =
                new Guest[queueSnapshot.size()];

        // Copy guests from ListInterface into array
        for (int i = 1;
                i <= queueSnapshot.size();
                i++) {

            guests[i - 1] =
                    queueSnapshot.getEntry(i);
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

    /**
     * Finds the first available room.
     */
    private Room findFirstAvailableRoom() {

        ListInterface<Room> rooms =
                roomController.getAll();

        for (int i = 1;
                i <= rooms.size();
                i++) {

            Room room = rooms.getEntry(i);

            if (ROOM_AVAILABLE_STATUS.equalsIgnoreCase(
                    room.getStatus())) {

                return room;
            }
        }

        return null;
    }

    /**
     * Generates a walk-in confirmation number.
     */
    private String generateConfirmationNo() {

        String confirmationNo =
                "WI" + String.format(
                        "%06d",
                        confirmationCounter
                );

        confirmationCounter++;

        return confirmationNo;
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

        Iterator<Guest> iterator =
                waitingQueue.getIterator();

        java.util.List<String> lines =
                new java.util.ArrayList<>();

        while (iterator.hasNext()) {

            Guest guest = iterator.next();
            lines.add(guest.getGuestId());
        }

        FileUtility.writeAllLines(
                QUEUE_DATA_FILE,
                lines.toArray(new String[0])
        );
    }
}