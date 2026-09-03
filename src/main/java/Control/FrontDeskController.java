package Control;

import ADT.HashTable;
import ADT.HashTableInterface;
import ADT.ListInterface;
import Entity.Booking;
import Entity.Room;
import Utility.ControllerResult;
import Utility.ValidationUtility;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author Chong Zhi Yi
 */

/**
 * Uses a HashTable as a fast search index for booking confirmation numbers.
 * The index stores references to the same Booking objects managed by
 * BookingController.
 *
 */
public class FrontDeskController {

    private final BookingController bookingController;
    private final RoomController roomController;
    private final PaymentController paymentController;
    private final HashTableInterface<String, Booking> searchIndex;

    public FrontDeskController(BookingController bookingController, RoomController roomController,
                                PaymentController paymentController) {
        this.bookingController = bookingController;
        this.roomController = roomController;
        this.paymentController = paymentController;
        this.searchIndex = new HashTable<>();
        refreshIndex();
    }

    /** Rebuilds the HashTable index from BookingController's current data. */
    public void refreshIndex() {
        ListInterface<Booking> all = bookingController.getAll();
        for (int i = 1; i <= all.size(); i++) {
            Booking booking = all.getEntry(i);
            searchIndex.insert(booking.getConfirmationNo(), booking);
        }
    }

    // Validates the format of a confirmation number. Returns an error
    // message, or null if the input is valid. This is a business rule
    // (exactly 8 digits), not presentation, so it stays here.
    public String validateConfirmationNo(String confNo) {
        ValidationUtility.ValidationAccumulator acc = new ValidationUtility.ValidationAccumulator();
        acc.check(ValidationUtility.validateRequired(confNo, "Confirmation number"));
        if (confNo != null && !confNo.trim().isEmpty() && !confNo.trim().matches("\\d{8}")) {
            acc.check("Confirmation number must be exactly 8 digits");
        }
        return acc.hasErrors() ? acc.getErrorMessage() : null;
    }

    // Validates that a guest-name search term was supplied.
    public String validateGuestName(String name) {
        return ValidationUtility.validateRequired(name, "Guest name");
    }

    // Looks up a single booking by its exact confirmation number.
    // Returns null if not found (does not validate format - use
    // validateConfirmationNo first).
    public Booking findBookingByConfirmationNo(String confNo) {
        if (confNo == null) {
            return null;
        }
        return searchIndex.search(confNo.trim());
    }

    /**
     * Uses linear search because guest-name matching is partial and
     * case-insensitive, unlike exact confirmation-number lookup.
     */
    public Booking[] findBookingsByGuestName(String name) {
        String needle = name.trim().toLowerCase();
        ListInterface<Booking> all = bookingController.getAll();

        int matches = 0;
        for (int i = 1; i <= all.size(); i++) {
            if (all.getEntry(i).getHolderName().toLowerCase().contains(needle)) {
                matches++;
            }
        }

        Booking[] result = new Booking[matches];
        int idx = 0;
        for (int i = 1; i <= all.size(); i++) {
            Booking booking = all.getEntry(i);
            if (booking.getHolderName().toLowerCase().contains(needle)) {
                result[idx++] = booking;
            }
        }
        return result;
    }

    // Returns all bookings sorted by check-in date, room type, or status.
    public Booking[] getAllBookingsSorted(String sortBy) {
        Booking[] bookings = toArray(bookingController.getAll());
        String key = (sortBy == null || sortBy.isBlank()) ? "checkin" : sortBy.trim().toLowerCase();
        insertionSortBookingsBy(bookings, key);
        return bookings;
    }

    /**
     * Checks whether the booking is currently in CheckedIn status.
     */
    public boolean isBookingCheckedIn(String confNo) {

        if (confNo == null) {
            return false;
        }

        Booking booking =
                searchIndex.search(confNo.trim());

        return booking != null
                && "CheckedIn".equalsIgnoreCase(
                        booking.getBookingStatus()
                );
    }

    // Checks whether the booking has already been checked out.
    public boolean isBookingCheckedOut(String confNo) {
        if (confNo == null) {
            return false;
        }
        Booking booking = searchIndex.search(confNo.trim());
        return booking != null && "CheckedOut".equalsIgnoreCase(booking.getBookingStatus());
    }

    // Retrieves the scheduled check-out date for the selected booking.
    public LocalDate getScheduledCheckOutDate(String confNo) {

        if (confNo == null) {
            return null;
        }

        Booking booking =
                searchIndex.search(confNo.trim());

        return booking == null
                ? null
                : booking.getCheckOutDate();
    }

    // Determines whether the check-out is early, on time, or late.
    public String getCheckOutTiming(String confNo) {

        Booking booking =
                searchIndex.search(confNo.trim());

        if (booking == null) {
            return null;
        }

        LocalDate today =
                LocalDate.now();

        LocalDate scheduled =
                booking.getCheckOutDate();

        if (today.isBefore(scheduled)) {
            return "EARLY";
        }

        if (today.isAfter(scheduled)) {
            return "LATE";
        }

        return "ON_TIME";
    }

    // Returns the room number linked to a booking, or null if not found.
    public String getRoomNoForBooking(String confNo) {
        if (confNo == null) {
            return null;
        }

        Booking booking = searchIndex.search(confNo.trim());

        if (booking == null || booking.getRoom() == null) {
            return null;
        }

        return booking.getRoom().getRoomNo();
    }

    // Returns the total number of booking records.
    public int getTotalBookings() {
        return bookingController.getAll().size();
    }

    // Updates the booking status and refreshes the Front Desk search index.
    public ControllerResult updateBookingStatus(String confNo, String status) {
        ControllerResult result =
                bookingController.updateBookingStatus(
                        confNo,
                        status
                );

        if (result.isOk()) {
            refreshIndex();
        }

        return result;
    }

    // Retrieves bookings scheduled to arrive on the current date.
    public Booking[] getTodaysArrivals() {
        LocalDate today = LocalDate.now();
        ListInterface<Booking> all = bookingController.getAll();

        int count = 0;
        for (int i = 1; i <= all.size(); i++) {
            Booking booking = all.getEntry(i);
            if (booking.getCheckInDate().equals(today)
                    && !"Cancelled".equalsIgnoreCase(booking.getBookingStatus())) {
                count++;
            }
        }

        Booking[] result = new Booking[count];
        int idx = 0;
        for (int i = 1; i <= all.size(); i++) {
            Booking booking = all.getEntry(i);
            if (booking.getCheckInDate().equals(today)
                    && !"Cancelled".equalsIgnoreCase(booking.getBookingStatus())) {
                result[idx++] = booking;
            }
        }
        return result;
    }

    // Retrieves bookings scheduled to depart on the current date.
    public Booking[] getTodaysDepartures() {
        LocalDate today = LocalDate.now();
        ListInterface<Booking> all = bookingController.getAll();

        int count = 0;
        for (int i = 1; i <= all.size(); i++) {
            Booking booking = all.getEntry(i);
            if (booking.getCheckOutDate().equals(today)
                    && !"Cancelled".equalsIgnoreCase(booking.getBookingStatus())) {
                count++;
            }
        }

        Booking[] result = new Booking[count];
        int idx = 0;
        for (int i = 1; i <= all.size(); i++) {
            Booking booking = all.getEntry(i);
            if (booking.getCheckOutDate().equals(today)
                    && !"Cancelled".equalsIgnoreCase(booking.getBookingStatus())) {
                result[idx++] = booking;
            }
        }
        return result;
    }

    // ───────────────────── Reports (data only) ─────────────────────

    public static class OperationalStats {
        public final int totalBookings;
        public final int available;
        public final int occupied;
        public final int dirty;
        public final int cleaningInProgress;
        public final int inspected;
        public final int readyForCheckIn;
        public final int maintenance;
        public final int totalPayments;
        public final double totalRevenue;

        public OperationalStats(int totalBookings, int available, int occupied, int dirty,
                                 int cleaningInProgress, int inspected, int readyForCheckIn,
                                 int maintenance, int totalPayments, double totalRevenue) {
            this.totalBookings = totalBookings;
            this.available = available;
            this.occupied = occupied;
            this.dirty = dirty;
            this.cleaningInProgress = cleaningInProgress;
            this.inspected = inspected;
            this.readyForCheckIn = readyForCheckIn;
            this.maintenance = maintenance;
            this.totalPayments = totalPayments;
            this.totalRevenue = totalRevenue;
        }
    }

    // Computes booking/room/payment counts and totals for the operational summary.
    public OperationalStats getOperationalStats() {

        int totalBookings = getTotalBookings();

        ListInterface<Room> allRooms = roomController.getAll();

        int available = 0;
        int occupied = 0;
        int dirty = 0;
        int cleaningInProgress = 0;
        int inspected = 0;
        int readyForCheckIn = 0;
        int maintenance = 0;

        for (int i = 1; i <= allRooms.size(); i++) {

            String status = allRooms.getEntry(i).getStatus();

            if ("Available".equalsIgnoreCase(status)) {
                available++;
            } else if ("Occupied".equalsIgnoreCase(status)) {
                occupied++;
            } else if ("Dirty".equalsIgnoreCase(status)) {
                dirty++;
            } else if ("CleaningInProgress".equalsIgnoreCase(status)) {
                cleaningInProgress++;
            } else if ("Inspected".equalsIgnoreCase(status)) {
                inspected++;
            } else if ("ReadyForCheckIn".equalsIgnoreCase(status)) {
                readyForCheckIn++;
            } else if ("Maintenance".equalsIgnoreCase(status)) {
                maintenance++;
            }
        }

        int totalPayments = paymentController.getTotalPaymentsProcessed();
        double totalRevenue = paymentController.getTotalRevenue();

        return new OperationalStats(totalBookings, available, occupied, dirty, cleaningInProgress,
                inspected, readyForCheckIn, maintenance, totalPayments, totalRevenue);
    }

    /**
     * Returns paid bookings in the given check-out date range, sorted by
     * total bill descending (highest revenue first).
     */
    public Booking[] getRevenueReportBookings(LocalDate start, LocalDate end) {

        Booking[] bookings = toArray(bookingController.getAll());

        if (start != null || end != null) {
            bookings = filterByDateRange(bookings, start, end);
        }

        bookings = filterPaidBookings(bookings);

        insertionSortByTotalDescending(bookings);

        return bookings;
    }

    /**
     * Returns unpaid bookings matching the optional status/date filters,
     * sorted by outstanding amount descending.
     */
    public Booking[] getOutstandingPaymentsBookings(String statusFilter, LocalDate start, LocalDate end) {

        Booking[] bookings = toArray(bookingController.getAll());

        bookings = filterUnpaidBookings(bookings);

        if (statusFilter != null && !statusFilter.isBlank()) {
            bookings = filterByStatus(bookings, statusFilter.trim());
        }

        if (start != null || end != null) {
            bookings = filterByDateRange(bookings, start, end);
        }

        insertionSortByTotalDescending(bookings);

        return bookings;
    }

    // Returns rooms matching the optional status filter, sorted by room number.
    public Room[] getRoomsByStatus(String statusFilter) {

        Room[] rooms = toRoomArray(roomController.getAll());

        if (statusFilter != null && !statusFilter.isBlank()) {

            int count = 0;
            for (Room room : rooms) {
                if (statusFilter.equalsIgnoreCase(room.getStatus())) {
                    count++;
                }
            }

            Room[] filtered = new Room[count];
            int index = 0;
            for (Room room : rooms) {
                if (statusFilter.equalsIgnoreCase(room.getStatus())) {
                    filtered[index++] = room;
                }
            }
            rooms = filtered;
        }

        insertionSortRoomsByRoomNo(rooms);

        return rooms;
    }

    // ───────────────────── Report helpers (search/filter/sort) ─────────────────────

    private Booking[] toArray(ListInterface<Booking> list) {
        Booking[] arr = new Booking[list.size()];
        for (int i = 1; i <= list.size(); i++) {
            arr[i - 1] = list.getEntry(i);
        }
        return arr;
    }

    private Room[] toRoomArray(ListInterface<Room> list) {
        Room[] arr = new Room[list.size()];
        for (int i = 1; i <= list.size(); i++) {
            arr[i - 1] = list.getEntry(i);
        }
        return arr;
    }

    private Booking[] filterByStatus(Booking[] bookings, String status) {
        int count = 0;
        for (Booking b : bookings) {
            if (status.equalsIgnoreCase(b.getBookingStatus())) {
                count++;
            }
        }
        Booking[] result = new Booking[count];
        int idx = 0;
        for (Booking b : bookings) {
            if (status.equalsIgnoreCase(b.getBookingStatus())) {
                result[idx++] = b;
            }
        }
        return result;
    }

    private Booking[] filterByStatusNot(Booking[] bookings, String status) {
        int count = 0;
        for (Booking b : bookings) {
            if (!status.equalsIgnoreCase(b.getBookingStatus())) {
                count++;
            }
        }
        Booking[] result = new Booking[count];
        int idx = 0;
        for (Booking b : bookings) {
            if (!status.equalsIgnoreCase(b.getBookingStatus())) {
                result[idx++] = b;
            }
        }
        return result;
    }

    private Booking[] filterByDateRange(Booking[] bookings, LocalDate start, LocalDate end) {
        int count = 0;
        for (Booking b : bookings) {
            if (inRange(b.getCheckOutDate(), start, end)) {
                count++;
            }
        }
        Booking[] result = new Booking[count];
        int idx = 0;
        for (Booking b : bookings) {
            if (inRange(b.getCheckOutDate(), start, end)) {
                result[idx++] = b;
            }
        }
        return result;
    }

    private boolean inRange(LocalDate date, LocalDate start, LocalDate end) {
        if (start != null && date.isBefore(start)) {
            return false;
        }
        if (end != null && date.isAfter(end)) {
            return false;
        }
        return true;
    }

    private Booking[] filterUnpaidBookings(Booking[] bookings) {
        int count = 0;

        for (Booking booking : bookings) {
            if (!paymentController.isPaid(booking.getConfirmationNo())) {
                count++;
            }
        }

        Booking[] result = new Booking[count];
        int index = 0;

        for (Booking booking : bookings) {
            if (!paymentController.isPaid(booking.getConfirmationNo())) {
                result[index++] = booking;
            }
        }

        return result;
    }

    // Mirror of filterUnpaidBookings: keeps only bookings that are fully paid.
    private Booking[] filterPaidBookings(Booking[] bookings) {
        int count = 0;

        for (Booking booking : bookings) {
            if (paymentController.isPaid(booking.getConfirmationNo())) {
                count++;
            }
        }

        Booking[] result = new Booking[count];
        int index = 0;

        for (Booking booking : bookings) {
            if (paymentController.isPaid(booking.getConfirmationNo())) {
                result[index++] = booking;
            }
        }

        return result;
    }

    // Insertion sort by check-in date, room type, or status.
    private void insertionSortBookingsBy(Booking[] bookings, String sortBy) {
        for (int i = 1; i < bookings.length; i++) {
            Booking key = bookings[i];
            int j = i - 1;
            while (j >= 0 && compareBookingsBy(bookings[j], key, sortBy) > 0) {
                bookings[j + 1] = bookings[j];
                j--;
            }
            bookings[j + 1] = key;
        }
    }

    private int compareBookingsBy(Booking a, Booking b, String sortBy) {
        switch (sortBy) {
            case "roomtype":
                return a.getRoom().getRoomType().compareToIgnoreCase(b.getRoom().getRoomType());
            case "status":
                return a.getBookingStatus().compareToIgnoreCase(b.getBookingStatus());
            case "checkin":
            default:
                return a.getCheckInDate().compareTo(b.getCheckInDate());
        }
    }

    // Insertion sort by total bill in descending order.
    private void insertionSortByTotalDescending(Booking[] bookings) {
        for (int i = 1; i < bookings.length; i++) {
            Booking key = bookings[i];
            double keyTotal = calculateTotal(key);
            int j = i - 1;
            while (j >= 0 && calculateTotal(bookings[j]) < keyTotal) {
                bookings[j + 1] = bookings[j];
                j--;
            }
            bookings[j + 1] = key;
        }
    }

    private void insertionSortRoomsByRoomNo(Room[] rooms) {

        for (int i = 1; i < rooms.length; i++) {

            Room current = rooms[i];
            int j = i - 1;

            while (j >= 0
                    && rooms[j].getRoomNo()
                            .compareToIgnoreCase(
                                    current.getRoomNo()
                            ) > 0) {

                rooms[j + 1] = rooms[j];
                j--;
            }

            rooms[j + 1] = current;
        }
    }

    // Calculates nights consistently for billing and payment.
    // Public so Boundary can display the same figures it doesn't recompute itself.
    public static long nightsBetween(Booking booking) {
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        return nights <= 0 ? 1 : nights; // guard against same-day / bad-date edge cases
    }

    public static double calculateTotal(Booking booking) {
        if (booking == null || booking.getRoom() == null) {
            return 0.0;
        }

        return booking.getRoom().getPrice() * nightsBetween(booking);
    }

}