package Entity;

// Author: [你的名字]
public class Room {
    private String roomNo;
    private String roomType;
    private double price;
    private String status;

    public Room() {
    }

    public Room(String roomNo, String roomType, double price, String status) {
        this.roomNo = roomNo;
        this.roomType = roomType;
        this.price = price;
        this.status = status;
    }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static Room fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid Room data format: " + line);
        }
        return new Room(parts[0].trim(), parts[1].trim(), Double.parseDouble(parts[2].trim()), parts[3].trim());
    }

    public String toCsvLine() {
        return roomNo + "," + roomType + "," + price + "," + status;
    }

    @Override
    public String toString() {
        return "Room{" + roomNo + ", " + roomType + ", RM" + price + ", " + status + "}";
    }
}