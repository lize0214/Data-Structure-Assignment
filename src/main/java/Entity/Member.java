/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entity;

// Author: [你的名字]
public class Member {
    private String memberId;
    private String name;
    private String tier;         // Silver, Gold, Elite, Diamond, Platinum
    private int points;

    public Member() {
    }
    
    public Member(String memberId, String name, String tier, int points) {
        this.memberId = memberId;
        this.name = name;
        this.tier = tier;
        this.points = points;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
    
    // 格式: memberId,name,tier,points
    public static Member fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid Member data format: " + line);
        }
        return new Member(parts[0].trim(), parts[1].trim(), parts[2].trim(),
                Integer.parseInt(parts[3].trim()));
    }

    public String toCsvLine() {
        return memberId + "," + name + "," + tier + "," + points;
    }

    @Override
    public String toString() {
        return "Member{" + memberId + ", " + name + ", " + tier + ", pts:" + points + "}";
    }
}
