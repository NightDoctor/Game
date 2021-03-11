package Server.MainServer;

import Server.Client.Client;
import Server.Connection;
import Server.ConsoleHelper;
import Server.Game.Server.GameServer;
import Server.Server;
import Server.Database.DatabaseHelper;
import Server.Interfaces.SendBroadcast;
import Server.Message.Message;
import Server.Message.MessageType;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.net.Socket;
import java.sql.Struct;

public class MainServer extends Server implements SendBroadcast {

    @Override
    public void startHandler(Socket socket) {
        Client client;
        try (Connection connection = new Connection(socket)) {
            ConsoleHelper.writeMessage("Установлено новое соединение с удаленным адресом " + socket.getRemoteSocketAddress());
            client = serverHandshake(connection);

            while (true) {
                Message message = connection.receive();
                if (message != null) {
                    if (message.getType() == MessageType.AUTHORIZATION) {
                        ConsoleHelper.writeMessage("Client want to authorization");
                        client = getAccount(connection, message);
                        if (client == null) {
                            return;
                        }
                        break;
                    } else if (message.getType() == MessageType.REGISTRATION) {
                        ConsoleHelper.writeMessage("Client want to registration");
                        client = addAccount(connection, message);
                        break;
                    }
                } else {
                    clientRemove(client, false);
                    return;
                }
            }
            client.setSocket(socket);
            serverMainLoop(client);
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка при обмене данными с удаленным адресом " + e.getMessage());
        }
    }

    @Override
    public void serverMainLoop(Client client) throws IOException {
        while (true) {
            try {
                Message message = client.getConnection().receive();
                if (message != null) {
                    if (message.getType() == MessageType.TEXT) {
                        String s = client.getNickname() + ": " + message.getData();
                        Message formattedMessage = new Message(MessageType.TEXT, s);
                        sendBroadcastMessage(formattedMessage);
                    } else if (message.getType() == MessageType.TEST_WORK) {
                        client.setOnline(true);
                    } else if (message.getType() == MessageType.GAME) {
                        clientGoGame(client);
                        return;
                    } else {
                        ConsoleHelper.writeMessage("Error type " + message.getType() + " " + message.getData());
                    }
                } else {
                    clientRemove(client, false);
                    return;
                }
            } catch (NullPointerException e) {
                break;
            }
        }
    }

    private Client addAccount(Connection connection, Message message) throws JsonProcessingException {
        String[] data = message.getData().split("#");

        String mail = data[0];
        String password = data[1];
        String nickname = data[2];

        try {
            //DatabaseHelper.addCharacter(new Client(nickname, password, mail, 1000, true));
            ConsoleHelper.writeMessage("Client was added");
        } catch (Exception e) {
            sendMessage(connection, new Message(MessageType.ERROR_REGISTRATION));
            ConsoleHelper.writeMessage("Client wasn't added");
            return null;
        }

        //Client client = DatabaseHelper.entryCli(mail, password);
        Client client = new Client();
        client.setConnection(connection);

        String mess = client.getId() + "#" + client.getNickname() + "#" + client.getPassword() + "#" + client.getEmail() + "#" +
                client.getGold();

        //DatabaseHelper.setStatus(client.getId(), true);

        sendMessage(connection, new Message(MessageType.REGISTRATION, mess));
        return client;
    }

    private void clientGoGame(Client client) {
        connectionList.remove(client);
        clientRemove(client, true);
        GameServer.potentialPlayersList.add(client);
        ConsoleHelper.writeMessage("Client " + client.getNickname() + " goes to game");
    }
}