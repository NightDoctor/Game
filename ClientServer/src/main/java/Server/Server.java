package Server;

import Server.Client.Client;
import Server.Database.DatabaseHelper;
import Server.Interfaces.SendMessage;
import Server.Interfaces.ServerMainLoop;
import Server.Message.Message;
import Server.Message.MessageType;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class Server implements SendMessage, ServerMainLoop {
    public static List<Client> connectionList = new ArrayList<>();
    public static List<Thread> threadList = new ArrayList<>();

    public abstract void startHandler(Socket socket);

    public void start(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Server with port + " + port + " started");

            Thread threadCheckClientConnection = new Thread(() -> checkClientConnection());
            threadCheckClientConnection.start();

            while (true) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Socket finalSocket = socket;
                Thread threadHandler = new Thread(() -> startHandler(finalSocket));
                threadHandler.start();
                threadList.add(threadHandler);
            }
        }
    }

    public Client serverHandshake(Connection connection) throws IOException {
        while (true) {
            connection.send(new Message(MessageType.CONNECTION_REQUEST));
            Message message = connection.receive();
            if (message != null) {
                if (message.getType() == MessageType.CONNECTION_ACCEPTED) {
                    Client client = new Client(connection);
                    connectionList.add(client);
                    connection.send(new Message(MessageType.CONNECTION_ACCEPTED));
                    return client;
                }
            }
        }
    }

    public Client getAccount(Connection connection, Message message) throws JsonProcessingException {
        String[] data = message.getData().split("#");
        //Client cl = (Client)Converter.xmlToObject(message.getData(), new Client());
        String mail, password;
        try {
            mail = data[0];
            password = data[1];
        } catch (Exception e) {
            sendMessage(connection, new Message(MessageType.ERROR_AUTHORIZATION));
            ConsoleHelper.writeMessage("Client wasn't founded");
            return null;
        }
        //Client client = DatabaseHelper.entryCli(mail, password);
        Client client = new Client(1, mail, password, mail, 1000, true);

        if (client == null) {
            sendMessage(connection, new Message(MessageType.ERROR_AUTHORIZATION));
            ConsoleHelper.writeMessage("Client wasn't founded");
            return null;
        }
        client.setConnection(connection);

        String mess = client.getId() + "#" + client.getNickname() + "#" + client.getPassword() + "#" + client.getEmail() +
                "#" + client.getGold();

        //DatabaseHelper.setStatus(client.getId(), true);

        ConsoleHelper.writeMessage("Client was founded");
        sendMessage(connection, new Message(MessageType.AUTHORIZATION, mess));
        return client;
    }

    private void checkClientConnection() {
        while (true) {
            for (Client client : connectionList) {
                try {
                    client.getConnection().send(new Message(MessageType.TEST_WORK));
                    if (!client.isOnline()) {
                        Thread.sleep(1000);
                        if (!client.isOnline()) {
                            clientRemove(client, false);
                            System.out.println("check close");
                        }
                    }
                    continue;
                } catch (JsonProcessingException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void clientRemove(Client client, boolean isGame) {
        if (isGame) {
            client.close();

            threadList.get(0).interrupt();
            //DatabaseHelper.setStatus(client.getId(), false);
        }

        ConsoleHelper.writeMessage("Client " + client.getConnection() + " disconnected");
    }
}
