package kz.timka.client;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    @FXML
    private TextField msgField, loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextArea msgArea;

    @FXML
    private HBox loginBox, msgBox;

    @FXML
    ListView<String> clientsList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    private ChatLocalHistory chatLocalHistory;

    public void setUsername(String username) {
        this.username = username;
        if(this.username == null) {
            loginBox.setVisible(true);
            loginBox.setManaged(true);
            msgBox.setVisible(false);
            msgBox.setManaged(false);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
        } else {
            loginBox.setVisible(false);
            loginBox.setManaged(false);
            msgBox.setVisible(true);
            msgBox.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);

            ChatLocalHistory.loadMessages(this.username, msgArea);
        }

    }

    public void connect() {
        try {
            socket = new Socket("localhost", 8189);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            chatLocalHistory = new ChatLocalHistory();

            new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        handleMessageFromServer(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnect();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to connect to server");
        }
    }

    private void handleMessageFromServer(String msg) {
        Platform.runLater(() -> {
            try {
                if (msg.startsWith("/login_ok ")) {
                    setUsername(msg.split("\\s+")[1]);
                } else if (msg.startsWith("/login_failed ")) {
                    String reason = msg.split("\\s+", 2)[1];
                    msgArea.appendText(reason + "\n");
                } else if (msg.startsWith("/clients_list ")) {
                    handleClientsList(msg);
                } else if (msg.matches("\\[.*\\] .* -> .*: .*")) {
                    handleChatMessage(msg);
                } else {
                    msgArea.appendText(msg + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleClientsList(String msg) {
        clientsList.getItems().clear();
        String[] tokens = msg.split("\\s+");
        for (int i = 1; i < tokens.length; i++) {
            clientsList.getItems().add(tokens[i]);
        }
    }

    private void handleChatMessage(String msg) {

        String[] parts = msg.split(" ", 5);
        if (parts.length >= 4) {
            String type = parts[0].substring(1, parts[0].length() - 1);
            String sender = parts[1];
            String receiver = parts[3].substring(0, parts[3].length() - 1);
            String message = parts[4];

            msgArea.appendText(msg + "\n");
            if (sender.equals(username)) {
                ChatLocalHistory.writeMessage(sender, receiver, message, type.equals("PRIVATE"));
            }

        }
    }

    public void login() {
        if(socket == null || socket.isClosed()) {
            connect();
        }

        if(loginField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Имя пользователя не может быть пустым", ButtonType.OK);
            alert.showAndWait();
            return;

        }
        try {
            out.writeUTF("/login " + loginField.getText() + " " + passwordField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void disconnect() {
        setUsername(null);
        if(socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно отправить сообщение");
            alert.showAndWait();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUsername(null);
    }
}