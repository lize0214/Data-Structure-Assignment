package Entity;

import java.time.LocalDateTime;

// Author: [你的名字]
public class HousekeepingTask {
    private String taskId;
    private Room room;
    private String status;
    private LocalDateTime timestamp;

    public HousekeepingTask() {
    }

    public HousekeepingTask(String taskId, Room room, String status, LocalDateTime timestamp) {
        this.taskId = taskId;
        this.room = room;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // 注意：CSV这一行只有roomNo字符串，没办法凭空生成完整Room对象
    // 需要Controller层先查到对应Room对象，再传进来
    // 格式: taskId,roomNo,status,timestamp
    public static HousekeepingTask fromCsvLine(String line, Room room) {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid HousekeepingTask data format: " + line);
        }
        return new HousekeepingTask(parts[0].trim(), room, parts[2].trim(),
                LocalDateTime.parse(parts[3].trim()));
    }

    public String toCsvLine() {
        return taskId + "," + room.getRoomNo() + "," + status + "," + timestamp;
    }

    @Override
    public String toString() {
        return "Task{" + taskId + ", Room:" + room.getRoomNo() + ", " + status + ", " + timestamp + "}";
    }
}