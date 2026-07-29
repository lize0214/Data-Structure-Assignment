/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
import Entity.Room;
import Control.RoomController;
import Utility.ControllerResult;
import Boundery.MainMenuUI;

/**
 *
 * @author USER
 */
public class Main {
    public static void main(String[] args) {
        RoomController roomController = new RoomController();
        Room room = new Room("205", "Deluxe", 260.00, "Dirty");
        ControllerResult result = roomController.add(room);
        if (result.isOk()) {
            System.out.println("Room added: " + result.getMessage());
        } else {
            System.out.println("Error adding room: " + result.getMessage());
        }

        MainMenuUI mainMenuUI = new MainMenuUI();
        mainMenuUI.run();
    }
}
