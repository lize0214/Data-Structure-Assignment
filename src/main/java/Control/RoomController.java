package Control;

import Entity.Room;
import Utility.ControllerResult;
import Utility.ValidationUtility;

public class RoomController extends AbstractEntityController<Room, String> {

    public RoomController() {
        super("data/rooms.txt");
        loadFromFile();
    }

    @Override
    protected Room parseCsvLine(String line) {
        return Room.fromCsvLine(line);
    }

    @Override
    protected String toCsvLine(Room item) {
        return item.toCsvLine();
    }

    @Override
    protected String getKey(Room item) {
        return item.getRoomNo();
    }

    public ControllerResult update(String roomNo, String newRoomType, double newPrice, String newStatus) {
        ValidationUtility.ValidationAccumulator acc = new ValidationUtility.ValidationAccumulator();
        acc.check(ValidationUtility.validateRequired(newRoomType, "Room Type"));
        acc.check(ValidationUtility.validatePositive(newPrice, "Price"));
        acc.check(ValidationUtility.validateRoomStatus(newStatus));
        if (acc.hasErrors()) return ControllerResult.fail(acc.getErrorMessage());

        Room room = findByKey(roomNo);
        if (room == null) return ControllerResult.fail("Room not found: " + roomNo);

        room.setRoomType(newRoomType);
        room.setPrice(newPrice);
        room.setStatus(newStatus);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updateStatus(String roomNo, String newStatus) {
        String error = ValidationUtility.validateRoomStatus(newStatus);
        if (error != null) return ControllerResult.fail(error);

        Room room = findByKey(roomNo);
        if (room == null) return ControllerResult.fail("Room not found: " + roomNo);

        room.setStatus(newStatus);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updateRoomType(String roomNo, String newRoomType) {
        String error = ValidationUtility.validateRequired(newRoomType, "Room Type");
        if (error != null) return ControllerResult.fail(error);

        Room room = findByKey(roomNo);
        if (room == null) return ControllerResult.fail("Room not found: " + roomNo);

        room.setRoomType(newRoomType);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult updatePrice(String roomNo, double newPrice) {
        String error = ValidationUtility.validatePositive(newPrice, "Price");
        if (error != null) return ControllerResult.fail(error);

        Room room = findByKey(roomNo);
        if (room == null) return ControllerResult.fail("Room not found: " + roomNo);

        room.setPrice(newPrice);
        saveToFile();
        return ControllerResult.success();
    }
}
