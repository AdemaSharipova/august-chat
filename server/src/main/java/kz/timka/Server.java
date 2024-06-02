package kz.timka;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Server {
    private int port;
    private List<ClientHandler> clientHandlers;
    private List<String> usernames;


    public Server(int port) {
        clientHandlers = new ArrayList<>();
        usernames = new ArrayList<>();
        this.port = port;
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту 8189. Ожидаем подключение клиента...");
            while (true) {
                Socket socket = serverSocket.accept();
                subscribe(new ClientHandler(this, socket));
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void addUsername(String username) {
        usernames.add(username);
    }

    public void deleteUsername(String username) {
        usernames.remove(username);
    }


    public boolean isUsernameUnique(String username) {
        return !usernames.contains(username);
    }


    public void broadcastMessage(String msg) throws IOException {
        for (ClientHandler clientHandler: clientHandlers) {
            clientHandler.sendMessage(msg);
        }
    }

    public void unicastMessage(String username, String msg) throws IOException {
        for (ClientHandler clientHandler: clientHandlers) {
            if (username.equals(clientHandler.getUsername())) {
                clientHandler.sendMessage(msg);
            }
        }
    }


    public void subscribe(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        deleteUsername(clientHandler.getUsername());
    }
}