/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Control;

import ADT.HashTable;
import ADT.HashTableInterface;
import Entity.Booking;
import Entity.Guest;
import Entity.Room;
import Utility.ControllerResult;
import Utility.FileUtility;
import Utility.ValidationUtility;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class FrontDeskController {

    private static final String DEFAULT_GUESTS_FILE = "data/guests.txt";
    private static final String DEFAULT_ROOMS_FILE = "data/rooms.txt";
    private static final String DEFAULT_BOOKINGS_FILE = "data/bookings.txt";

    private final HashTableInterface<String, Booking> hashTable;

    public FrontDeskController() {
        this(DEFAULT_GUESTS_FILE, DEFAULT_ROOMS_FILE, DEFAULT_BOOKINGS_FILE);
    }

    public FrontDeskController(String guestsFile, String roomsFile, String bookingsFile) {
        this.hashTable = new HashTable<>();
        loadData(guestsFile, roomsFile, bookingsFile);
    }

    /**
     * Booking.fromCsvLine() only has access to a guestId and roomNo from its own
     * CSV line - it can't build a full Booking by itself. So this controller reads
     * guests and rooms first, keeps them in lookup maps, then assembles each
     * Booking by resolving those IDs, exactly as described in the comment on
     * Entity.Booking.fromCsvLine().
     */
    private void loadData(String guestsFile, String roomsFile, String bookingsFile) {
        Map<String, Guest> guestsById = new HashMap<>();
        if (FileUtility.fileExists(guestsFile)) {
            for (String line : FileUtility.readLines(guestsFile)) {
                Guest guest = Guest.fromCsvLine(line);
                guestsById.put(guest.getGuestId(), guest);
            }
        }

        Map<String, Room> roomsByNo = new HashMap<>();
        if (FileUtility.fileExists(roomsFile)) {
            for (String line : FileUtility.readLines(roomsFile)) {
                Room room = Room.fromCsvLine(line);
                String statusError = ValidationUtility.validateRoomStatus(room.getStatus());
                if (statusError != null) {
                    System.out.println("Warning (rooms.csv): " + statusError);
                }
                roomsByNo.put(room.getRoomNo(), room);
            }
        }

        if (FileUtility.fileExists(bookingsFile)) {
            for (String line : FileUtility.readLines(bookingsFile)) {
                String[] parts = line.split(",");
                if (parts.length != 6) {
                    System.out.println("Warning (bookings.csv): skipping malformed line: " + line);
                    continue;
                }
                String guestId = parts[1].trim();
                String roomNo = parts[2].trim();

                Guest guest = guestsById.get(guestId);
                Room room = roomsByNo.get(roomNo);
                if (guest == null || room == null) {
                    System.out.println("Warning (bookings.csv): unknown guestId/roomNo in line: " + line);
                    continue;
                }

                Booking booking = Booking.fromCsvLine(line, guest, room);
                String statusError = ValidationUtility.validateBookingStatus(booking.getBookingStatus());
                if (statusError != null) {
                    System.out.println("Warning (bookings.csv): " + statusError);
                }
                hashTable.insert(booking.getConfirmationNo(), booking);
            }
        }
    }

    /**
     * ValidationUtility doesn't ship a confirmation-number check, since that's
     * specific to this controller rather than a generic field validator. Built
     * the same way the rest of the project's validators work: return null on
     * success, an error string on failure - combined via ValidationAccumulator.
     */
    private String validateConfirmationNo(String confNo) {
        ValidationUtility.ValidationAccumulator acc = new ValidationUtility.ValidationAccumulator();
        acc.check(ValidationUtility.validateRequired(confNo, "Confirmation number"));
        if (confNo != null && !confNo.trim().isEmpty() && !confNo.trim().matches("\\d{8}")) {
            acc.check("Confirmation number must be exactly 8 digits");
        }
        return acc.hasErrors() ? acc.getErrorMessage() : null;
    }

    public ControllerResult searchByConfirmationNo(String confNo) {
        String validationError = validateConfirmationNo(confNo);
        if (validationError != null) {
            return ControllerResult.fail(validationError);
        }

        Booking booking = hashTable.search(confNo.trim());
        if (booking == null) {
            return ControllerResult.fail("No booking found for confirmation number: " + confNo);
        }
        return ControllerResult.success(formatBookingDetails(booking));
    }

    public ControllerResult checkRoomAvailability(String confNo) {
        String validationError = validateConfirmationNo(confNo);
        if (validationError != null) {
            return ControllerResult.fail(validationError);
        }

        Booking booking = hashTable.search(confNo.trim());
        if (booking == null) {
            return ControllerResult.fail("No booking found for confirmation number: " + confNo);
        }

        Room room = booking.getRoom();
        String message = String.format("Room %s (%s) status: %s", room.getRoomNo(), room.getRoomType(), room.getStatus());
        return ControllerResult.success(message);
    }

    public ControllerResult getBillingDetails(String confNo) {
        String validationError = validateConfirmationNo(confNo);
        if (validationError != null) {
            return ControllerResult.fail(validationError);
        }

        Booking booking = hashTable.search(confNo.trim());
        if (booking == null) {
            return ControllerResult.fail("No booking found for confirmation number: " + confNo);
        }

        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        if (nights <= 0) {
            nights = 1; // guard against same-day / bad-date edge cases
        }

        double total = booking.getRoom().getPrice() * nights;
        String message = String.format(
                "%s | Room %s (%s) | %d night(s) x RM%.2f = RM%.2f",
                booking.getGuest().getName(),
                booking.getRoom().getRoomNo(),
                booking.getRoom().getRoomType(),
                nights,
                booking.getRoom().getPrice(),
                total
        );
        return ControllerResult.success(message);
    }

    private String formatBookingDetails(Booking b) {
        return String.format(
                "Confirmation #%s | Guest: %s | Room: %s (%s) | %s -> %s | Status: %s",
                b.getConfirmationNo(),
                b.getGuest().getName(),
                b.getRoom().getRoomNo(),
                b.getRoom().getRoomType(),
                b.getCheckInDate(),
                b.getCheckOutDate(),
                b.getBookingStatus()
        );
    }

    public int getTotalBookings() {
        return hashTable.getSize();
    }
}
