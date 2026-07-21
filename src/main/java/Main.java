/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
import Entity.Room;
import Control.RoomController;
import Utility.ControllerResult;

/**
 *
 * @author USER
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        RoomController roomController = new RoomController();
        Room room = new Room("205", "Deluxe", 260.00, "Dirty");
        ControllerResult result = roomController.add(room);
        System.out.println(result); 
        //hiiiii
    }
}
