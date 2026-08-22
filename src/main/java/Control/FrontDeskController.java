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

    // Searches for a booking using the entered 8-digit confirmation number.
    public ControllerResult searchByConfirmationNo(String confNo) {

        String validationError = validateConfirmationNo(confNo);

        if (validationError != null) {
            return ControllerResult.fail(validationError);
        }

        Booking booking =
                searchIndex.search(confNo.trim());

        if (booking == null) {
            return ControllerResult.fail("No booking found for confirmation number: " + confNo);
        }

        StringBuilder sb = new StringBuilder();

        appendBookingTableHeader(sb);
        appendBookingRow(sb, booking);
        appendBookingTableFooter(sb);

        return ControllerResult.success(
                sb.toString()
        );
    }

    // Retrieves the assigned room details and current room status for a booking.
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

        String line =
                "------------------------------------------------------------------------------";

        StringBuilder sb = new StringBuilder();

        sb.append("\n").append(line).append("\n");

        sb.append(String.format(
                "%-10s %-20s %-8s %-16s %s%n",
                "Conf#",
                "Guest",
                "Room",
                "Type",
                "Status"
        ));

        sb.append(line).append("\n");

        sb.append(String.format(
                "%-10s %-20s %-8s %-16s %s%n",
                booking.getConfirmationNo(),
                booking.getGuest().getName(),
                room.getRoomNo(),
                room.getRoomType(),
                room.getStatus()
        ));

        sb.append(line);

        return ControllerResult.success(sb.toString());
    }

    // Calculates and displays the billing details for a selected booking.
    public ControllerResult getBillingDetails(String confNo) {
        String validationError = validateConfirmationNo(confNo);
        if (validationError != null) {
            return ControllerResult.fail(validationError);
        }

        Booking booking = searchIndex.search(confNo.trim());

        if (booking == null) {
            return ControllerResult.fail("No booking found for confirmation number: " + confNo);
        }

        // Ensure the booking's room information is available
        if (booking.getRoom() == null) {
            return ControllerResult.fail(
                "Room information for this booking could not be found."
            );
        }

        long nights = nightsBetween(booking);
        double total = calculateTotal(booking);

        String line = "------------------------------------------------------------------------------";

        StringBuilder sb = new StringBuilder();

        sb.append("\n")
                .append(line)
                .append("\n");

        sb.append(String.format(
                "%45s%n",
                "BILLING DETAILS"
        ));

        sb.append(line)
                .append("\n");

        sb.append(String.format(
                "%-17s: %s%n",
                "Confirmation No.",
                booking.getConfirmationNo()
        ));

        sb.append(String.format(
                "%-17s: %s%n",
                "Guest Name",
                booking.getGuest().getName()
        ));

        sb.append(String.format(
                "%-17s: %s (%s)%n",
                "Room",
                booking.getRoom().getRoomNo(),
                booking.getRoom().getRoomType()
        ));

        sb.append(String.format(
                "%-17s: RM%.2f / night%n",
                "Room Rate",
                booking.getRoom().getPrice()
        ));

        sb.append(String.format(
                "%-17s: %d%n",
                "Number of Nights",
                nights
        ));

        sb.append(line)
                .append("\n");

        sb.append(String.format(
                "%-17s: RM%.2f%n",
                "Total Amount",
                total
        ));

        sb.append(line);

        return ControllerResult.success(sb.toString());
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

    /**
     * Uses linear search because guest-name matching is partial and
     * case-insensitive, unlike exact confirmation-number lookup.
     */
    public ControllerResult searchByGuestName(String name) {

        String error =
                ValidationUtility.validateRequired(
                        name,
                        "Guest name"
                );

        if (error != null) {
            return ControllerResult.fail(error);
        }

        String needle =
                name.trim().toLowerCase();

        ListInterface<Booking> all =
                bookingController.getAll();

        StringBuilder sb =
                new StringBuilder();

        int matches = 0;

        // Linear search
        for (int i = 1; i <= all.size(); i++) {

            Booking booking =
                    all.getEntry(i);

            if (booking.getGuest()
                    .getName()
                    .toLowerCase()
                    .contains(needle)) {

                // Print header only once
                if (matches == 0) {
                    appendBookingTableHeader(sb);
                }

                appendBookingRow(
                        sb,
                        booking
                );

                matches++;
            }
        }

        if (matches == 0) {
            return ControllerResult.fail(
                    "No booking found for guest name: " + name
            );
        }

        appendBookingTableFooter(sb);

        sb.append("\n");
        sb.append(
                "Total Matches: "
        ).append(matches);

        return ControllerResult.success(
                sb.toString()
        );
    }

    /**
     * Displays all bookings sorted by check-in date, room type, or status.
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

    // Generates a summary of the current booking, room and payment information.
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
     * Generates a revenue report filtered by date and sorted by total amount.
     */
    public ControllerResult revenueReport(LocalDate start, LocalDate end) {

        Booking[] bookings =
                toArray(bookingController.getAll());

        if (start != null || end != null) {
            bookings = filterByDateRange(
                    bookings,
                    start,
                    end
            );
        }

        insertionSortByTotalDescending(bookings);

        double grandTotal = 0;
        int paidCount = 0;

        StringBuilder sb = new StringBuilder();

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(String.format(
                "%-10s %-20s %-8s %-12s %s%n",
                "Conf#",
                "Guest",
                "Nights",
                "Status",
                "Total (RM)"
        ));

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        for (Booking b : bookings) {

            // Revenue = successful payments only
            if (!paymentController.isPaid(
                    b.getConfirmationNo())) {
                continue;
            }

            double total = calculateTotal(b);

            grandTotal += total;
            paidCount++;

            sb.append(String.format(
                    "%-10s %-20s %-8d %-12s %.2f%n",
                    b.getConfirmationNo(),
                    b.getGuest().getName(),
                    nightsBetween(b),
                    b.getBookingStatus(),
                    total
            ));
        }

        if (paidCount == 0) {
            return ControllerResult.success(
                    "No paid bookings match the given date range."
            );
        }

        sb.append(
                "------------------------------------------------------------------------------\n"
        );

        sb.append(String.format(
                "Total Revenue Collected: RM %.2f (%d paid booking(s))%n",
                grandTotal,
                paidCount
        ));

        sb.append(
                "------------------------------------------------------------------------------"
        );

        return ControllerResult.success(sb.toString());
    }

    /**
     * Generates unpaid bookings sorted by outstanding amount in descending order.
     */
    public ControllerResult outstandingPaymentsReport(String statusFilter, LocalDate start, LocalDate end) {

        Booking[] bookings =
                toArray(
                        bookingController.getAll()
                );

        // Only unpaid bookings
        bookings =
                filterUnpaidBookings(bookings);

        // Optional booking status filter
        if (statusFilter != null
                && !statusFilter.isBlank()) {

            bookings =
                    filterByStatus(
                            bookings,
                            statusFilter.trim()
                    );
        }

        // Optional checkout date range filter
        if (start != null || end != null) {

            bookings =
                    filterByDateRange(
                            bookings,
                            start,
                            end
                    );
        }

        // Highest outstanding amount first
        insertionSortByTotalDescending(bookings);

        if (bookings.length == 0) {

            return ControllerResult.success(
                    "No outstanding payments match the given filters."
            );
        }

        double totalOutstanding = 0;

        StringBuilder sb =
                new StringBuilder();

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

            double due =
                    calculateTotal(b);

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
                        "Total Outstanding: RM %.2f (%d booking(s))%n",
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

    // Calculates nights consistently for billing and payment.
    static long nightsBetween(Booking booking) {
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        return nights <= 0 ? 1 : nights; // guard against same-day / bad-date edge cases
    }

    static double calculateTotal(Booking booking) {
        if (booking == null || booking.getRoom() == null) {
            return 0.0;
        }

        return booking.getRoom().getPrice() * nightsBetween(booking);
    }

    private void appendBookingTableHeader(StringBuilder sb) {

        String line =
                "----------------------------------------------------------------------------------------------";

        sb.append(line)
                .append("\n");

        sb.append(
                String.format(
                        "%-10s %-20s %-8s %-12s %-12s %s%n",
                        "Conf#",
                        "Guest",
                        "Room",
                        "Type",
                        "Status",
                        "Check-In -> Check-Out"
                )
        );

        sb.append(line)
                .append("\n");
    }


    private void appendBookingRow(
            StringBuilder sb,
            Booking booking) {

        sb.append(
                String.format(
                        "%-10s %-20s %-8s %-12s %-12s %s -> %s%n",
                        booking.getConfirmationNo(),
                        booking.getGuest().getName(),
                        booking.getRoom().getRoomNo(),
                        booking.getRoom().getRoomType(),
                        booking.getBookingStatus(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate()
                )
        );
    }


    private void appendBookingTableFooter(
            StringBuilder sb) {

        sb.append(
                "----------------------------------------------------------------------------------------------"
        );
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
    
    // Retrieves and displays bookings scheduled to arrive on the current date.
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

    // Retrieves and displays bookings scheduled to depart on the current date.
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
    
    // Generates a room status report based on the selected status filter.
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