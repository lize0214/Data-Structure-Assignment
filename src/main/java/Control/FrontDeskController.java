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
 * Front Desk keeps its OWN HashTable<String, Booking> as a fast search index
 * for confirmation-number lookups, built once from BookingController's shared
 * data.
 * Front Desk's job is specifically fast repeated lookup by confirmation
 * number, so it builds a HashTable index on top instead of duplicating data:
 * the index holds references to the SAME Booking objects BookingController
 * holds, so a status change made elsewhere (e.g. by PaymentController) is
 * visible through this index immediately, with no re-sync needed.
 *
 * If another module adds/removes bookings during the same run (e.g. Walk-in
 * Registration), call refreshIndex() before relying on this index again.
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

        Booking booking = searchIndex.search(confNo.trim());
        if (booking == null) {
            return ControllerResult.fail("No booking found for confirmation number: " + confNo);
        }
        return ControllerResult.success(formatBookingDetails(booking));
    }

    public ControllerResult checkAssignedRoomStatus(String confNo) {
        String validationError = validateConfirmationNo(confNo);
        if (validationError != null) {
            return ControllerResult.fail(validationError);
        }

        Booking booking = searchIndex.search(confNo.trim());
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

        Booking booking = searchIndex.search(confNo.trim());
        if (booking == null) {
            return ControllerResult.fail("No booking found for confirmation number: " + confNo);
        }

        long nights = nightsBetween(booking);
        double total = calculateTotal(booking);

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

    /**
     * Whether this booking has already been paid and checked out. Used by
     * FrontDeskUI's Check-Out flow to decide whether payment needs to be
     * collected before finishing the check-out.
     */
    public boolean isBookingCheckedOut(String confNo) {
        if (confNo == null) {
            return false;
        }
        Booking booking = searchIndex.search(confNo.trim());
        return booking != null && "CheckedOut".equalsIgnoreCase(booking.getBookingStatus());
    }

    /** Returns the room number tied to a booking, or null if not found. Used
     *  by FrontDeskUI's Check-Out flow to flip the room back to Available
     *  once payment succeeds. */
    public String getRoomNoForBooking(String confNo) {
        if (confNo == null) {
            return null;
        }
        Booking booking = searchIndex.search(confNo.trim());
        return booking == null ? null : booking.getRoom().getRoomNo();
    }

    /**
     * Deliberately does NOT use the HashTable index - this is a plain
     * linear scan over every booking, kept alongside searchByConfirmationNo()
     * as a contrast: O(1) average-case hash lookup by exact confirmation
     * number vs O(n) linear scan for a partial, case-insensitive name match
     * (name search can't hash to a single bucket the way an exact key can).
     */
    public ControllerResult searchByGuestName(String name) {
        String error = ValidationUtility.validateRequired(name, "Guest name");
        if (error != null) {
            return ControllerResult.fail(error);
        }

        String needle = name.trim().toLowerCase();
        ListInterface<Booking> all = bookingController.getAll();

        StringBuilder sb = new StringBuilder();
        int matches = 0;
        for (int i = 1; i <= all.size(); i++) {
            Booking b = all.getEntry(i);
            if (b.getGuest().getName().toLowerCase().contains(needle)) {
                if (matches > 0) {
                    sb.append("\n");
                }
                sb.append(formatBookingDetails(b));
                matches++;
            }
        }

        if (matches == 0) {
            return ControllerResult.fail("No booking found for guest name: " + name);
        }
        return ControllerResult.success(sb.toString());
    }

    /**
     * Lists every booking, sorted by the requested field. sortBy accepts
     * "checkin" (default), "roomtype", or "status" - anything else falls
     * back to check-in date.
     */
    public ControllerResult viewAllBookings(String sortBy) {
        Booking[] bookings = toArray(bookingController.getAll());
        if (bookings.length == 0) {
            return ControllerResult.success("No bookings found.");
        }

        String key = (sortBy == null || sortBy.isBlank()) ? "checkin" : sortBy.trim().toLowerCase();
        insertionSortBookingsBy(bookings, key);

        StringBuilder sb = new StringBuilder();

        String line =
                "----------------------------------------------------------------------------------------------";

        sb.append(line).append("\n");

        sb.append(String.format(
                "%-10s %-20s %-8s %-12s %-12s %s%n",
                "Conf#",
                "Guest",
                "Room",
                "Type",
                "Status",
                "Check-In -> Check-Out"
        ));

        sb.append(line).append("\n");

        for (Booking b : bookings) {

            sb.append(String.format(
                    "%-10s %-20s %-8s %-12s %-12s %s -> %s%n",
                    b.getConfirmationNo(),
                    b.getGuest().getName(),
                    b.getRoom().getRoomNo(),
                    b.getRoom().getRoomType(),
                    b.getBookingStatus(),
                    b.getCheckInDate(),
                    b.getCheckOutDate()
            ));
        }

        sb.append(line).append("\n");

        sb.append(
                "Total Bookings: "
        ).append(bookings.length).append("\n");

        sb.append(line);

        return ControllerResult.success(
                sb.toString()
        );
    }

    public ControllerResult quickStats() {

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

        int totalPayments =
                paymentController
                        .getTotalPaymentsProcessed();

        double totalRevenue =
                paymentController
                        .getTotalRevenue();

        StringBuilder sb =
                new StringBuilder();

        sb.append(
                ("\n"+ "------------------------------------------------------------------------------\n")
        );

        sb.append(
                "                           OPERATIONAL SUMMARY\n"
        );

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(
                "BOOKING SUMMARY\n"
        );

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(
                String.format(
                        "%-25s : %d%n",
                        "Total Bookings",
                        totalBookings
                )
        );

        sb.append(
                "\nROOM SUMMARY\n"
        );

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(String.format(
        "%-25s : %d%n", "Available", available));

        sb.append(String.format(
                "%-25s : %d%n", "Occupied", occupied));

        sb.append(String.format(
                "%-25s : %d%n", "Dirty", dirty));

        sb.append(String.format(
                "%-25s : %d%n", "Cleaning In Progress", cleaningInProgress));

        sb.append(String.format(
                "%-25s : %d%n", "Inspected", inspected));

        sb.append(String.format(
                "%-25s : %d%n", "Ready For Check-In", readyForCheckIn));

        sb.append(String.format(
                "%-25s : %d%n", "Maintenance", maintenance));

        sb.append(
                "\nPAYMENT SUMMARY\n"
        );

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(
                String.format(
                        "%-25s : %d%n",
                        "Payments Processed",
                        totalPayments
                )
        );

        sb.append(
                String.format(
                        "%-25s : RM %.2f%n",
                        "Total Revenue Collected",
                        totalRevenue
                )
        );

        sb.append(
                "------------------------------------------------------------------------------"
        );

        return ControllerResult.success(
                sb.toString()
        );
    }

    /**
     * Sorted (by total bill, descending) and optionally filtered (by booking
     * status and/or check-out date range) revenue report.
     * Pass null / blank for statusFilter, and null for start/end to skip those filters.
     */
    public ControllerResult revenueReport(
            String statusFilter,
            LocalDate start,
            LocalDate end) {

        Booking[] bookings =
                toArray(bookingController.getAll());

        if (statusFilter != null
                && !statusFilter.isBlank()) {

            bookings = filterByStatus(
                    bookings,
                    statusFilter.trim()
            );
        }

        if (start != null || end != null) {
            bookings = filterByDateRange(
                    bookings,
                    start,
                    end
            );
        }

        insertionSortByTotalDescending(bookings);

        if (bookings.length == 0) {
            return ControllerResult.success(
                    "No bookings match the given filters."
            );
        }

        double grandTotal = 0;
        int paidCount = 0;

        StringBuilder sb = new StringBuilder();

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(
                String.format(
                        "%-10s %-20s %-8s %-12s %s%n",
                        "Conf#",
                        "Guest",
                        "Nights",
                        "Status",
                        "Total (RM)"
                )
        );

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        for (Booking b : bookings) {

            // Revenue only includes successful payments
            if (!paymentController.isPaid(
                    b.getConfirmationNo())) {
                continue;
            }

            double total = calculateTotal(b);

            grandTotal += total;
            paidCount++;

            sb.append(
                    String.format(
                            "%-10s %-20s %-8d %-12s %.2f%n",
                            b.getConfirmationNo(),
                            b.getGuest().getName(),
                            nightsBetween(b),
                            b.getBookingStatus(),
                            total
                    )
            );
        }

        if (paidCount == 0) {
            return ControllerResult.success(
                    "No paid bookings match the given filters."
            );
        }

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(
                String.format(
                        "Total Revenue Collected: RM%.2f (%d paid booking(s))%n",
                        grandTotal,
                        paidCount
                )
        );

        sb.append(
                "------------------------------------------------------------------------------"
        );

        return ControllerResult.success(
                sb.toString()
        );
    }

    /**
     * Bookings not yet checked out (i.e. not yet paid), sorted by amount
     * due, descending, so the highest outstanding balances surface first.
     */
    public ControllerResult outstandingPaymentsReport() {

        Booking[] bookings =
                toArray(bookingController.getAll());

        bookings =
                filterUnpaidBookings(bookings);

        insertionSortByTotalDescending(bookings);

        if (bookings.length == 0) {
            return ControllerResult.success(
                    "No outstanding payments - all bookings are settled."
            );
        }

        double totalOutstanding = 0;

        StringBuilder sb = new StringBuilder();

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(
                String.format(
                        "%-10s %-20s %-12s %s%n",
                        "Conf#",
                        "Guest",
                        "Status",
                        "Amount Due (RM)"
                )
        );

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        for (Booking b : bookings) {

            double due = calculateTotal(b);

            totalOutstanding += due;

            sb.append(
                    String.format(
                            "%-10s %-20s %-12s %.2f%n",
                            b.getConfirmationNo(),
                            b.getGuest().getName(),
                            b.getBookingStatus(),
                            due
                    )
            );
        }

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(
                String.format(
                        "Total Outstanding: RM%.2f (%d booking(s))%n",
                        totalOutstanding,
                        bookings.length
                )
        );

        sb.append(
                "------------------------------------------------------------------------------"
        );

        return ControllerResult.success(
                sb.toString()
        );
    }

    // ───────────────────── Report helpers (search/filter/sort) ─────────────────────

    private Booking[] toArray(ListInterface<Booking> list) {
        Booking[] arr = new Booking[list.size()];
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

    /** Manual insertion sort, ascending, keyed by "checkin", "roomtype", or "status". */
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

    /** Manual insertion sort, descending by calculateTotal(booking). */
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

    /** Shared with PaymentController so both charge/display the exact same figure. */
    static long nightsBetween(Booking booking) {
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        return nights <= 0 ? 1 : nights; // guard against same-day / bad-date edge cases
    }

    static double calculateTotal(Booking booking) {
        return booking.getRoom().getPrice() * nightsBetween(booking);
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
        return bookingController.getAll().size();
    }
    
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
    
    private Booking[] filterUnpaidBookings(Booking[] bookings) {
        int count = 0;

        // Count unpaid bookings first
        for (Booking booking : bookings) {
            if (!paymentController.isPaid(
                    booking.getConfirmationNo()
            )) {
                count++;
            }
        }

        Booking[] result = new Booking[count];
        int index = 0;

        // Add unpaid bookings into result array
        for (Booking booking : bookings) {
            if (!paymentController.isPaid(
                    booking.getConfirmationNo()
            )) {
                result[index++] = booking;
            }
        }

        return result;
    }
    
    public ControllerResult getTodaysArrivals() {

        LocalDate today = LocalDate.now();
        ListInterface<Booking> all = bookingController.getAll();

        StringBuilder sb = new StringBuilder();

        sb.append(String.format(
                "%-10s %-20s %-8s %-12s%n",
                "Conf#", "Guest", "Room", "Status"
        ));

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        int count = 0;

        for (int i = 1; i <= all.size(); i++) {

            Booking booking = all.getEntry(i);

            if (booking.getCheckInDate().equals(today)
                    && !"Cancelled".equalsIgnoreCase(
                            booking.getBookingStatus()
                    )) {

                sb.append(String.format(
                        "%-10s %-20s %-8s %-12s%n",
                        booking.getConfirmationNo(),
                        booking.getGuest().getName(),
                        booking.getRoom().getRoomNo(),
                        booking.getBookingStatus()
                ));

                count++;
            }
        }

        if (count == 0) {
            return ControllerResult.success(
                    "No arrivals scheduled for today."
            );
        }

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(
                "Total Arrivals: "
        ).append(count);

        return ControllerResult.success(
                sb.toString()
        );
    }

    public ControllerResult getTodaysDepartures() {

        LocalDate today = LocalDate.now();
        ListInterface<Booking> all = bookingController.getAll();

        StringBuilder sb = new StringBuilder();

        sb.append(String.format(
                "%-10s %-20s %-8s %-12s%n",
                "Conf#", "Guest", "Room", "Status"
        ));

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        int count = 0;

        for (int i = 1; i <= all.size(); i++) {

            Booking booking = all.getEntry(i);

            if (booking.getCheckOutDate().equals(today)
                    && !"Cancelled".equalsIgnoreCase(
                            booking.getBookingStatus()
                    )) {

                sb.append(String.format(
                        "%-10s %-20s %-8s %-12s%n",
                        booking.getConfirmationNo(),
                        booking.getGuest().getName(),
                        booking.getRoom().getRoomNo(),
                        booking.getBookingStatus()
                ));

                count++;
            }
        }

        if (count == 0) {
            return ControllerResult.success(
                    "No departures scheduled for today."
            );
        }

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(
                "Total Departures: "
        ).append(count);

        return ControllerResult.success(
                sb.toString()
        );
    }
    
    public ControllerResult roomStatusReport(
        String statusFilter){

        ListInterface<Room> allRooms =
                roomController.getAll();

        if (allRooms.isEmpty()) {
            return ControllerResult.success(
                    "No room records found."
            );
        }

        // Convert custom ListInterface to array
        Room[] rooms = new Room[allRooms.size()];

        for (int i = 1; i <= allRooms.size(); i++) {
            rooms[i - 1] = allRooms.getEntry(i);
        }

        // Filter by room status
        if (statusFilter != null
                && !statusFilter.isBlank()) {

            int count = 0;

            for (Room room : rooms) {
                if (statusFilter.equalsIgnoreCase(
                        room.getStatus())) {
                    count++;
                }
            }

            Room[] filtered =
                    new Room[count];

            int index = 0;

            for (Room room : rooms) {
                if (statusFilter.equalsIgnoreCase(
                        room.getStatus())) {

                    filtered[index++] = room;
                }
            }

            rooms = filtered;
        }

        if (rooms.length == 0) {
            return ControllerResult.success(
                    "No rooms match the given status filter."
            );
        }

        // Explicit insertion sort
        insertionSortRoomsByRoomNo(rooms);

        StringBuilder sb =
                new StringBuilder();

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(
                String.format(
                        "%-12s %-20s %-15s %s%n",
                        "Room No.",
                        "Room Type",
                        "Rate (RM)",
                        "Status"
                )
        );

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        for (Room room : rooms) {

            sb.append(
                    String.format(
                            "%-12s %-20s %-15.2f %s%n",
                            room.getRoomNo(),
                            room.getRoomType(),
                            room.getPrice(),
                            room.getStatus()
                    )
            );
        }

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(
                String.format(
                        "Total Rooms: %d%n",
                        rooms.length
                )
        );

        sb.append(
                "------------------------------------------------------------------------------"
        );

        return ControllerResult.success(
                sb.toString()
        );
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

}