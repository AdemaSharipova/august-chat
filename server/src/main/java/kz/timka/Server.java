package kz.timka;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;

    private List<ClientHandler> list;

    private AuthenticationProvider authenticationProvider;

    public Server(int port) {
        this.port = port;
        this.list = new ArrayList<>();
        this.authenticationProvider = new InMemoryAuthProvider();
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту 8189. Ожидаем подключение клиента...");
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String msg) throws IOException {
        for(ClientHandler clientHandler : list) {
            clientHandler.sendMessage(msg);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        list.add(clientHandler);
        sendClientsList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        list.remove(clientHandler);
        sendClientsList();
    }

    public boolean isUserOnline(String username) {
        for(ClientHandler clientHandler : list) {
            if(clientHandler.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void sendPrivateMsg(ClientHandler sender, String receiver, String msg) throws IOException{
        for(ClientHandler c : list) {
            if(c.getUsername().equals(receiver)) {
                c.sendMessage("From: " + sender.getUsername() + " Message: " + msg);
                sender.sendMessage("Receiver: " + receiver + " Message: " + msg);
                return;
            }
        }
        sender.sendMessage("Unable to send message to " + receiver);
    }

    public void sendClientsList(){
        StringBuilder builder =  new StringBuilder("/clients_list ");
        for(ClientHandler c: list) {
            builder.append(c.getUsername()).append(" ");
        }
        builder.setLength(builder.length() - 1);
        String clientsList = builder.toString();
        for(ClientHandler c: list) {
            c.sendMessage(clientsList);
        }
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public void changeNickname(ClientHandler sender, String currentNick, String newNick){
        if (sender.getUsername().equals(currentNick)) {
            authenticationProvider.changeList(currentNick, newNick);
            sender.sendMessage("Username - " + currentNick + " changed to " + newNick);
        } else {
            sender.sendMessage("Incorrect Current Username");
        }
    }
}