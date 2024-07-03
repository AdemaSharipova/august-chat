package kz.timka.client;

import javafx.scene.control.TextArea;

import java.io.*;

public class ChatLocalHistory {
    private static final String fileName = "local_history.txt";

    public static void writeMessage(String sender, String receiver, String message, boolean isPrivate) {
        String formattedMessage = String.format("[%s] %s -> %s: %s\n",
                isPrivate ? "PRIVATE" : "BROADCAST", sender, receiver, message);

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName, true))) {
            out.write(formattedMessage.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadMessages(String username, TextArea msgArea) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("-> " + username) || line.contains("-> ALL") || line.contains(username + " ->")) {
                    msgArea.appendText(line + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
