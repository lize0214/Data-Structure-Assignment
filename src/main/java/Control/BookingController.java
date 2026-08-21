package Control;

import Entity.Guest;
import Entity.Member;
import Entity.Room;
import Entity.Booking;
import Entity.BookingType;
import ADT.ListInterface;
import Utility.ControllerResult;
import Utility.ValidationUtility;

import java.time.LocalDate;
import java.security.SecureRandom;
/**
 * @author Chin Yik Heng
 */
public class BookingController extends AbstractEntityController<Booking, String> {
    private static final SecureRandom CONFIRMATION_RANDOM = new SecureRandom();
    private static final int CONFIRMATION_NUMBER_LIMIT = 100_000_000;

    private final GuestController guestController;
    private final MemberController memberController;
    private final RoomController roomController;

    public BookingController(GuestController guestController, RoomController roomController) {
        this(guestController, new MemberController(), roomController);
    }

    public BookingController(GuestController guestController,
            MemberController memberController, RoomController roomController) {
        super("data/bookings.txt");
        this.guestController = guestController;
        this.memberController = memberController;
        this.roomController = roomController;
        loadFromFile();
    }

    @Override
    protected Booking parseCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 6 && parts.length != 7) {
            throw new IllegalArgumentException("Invalid Booking data format: " + line);
        }
        String holderId = parts[1].trim();
        String roomNo = parts[2].trim();

        Guest guest = guestController.findByKey(holderId);
        Member member = memberController.findByKey(holderId);
        Room room = roomController.findByKey(roomNo);

        return Booking.fromCsvLine(line, guest, member, room);
    }

    @Override
    protected String toCsvLine(Booking item) {
        return item.toCsvLine();
    }

    @Override
    protected String getKey(Booking item) {
        return item.getConfirmationNo();
    }

    /** Adds a booking only when exactly one resolved holder is present. */
    @Override
    public ControllerResult add(Booking item) {
        if (item == null) return ControllerResult.fail("Booking is required.");
        boolean hasGuest = item.getGuest() != null;
        boolean hasMember = item.getMember() != null;
        if (hasGuest == hasMember) {
            return ControllerResult.fail(
                    "Booking must have exactly one holder: either Guest or Member.");
        }
        return super.add(item);
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

    /** Adds a standard reservation to the custom List ADT in check-in-date order. */
    public ControllerResult addStandardBooking(Guest guest, Room room,
            LocalDate checkInDate, LocalDate checkOutDate) {
        String error = validateStandardBooking(guest, room, checkInDate, checkOutDate, null);
        if (error != null) return ControllerResult.fail(error);

        String confirmationNo = nextStandardConfirmationNo();
        Booking booking = new Booking(confirmationNo, guest, room,
                checkInDate, checkOutDate, "Confirmed", BookingType.STANDARD);
        insertChronologically(booking);
        saveToFile();
        return ControllerResult.success("Booking created. Confirmation No: " + confirmationNo);
    }

    public ControllerResult addStandardBooking(String guestId, String roomNo,
            LocalDate checkInDate, LocalDate checkOutDate) {
        return addStandardBooking(guestController.findByKey(guestId),
                roomController.findByKey(roomNo), checkInDate, checkOutDate);
    }

    /** Creates a generated guest record and its standard booking as one operation. */
    public ControllerResult addStandardBookingForNewGuest(String guestId, String name,
            String contact, String roomNo, LocalDate checkInDate, LocalDate checkOutDate) {
        Room room = roomController.findByKey(roomNo);
        Guest guest = new Guest(guestId, name, contact);

        String bookingError = validateStandardBooking(
                guest, room, checkInDate, checkOutDate, null);
        if (bookingError != null) return ControllerResult.fail(bookingError);

        ControllerResult guestResult = guestController.addGeneratedGuest(
                guestId, name, contact);
        if (!guestResult.isOk()) return guestResult;

        ControllerResult bookingResult = addStandardBooking(
                guestController.findByKey(guestId), room, checkInDate, checkOutDate);
        if (!bookingResult.isOk()) {
            guestController.delete(guestId);
            return bookingResult;
        }

        return ControllerResult.success("Guest ID: " + guestId + ". "
                + bookingResult.getMessage());
    }

    public ControllerResult modifyStandardBooking(String confirmationNo, Guest guest, Room room,
            LocalDate checkInDate, LocalDate checkOutDate) {
        if (!isValidStandardConfirmationNo(confirmationNo)) {
            return ControllerResult.fail("Confirmation number must contain exactly 8 digits.");
        }
        Booking existing = findByKey(confirmationNo);
        if (existing == null) return ControllerResult.fail("Booking not found: " + confirmationNo);
        if (existing.getBookingType() != BookingType.STANDARD)
            return ControllerResult.fail("This is not a standard booking.");
        if ("Cancelled".equalsIgnoreCase(existing.getBookingStatus()))
            return ControllerResult.fail("A cancelled booking cannot be modified.");

        String error = validateStandardBooking(guest, room, checkInDate, checkOutDate, confirmationNo);
        if (error != null) return ControllerResult.fail(error);

        list.remove(findIndexByKey(confirmationNo));
        existing.setGuest(guest);
        existing.setRoom(room);
        existing.setCheckInDate(checkInDate);
        existing.setCheckOutDate(checkOutDate);
        insertChronologically(existing);
        saveToFile();
        return ControllerResult.success("Booking " + confirmationNo + " modified.");
    }

    public ControllerResult modifyStandardBooking(String confirmationNo, String guestId,
            String roomNo, LocalDate checkInDate, LocalDate checkOutDate) {
        return modifyStandardBooking(confirmationNo, guestController.findByKey(guestId),
                roomController.findByKey(roomNo), checkInDate, checkOutDate);
    }

    public boolean guestExists(String guestId) {
        return guestController.findByKey(guestId) != null;
    }

    public boolean roomExists(String roomNo) {
        return roomController.findByKey(roomNo) != null;
    }

    /** Returns rooms whose current status is Available. */
    public String[] getAvailableRoomRows() {
        ListInterface<Room> rooms = roomController.getAll();
        int count = 0;
        for (int i = 1; i <= rooms.size(); i++) {
            Room room = rooms.getEntry(i);
            if ("Available".equalsIgnoreCase(room.getStatus())) {
                count++;
            }
        }

        String[] rows = new String[count];
        int outputIndex = 0;
        for (int i = 1; i <= rooms.size(); i++) {
            Room room = rooms.getEntry(i);
            if ("Available".equalsIgnoreCase(room.getStatus())) {
                rows[outputIndex++] = String.format("%-10s %-12s %-18s",
                        room.getRoomNo(), room.getRoomType(), room.getStatus());
            }
        }
        return rows;
    }

    /** Validates dates for a room before the UI attempts to create a booking. */
    public String validateStandardBookingDates(String roomNo, LocalDate checkIn,
            LocalDate checkOut) {
        Room room = roomController.findByKey(roomNo);
        if (room == null) return "Room not found.";
        return validateRoomBookingDates(room, checkIn, checkOut, null);
    }

    public ControllerResult cancelStandardBooking(String confirmationNo) {
        if (!isValidStandardConfirmationNo(confirmationNo)) {
            return ControllerResult.fail("Confirmation number must contain exactly 8 digits.");
        }
        Booking booking = findByKey(confirmationNo);
        if (booking == null) return ControllerResult.fail("Booking not found: " + confirmationNo);
        if (booking.getBookingType() != BookingType.STANDARD)
            return ControllerResult.fail("This is not a standard booking.");
        if ("Cancelled".equalsIgnoreCase(booking.getBookingStatus()))
            return ControllerResult.fail("Booking is already cancelled.");
        booking.setBookingStatus("Cancelled");
        saveToFile();
        return ControllerResult.success("Booking " + confirmationNo + " cancelled.");
    }

    private String validateStandardBooking(Guest guest, Room room, LocalDate checkIn,
            LocalDate checkOut, String excludedConfirmationNo) {
        if (guest == null) return "Guest is required.";
        return validateRoomBookingDates(room, checkIn, checkOut, excludedConfirmationNo);
    }

    private String validateRoomBookingDates(Room room, LocalDate checkIn,
            LocalDate checkOut, String excludedConfirmationNo) {
        if (room == null) return "Room is required.";
        String dateError = ValidationUtility.validateDateRange(checkIn, checkOut);
        if (dateError != null) return dateError;
        if (checkIn.isBefore(LocalDate.now())) return "Check-in date cannot be in the past.";
        if (checkIn.equals(LocalDate.now())
                && !("Available".equalsIgnoreCase(room.getStatus())
                || "ReadyForCheckIn".equalsIgnoreCase(room.getStatus()))) {
            return "Room " + room.getRoomNo() + " is not ready for check-in today (status: "
                    + room.getStatus() + ").";
        }
        if (hasRoomConflict(room.getRoomNo(), checkIn, checkOut, excludedConfirmationNo)) {
            return "Room " + room.getRoomNo() + " is booked during that date range.";
        }
        return null;
    }

    /** Returns whether an active booking overlaps the requested room/date range. */
    public boolean hasRoomConflict(String roomNo, LocalDate checkIn,
            LocalDate checkOut, String excludedConfirmationNo) {
        for (int i = 1; i <= list.size(); i++) {
            Booking booking = list.getEntry(i);
            // Holder resolution is not required to reserve a room/date range.
            // A legacy record with a removed room still cannot participate.
            if (booking.getRoom() == null) continue;
            if (excludedConfirmationNo != null
                    && excludedConfirmationNo.equals(booking.getConfirmationNo())) continue;
            if (!roomNo.equals(booking.getRoom().getRoomNo())) continue;
            if ("Cancelled".equalsIgnoreCase(booking.getBookingStatus())
                    || "CheckedOut".equalsIgnoreCase(booking.getBookingStatus())) continue;
            if (checkIn.isBefore(booking.getCheckOutDate())
                    && checkOut.isAfter(booking.getCheckInDate()))
                return true;
        }
        return false;
    }

    public boolean isValidStandardConfirmationNo(String confirmationNo) {
        return confirmationNo != null && confirmationNo.matches("\\d{8}");
    }

    public String validateReportFilters(String status, LocalDate fromDate, LocalDate toDate) {
        if (status != null && !status.isEmpty()
                && ValidationUtility.validateBookingStatus(status) != null) {
            return ValidationUtility.validateBookingStatus(status);
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            return "Report start date cannot be after the end date.";
        }
        return null;
    }

    private void insertChronologically(Booking booking) {
        int position = 1;
        while (position <= list.size()
                && !booking.getCheckInDate().isBefore(list.getEntry(position).getCheckInDate())) {
            position++;
        }
        list.add(position, booking);
    }

    private String nextStandardConfirmationNo() {
        return nextNumericConfirmationNo();
    }

    public String nextNumericConfirmationNo() {
        String value;
        do {
            int randomNumber = CONFIRMATION_RANDOM.nextInt(CONFIRMATION_NUMBER_LIMIT);
            value = String.format("%08d", randomNumber);
        } while (findByKey(value) != null);
        return value;
    }

    /**
     * Generates the Registration Report: all bookings that originated from a
     * Walk-In Registration (identified by BookingType.WALK_IN), sorted by
     * confirmation number using a manual selection sort (no Java Collections
     * Framework). This is a historical log of processed walk-ins, distinct
     * from the Waiting List Report, which shows guests not yet processed.
     */
    public Booking[] getWalkInRegistrationReport() {
        return getWalkInRegistrationReport("", null, null, "");
    }

    /** Linear search with status, date-range and guest-name filters, then selection sort. */
    public Booking[] getWalkInRegistrationReport(String status, LocalDate fromDate,
            LocalDate toDate, String guestKeyword) {
        return getBookingReport(BookingType.WALK_IN, status, fromDate, toDate, guestKeyword);
    }

    /** Standard bookings filtered by status, date range and guest, then manually sorted. */
    public Booking[] getStandardBookingReport(String status, LocalDate fromDate,
            LocalDate toDate, String guestKeyword) {
        return getBookingReport(BookingType.STANDARD, status, fromDate, toDate, guestKeyword);
    }

    public String[] getWalkInRegistrationReportRows(String status, LocalDate fromDate,
            LocalDate toDate, String guestKeyword) {
        return formatBookingRows(getWalkInRegistrationReport(status, fromDate, toDate, guestKeyword));
    }

    public String[] getStandardBookingReportRows(String status, LocalDate fromDate,
            LocalDate toDate, String guestKeyword) {
        return formatBookingRows(getStandardBookingReport(status, fromDate, toDate, guestKeyword));
    }

    /**
     * Returns all booking categories for the Home Page booking list. Records are
     * found with a linear search/filter pass and ordered with selection sort.
     * Null or blank filter values mean "all".
     */
    public Booking[] getBookingList(String keyword, BookingType bookingType,
            String status, String roomType, LocalDate fromDate, LocalDate toDate,
            String sortBy, boolean ascending) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "Booking list start date cannot be after end date.");
        }

        int matchCount = 0;
        for (int i = 1; i <= list.size(); i++) {
            if (matchesBookingListFilters(list.getEntry(i), keyword, bookingType,
                    status, roomType, fromDate, toDate)) {
                matchCount++;
            }
        }

        Booking[] filtered = new Booking[matchCount];
        int outputIndex = 0;
        for (int i = 1; i <= list.size(); i++) {
            Booking booking = list.getEntry(i);
            if (matchesBookingListFilters(booking, keyword, bookingType,
                    status, roomType, fromDate, toDate)) {
                filtered[outputIndex++] = booking;
            }
        }

        selectionSortBookingList(filtered, sortBy, ascending);
        return filtered;
    }

    private boolean matchesBookingListFilters(Booking booking, String keyword,
            BookingType bookingType, String status, String roomType,
            LocalDate fromDate, LocalDate toDate) {
        if (booking == null) return false;

        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        boolean correctKeyword = normalizedKeyword.isEmpty()
                || containsIgnoreCase(booking.getConfirmationNo(), normalizedKeyword)
                || containsIgnoreCase(booking.getHolderId(), normalizedKeyword)
                || containsIgnoreCase(booking.getHolderName(), normalizedKeyword)
                || containsIgnoreCase(booking.getRoomNo(), normalizedKeyword);
        boolean correctType = bookingType == null || booking.getBookingType() == bookingType;
        boolean correctStatus = status == null || status.isBlank()
                || booking.getBookingStatus().equalsIgnoreCase(status.trim());
        boolean correctRoomType = roomType == null || roomType.isBlank()
                || (booking.getRoom() != null
                && booking.getRoom().getRoomType().equalsIgnoreCase(roomType.trim()));
        boolean afterStart = fromDate == null
                || !booking.getCheckInDate().isBefore(fromDate);
        boolean beforeEnd = toDate == null
                || !booking.getCheckInDate().isAfter(toDate);
        return correctKeyword && correctType && correctStatus && correctRoomType
                && afterStart && beforeEnd;
    }

    private boolean containsIgnoreCase(String value, String normalizedKeyword) {
        return value != null && value.toLowerCase().contains(normalizedKeyword);
    }

    /** Selectable manual selection sort used by the Home Page booking list. */
    private void selectionSortBookingList(Booking[] bookings,
            String sortBy, boolean ascending) {
        for (int i = 0; i < bookings.length - 1; i++) {
            int selectedIndex = i;
            for (int j = i + 1; j < bookings.length; j++) {
                int comparison = compareBookingListFields(
                        bookings[j], bookings[selectedIndex], sortBy);
                if ((!ascending && comparison > 0) || (ascending && comparison < 0)) {
                    selectedIndex = j;
                }
            }
            if (selectedIndex != i) {
                Booking temporary = bookings[i];
                bookings[i] = bookings[selectedIndex];
                bookings[selectedIndex] = temporary;
            }
        }
    }

    private int compareBookingListFields(Booking left, Booking right, String sortBy) {
        String option = sortBy == null ? "" : sortBy.trim().toLowerCase();
        int comparison = switch (option) {
            case "confirmation no" -> compareText(
                    left.getConfirmationNo(), right.getConfirmationNo());
            case "holder name" -> compareText(
                    left.getHolderName(), right.getHolderName());
            case "booking type" -> compareText(
                    left.getBookingType().name(), right.getBookingType().name());
            case "room no" -> compareText(left.getRoomNo(), right.getRoomNo());
            default -> left.getCheckInDate().compareTo(right.getCheckInDate());
        };
        if (comparison != 0) return comparison;
        return compareText(left.getConfirmationNo(), right.getConfirmationNo());
    }

    private int compareText(String left, String right) {
        if (left == null && right == null) return 0;
        if (left == null) return -1;
        if (right == null) return 1;
        return left.compareToIgnoreCase(right);
    }

    private String[] formatBookingRows(Booking[] bookings) {
        String[] rows = new String[bookings.length];
        for (int i = 0; i < bookings.length; i++) {
            Booking booking = bookings[i];
            rows[i] = booking.getConfirmationNo() + " | " + booking.getGuest().getName()
                    + " | Room " + booking.getRoom().getRoomNo() + " | "
                    + booking.getCheckInDate() + " to " + booking.getCheckOutDate()
                    + " | " + booking.getBookingStatus();
        }
        return rows;
    }

    private Booking[] getBookingReport(BookingType bookingType, String status,
            LocalDate fromDate, LocalDate toDate, String guestKeyword) {
        ListInterface<Booking> all = getAll();

        int matchCount = 0;
        for (int i = 1; i <= all.size(); i++) {
            if (matchesReportFilters(all.getEntry(i), bookingType, status,
                    fromDate, toDate, guestKeyword)) {
                matchCount++;
            }
        }

        Booking[] filtered = new Booking[matchCount];
        int index = 0;
        for (int i = 1; i <= all.size(); i++) {
            Booking booking = all.getEntry(i);
            if (matchesReportFilters(booking, bookingType, status,
                    fromDate, toDate, guestKeyword)) {
                filtered[index++] = booking;
            }
        }

        selectionSortByConfirmationNo(filtered);
        return filtered;
    }

    private boolean matchesReportFilters(Booking booking, BookingType bookingType, String status,
            LocalDate fromDate, LocalDate toDate, String guestKeyword) {
        // Only complete, resolvable bookings are suitable for a management report.
        if (booking == null || booking.getGuest() == null || booking.getRoom() == null) {
            return false;
        }
        boolean correctType = booking.getBookingType() == bookingType;
        boolean correctStatus = status == null || status.isEmpty()
                || booking.getBookingStatus().equalsIgnoreCase(status);
        boolean afterStart = fromDate == null || !booking.getCheckInDate().isBefore(fromDate);
        boolean beforeEnd = toDate == null || !booking.getCheckInDate().isAfter(toDate);
        boolean correctGuest = guestKeyword == null || guestKeyword.isEmpty()
                || booking.getGuest().getName().toLowerCase().contains(guestKeyword.toLowerCase())
                || booking.getGuest().getGuestId().equalsIgnoreCase(guestKeyword);
        return correctType && correctStatus && afterStart && beforeEnd && correctGuest;
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
