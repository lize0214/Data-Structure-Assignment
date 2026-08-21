package Entity;

import java.time.LocalDate;

/**
 * @author Chin Yik Heng
 */
public class Booking {
    private String confirmationNo;
    private Guest guest;
    private Member member;
    private Room room;
    // Keep foreign-key values separately so unresolved legacy records can still
    // be written back even if the referenced holder or room no longer exists.
    private String holderId;
    private String roomNo;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String bookingStatus;
    private BookingType bookingType;

    public Booking() {
    }

    public Booking(String confirmationNo, Guest guest, Room room,
                   LocalDate checkInDate, LocalDate checkOutDate, String bookingStatus) {
        this(confirmationNo, guest, room, checkInDate, checkOutDate,
                bookingStatus, BookingType.STANDARD);
    }

    public Booking(String confirmationNo, Guest guest, Room room,
                   LocalDate checkInDate, LocalDate checkOutDate, String bookingStatus,
                   BookingType bookingType) {
        this.confirmationNo = confirmationNo;
        this.guest = guest;
        this.member = null;
        this.room = room;
        this.holderId = guest == null ? null : guest.getGuestId();
        this.roomNo = room == null ? null : room.getRoomNo();
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingStatus = bookingStatus;
        this.bookingType = bookingType;
    }

    public Booking(String confirmationNo, Member member, Room room,
                   LocalDate checkInDate, LocalDate checkOutDate, String bookingStatus) {
        this(confirmationNo, member, room, checkInDate, checkOutDate,
                bookingStatus, BookingType.VIP_ALLOCATION);
    }

    public Booking(String confirmationNo, Member member, Room room,
                   LocalDate checkInDate, LocalDate checkOutDate, String bookingStatus,
                   BookingType bookingType) {
        this.confirmationNo = confirmationNo;
        this.guest = null;
        this.member = member;
        this.room = room;
        this.holderId = member == null ? null : member.getMemberId();
        this.roomNo = room == null ? null : room.getRoomNo();
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingStatus = bookingStatus;
        this.bookingType = bookingType;
    }

    public String getConfirmationNo() { return confirmationNo; }
    public void setConfirmationNo(String confirmationNo) { this.confirmationNo = confirmationNo; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) {
        this.guest = guest;
        if (guest != null) {
            this.member = null;
            this.holderId = guest.getGuestId();
        } else if (member == null) {
            this.holderId = null;
        }
    }

    public Member getMember() { return member; }
    public void setMember(Member member) {
        this.member = member;
        if (member != null) {
            this.guest = null;
            this.holderId = member.getMemberId();
        } else if (guest == null) {
            this.holderId = null;
        }
    }

    public Room getRoom() { return room; }
    public void setRoom(Room room) {
        this.room = room;
        if (room != null) this.roomNo = room.getRoomNo();
    }

    /** Returns the Guest ID for Guest bookings, or null for Member bookings. */
    public String getGuestId() {
        return isMemberBooking() ? null : holderId;
    }

    /** Returns the Member ID for Member bookings, or null for Guest bookings. */
    public String getMemberId() {
        return isMemberBooking() ? holderId : null;
    }

    public String getHolderId() { return holderId; }

    public String getHolderName() {
        if (member != null) return member.getName();
        if (guest != null) return guest.getName();
        return holderId == null || holderId.isBlank() ? "Unknown" : holderId;
    }

    public boolean isMemberBooking() {
        return member != null || (guest == null && holderId != null
                && holderId.toUpperCase().startsWith("M"));
    }

    public String getRoomNo() { return roomNo; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public BookingType getBookingType() { return bookingType; }
    public void setBookingType(BookingType bookingType) { this.bookingType = bookingType; }

    // Existing Guest rows remain unchanged. The second column may now contain
    // either a Guest ID (G...) or Member ID (M...).
    // Format: confirmationNo,holderId,roomNo,checkInDate,checkOutDate,bookingStatus[,bookingType]
    public static Booking fromCsvLine(String line, Guest guest, Room room) {
        return fromCsvLine(line, guest, null, room);
    }

    public static Booking fromCsvLine(String line, Guest guest, Member member, Room room) {
        String[] parts = line.split(",");
        if (parts.length != 6 && parts.length != 7) {
            throw new IllegalArgumentException("Invalid Booking data format: " + line);
        }
        BookingType type = parts.length == 7
                ? BookingType.valueOf(parts[6].trim().toUpperCase())
                : BookingType.STANDARD;
        String holderId = parts[1].trim();
        Booking booking;
        if (member != null || holderId.toUpperCase().startsWith("M")) {
            booking = new Booking(parts[0].trim(), member, room,
                    LocalDate.parse(parts[3].trim()), LocalDate.parse(parts[4].trim()),
                    parts[5].trim(), type);
        } else {
            booking = new Booking(parts[0].trim(), guest, room,
                    LocalDate.parse(parts[3].trim()), LocalDate.parse(parts[4].trim()),
                    parts[5].trim(), type);
        }
        // Preserve IDs from the file even when a referenced entity is missing.
        booking.holderId = holderId;
        booking.roomNo = parts[2].trim();
        return booking;
    }

    public String toCsvLine() {
        if (holderId == null || holderId.isEmpty() || roomNo == null || roomNo.isEmpty()) {
            throw new IllegalStateException("Booking " + confirmationNo
                    + " is missing its holder ID or room number.");
        }
        return confirmationNo + "," + holderId + "," + roomNo + "," +
                checkInDate + "," + checkOutDate + "," + bookingStatus + "," + bookingType;
    }

    @Override
    public String toString() {
        return "Booking{" + confirmationNo + ", Type:" + bookingType
                + ", Holder:" + getHolderName() + " (" + holderId + ")"
                + ", Room:" + roomNo + "}";
    }
}
