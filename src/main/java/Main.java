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

    /**
     * "Clears" the console. NetBeans' built-in Output panel isn't a real
     * terminal - it ignores "cls"/"clear" system commands and ANSI escape
     * codes entirely, so the one thing that reliably works there is
     * pushing enough blank lines through that old output scrolls out of
     * view, which is what this does.
     */
    private static void clearScreen() {
        for (int i = 0; i < 60; i++) {
            System.out.println();
        }
    }

    public static void main(String[] args) {

        clearScreen();

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
        System.out.println(result);
        
        // abc123
        System.out.println(result); 
        //hiiiii
        //bye
    }
}