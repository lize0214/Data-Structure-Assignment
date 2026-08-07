package Control;

import Entity.Guest;
import Entity.Room;
import Entity.Booking;
import ADT.ListInterface;
import Utility.ControllerResult;
import Utility.ValidationUtility;

import java.time.LocalDate;
//aaa
public class BookingController extends AbstractEntityController<Booking, String> {
    private final GuestController guestController;
    private final RoomController roomController;

    public BookingController(GuestController guestController, RoomController roomController) {
        super("data/bookings.txt");
        this.guestController = guestController;
        this.roomController = roomController;
        loadFromFile();
    }

    @Override
    protected Booking parseCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid Booking data format: " + line);
        }
        String guestId = parts[1].trim();
        String roomNo = parts[2].trim();

        Guest guest = guestController.findByKey(guestId);
        Room room = roomController.findByKey(roomNo);

        return Booking.fromCsvLine(line, guest, room);
    }

    @Override
    protected String toCsvLine(Booking item) {
        return item.toCsvLine();
    }

    @Override
    protected String getKey(Booking item) {
        return item.getConfirmationNo();
    }

    public ControllerResult update(String confirmationNo, Guest guest, Room room,
                                   LocalDate checkInDate, LocalDate checkOutDate, String bookingStatus) {
        ValidationUtility.ValidationAccumulator acc = new ValidationUtility.ValidationAccumulator();
        acc.check(ValidationUtility.validateRequired(confirmationNo, "Confirmation No"));
        acc.check(ValidationUtility.validateNotNull(guest, "Guest"));
        acc.check(ValidationUtility.validateNotNull(room, "Room"));
        acc.check(ValidationUtility.validateDateRange(checkInDate, checkOutDate));
        acc.check(ValidationUtility.validateBookingStatus(bookingStatus));
        if (acc.hasErrors()) return ControllerResult.fail(acc.getErrorMessage());

        Booking booking = findByKey(confirmationNo);
        if (booking == null) return ControllerResult.fail("Booking not found: " + confirmationNo);

        booking.setConfirmationNo(confirmationNo);
        booking.setGuest(guest);
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setBookingStatus(bookingStatus);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updateGuest(String confirmationNo, Guest newGuest) {
        String error = ValidationUtility.validateNotNull(newGuest, "Guest");
        if (error != null) return ControllerResult.fail(error);

        Booking booking = findByKey(confirmationNo);
        if (booking == null) return ControllerResult.fail("Booking not found: " + confirmationNo);

        booking.setGuest(newGuest);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updateRoom(String confirmationNo, Room newRoom) {
        String error = ValidationUtility.validateNotNull(newRoom, "Room");
        if (error != null) return ControllerResult.fail(error);

        Booking booking = findByKey(confirmationNo);
        if (booking == null) return ControllerResult.fail("Booking not found: " + confirmationNo);

        booking.setRoom(newRoom);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updateCheckInDate(String confirmationNo, LocalDate checkInDate) {
        Booking booking = findByKey(confirmationNo);
        if (booking == null) return ControllerResult.fail("Booking not found: " + confirmationNo);

        String error = ValidationUtility.validateDateRange(checkInDate, booking.getCheckOutDate());
        if (error != null) return ControllerResult.fail(error);

        booking.setCheckInDate(checkInDate);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updateCheckOutDate(String confirmationNo, LocalDate checkOutDate) {
        Booking booking = findByKey(confirmationNo);
        if (booking == null) return ControllerResult.fail("Booking not found: " + confirmationNo);

        String error = ValidationUtility.validateDateRange(booking.getCheckInDate(), checkOutDate);
        if (error != null) return ControllerResult.fail(error);

        booking.setCheckOutDate(checkOutDate);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updateBookingStatus(String confirmationNo, String bookingStatus) {
        String error = ValidationUtility.validateBookingStatus(bookingStatus);
        if (error != null) return ControllerResult.fail(error);

        Booking booking = findByKey(confirmationNo);
        if (booking == null) return ControllerResult.fail("Booking not found: " + confirmationNo);

        booking.setBookingStatus(bookingStatus);
        saveToFile();
        return ControllerResult.success();
    }

    /**
     * Generates the Registration Report: all bookings that originated from a
     * Walk-In Registration (confirmation numbers prefixed "WI"), sorted by
     * confirmation number using a manual selection sort (no Java Collections
     * Framework). This is a historical log of processed walk-ins, distinct
     * from the Waiting List Report, which shows guests not yet processed.
     */
    public Booking[] getWalkInRegistrationReport() {
        ListInterface<Booking> all = getAll();
        String walkInPrefix = "WI";

        int matchCount = 0;
        for (int i = 1; i <= all.size(); i++) {
            if (all.getEntry(i).getConfirmationNo().startsWith(walkInPrefix)) {
                matchCount++;
            }
        }

        Booking[] filtered = new Booking[matchCount];
        int index = 0;
        for (int i = 1; i <= all.size(); i++) {
            Booking booking = all.getEntry(i);
            if (booking.getConfirmationNo().startsWith(walkInPrefix)) {
                filtered[index++] = booking;
            }
        }

        selectionSortByConfirmationNo(filtered);
        return filtered;
    }

    private void selectionSortByConfirmationNo(Booking[] bookings) {
        for (int i = 0; i < bookings.length - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < bookings.length; j++) {
                if (bookings[j].getConfirmationNo().compareTo(bookings[minIndex].getConfirmationNo()) < 0) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                Booking temp = bookings[i];
                bookings[i] = bookings[minIndex];
                bookings[minIndex] = temp;
            }
        }
    }
}