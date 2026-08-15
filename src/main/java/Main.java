/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
import Entity.Room;
import Control.RoomController;
import Utility.ControllerResult;
import Boundary.MainMenuUI;
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

        MainMenuUI mainMenuUI = new MainMenuUI();
        mainMenuUI.run();
    }
}