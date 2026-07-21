/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utility;

import java.io.*;
import java.nio.charset.StandardCharsets;

// Author: [Your Name]
public class FileUtility {

    // Read all lines from a file.
    // Return the content as a String array.
    public static String[] readLines(String filePath) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + filePath + " - " + e.getMessage());
        }
        return lines.toArray(new String[0]);
    }

    // Append a new line to the file.
    // Used for saving new data.
    public static void appendLine(String filePath, String line) {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath, true), StandardCharsets.UTF_8))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + filePath + " - " + e.getMessage());
        }
    }

    // Overwrite the file with all lines.
    // Used after updating or deleting data.
    public static void writeAllLines(String filePath, String[] lines) {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath, false), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + filePath + " - " + e.getMessage());
        }
    }

    // Check whether the file exists.
    public static boolean fileExists(String filePath) {
        File f = new File(filePath);
        return f.exists() && f.isFile();
    }
}