package Entity;

import java.time.LocalDate;

// Author: [你的名字]
public class Booking {
    private String confirmationNo;
    private Guest guest;
    private Room room;
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
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingStatus = bookingStatus;
    }

    public String getConfirmationNo() { return confirmationNo; }
    public void setConfirmationNo(String confirmationNo) { this.confirmationNo = confirmationNo; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

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
        return new Booking(
                parts[0].trim(),
                guest,
                room,
                LocalDate.parse(parts[3].trim()),
                LocalDate.parse(parts[4].trim()),
                parts[5].trim()
        );
    }

    public String toCsvLine() {
        return confirmationNo + "," + guest.getGuestId() + "," + room.getRoomNo() + "," +
                checkInDate + "," + checkOutDate + "," + bookingStatus;
    }

    @Override
    public String toString() {
        return "Booking{" + confirmationNo + ", Guest:" + guest.getName() + ", Room:" + room.getRoomNo() + "}";
    }
}