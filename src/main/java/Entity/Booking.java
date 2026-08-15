package Entity;

import java.time.LocalDate;

// Author: [你的名字]
public class Booking {
    private String confirmationNo;
    private Guest guest;
    private Room room;
    // Keep the foreign-key values separately so legacy bookings can still be
    // written back when the referenced guest or room record no longer exists.
    private String guestId;
    private String roomNo;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String bookingStatus;

    public Booking() {
    }

    public Booking(String confirmationNo, Guest guest, Room room,
                   LocalDate checkInDate, LocalDate checkOutDate, String bookingStatus) {
        this.confirmationNo = confirmationNo;
        this.guest = guest;
        this.room = room;
        this.guestId = guest == null ? null : guest.getGuestId();
        this.roomNo = room == null ? null : room.getRoomNo();
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingStatus = bookingStatus;
    }

    public String getConfirmationNo() { return confirmationNo; }
    public void setConfirmationNo(String confirmationNo) { this.confirmationNo = confirmationNo; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) {
        this.guest = guest;
        if (guest != null) this.guestId = guest.getGuestId();
    }

    public Room getRoom() { return room; }
    public void setRoom(Room room) {
        this.room = room;
        if (room != null) this.roomNo = room.getRoomNo();
    }

    public String getGuestId() { return guestId; }
    public String getRoomNo() { return roomNo; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    // 注意：这里只能拿到guestId和roomNo，不能直接生成完整Booking对象
    // 需要在Controller层查到对应的Guest和Room对象后，再手动组装
    // 格式: confirmationNo,guestId,roomNo,checkInDate,checkOutDate,bookingStatus
    public static Booking fromCsvLine(String line, Guest guest, Room room) {
        String[] parts = line.split(",");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid Booking data format: " + line);
        }
        Booking booking = new Booking(
                parts[0].trim(),
                guest,
                room,
                LocalDate.parse(parts[3].trim()),
                LocalDate.parse(parts[4].trim()),
                parts[5].trim()
        );
        // Preserve the IDs from the booking file even if lookup returned null.
        booking.guestId = parts[1].trim();
        booking.roomNo = parts[2].trim();
        return booking;
    }

    public String toCsvLine() {
        if (guestId == null || guestId.isEmpty() || roomNo == null || roomNo.isEmpty()) {
            throw new IllegalStateException("Booking " + confirmationNo
                    + " is missing its guest ID or room number.");
        }
        return confirmationNo + "," + guestId + "," + roomNo + "," +
                checkInDate + "," + checkOutDate + "," + bookingStatus;
    }

    @Override
    public String toString() {
        String guestDisplay = guest == null ? guestId : guest.getName();
        return "Booking{" + confirmationNo + ", Guest:" + guestDisplay + ", Room:" + roomNo + "}";
    }
}
