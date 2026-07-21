/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entity;

// Author: [你的名字]
public class Guest {
    private String guestId;
    private String name;
    private String contact;
    
    public Guest() {
    }

    public Guest(String guestId, String name, String contact) {
        this.guestId = guestId;
        this.name = name;
        this.contact = contact;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    // 格式: guestId,name,contact
    public static Guest fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Guest data format: " + line);
        }
        return new Guest(parts[0].trim(), parts[1].trim(), parts[2].trim());
    }

    public String toCsvLine() {
        return guestId + "," + name + "," + contact;
    }

    @Override
    public String toString() {
        return "Guest{" + guestId + ", " + name + ", " + contact + "}";
    }
}
