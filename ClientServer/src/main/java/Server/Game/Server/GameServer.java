package Server.Game.Server;

import Server.Client.Client;
import Server.Connection;
import Server.ConsoleHelper;
import Server.Game.Chest.Chest;
import Server.Game.Enemy.EnemyBot;
import Server.Game.GameProgress;
import Server.Game.Item.Item;
import Server.Message.Message;
import Server.Message.MessageType;
import Server.Server;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

// TODO при отключении пользователя нужно перезагружать сервер, иначе клиенту не начать игру

public class GameServer extends Server {
    public static ArrayList<Client> gamerList = new ArrayList<>();
    public static ArrayList<Client> potentialPlayersList = new ArrayList<>();
    public static ArrayList<GameProgress> gameProgresses = new ArrayList<>();

    Chest chest;
    EnemyBot enemy;

    public GameServer() {
    }

    @Override
    public void startHandler(Socket socket) {
        Client client;
        try (Connection connection = new Connection(socket)) {
            ConsoleHelper.writeMessage("Установлено новое соединение с удаленным адресом " + socket.getRemoteSocketAddress() + " на игру");
            client = serverHandshake(connection);

            while (true) {
                Message message = connection.receive();
                if (message != null) {
                    if (message.getType() == MessageType.AUTHORIZATION) {
                        ConsoleHelper.writeMessage("Client want to authorization in the game");
                        if (checkAccount(client, message)) {
                            sendMessage(client.getConnection(), new Message(MessageType.AUTHORIZATION));
                            break;
                        } else {
                            clientRemove(client, false);
                            return;
                        }
                    }
                } else {
                    clientRemove(client, false);
                    return;
                }
            }

            serverMainLoop(client);
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка при обмене данными с удаленным адресом");
        }
    }

    private boolean checkAccount(Client clientPat, Message message) throws JsonProcessingException {
        for (Client client : potentialPlayersList) {
            String[] data = message.getData().split("#");
            String mail, password;

            try {
                mail = data[0];
                password = data[1];
            } catch (Exception e) {
                sendMessage(clientPat.getConnection(), new Message(MessageType.ERROR_AUTHORIZATION));
                ConsoleHelper.writeMessage("Client wasn't founded");
                return false;
            }

            if (client.getEmail().equals(mail) && client.getPassword().equals(password)) {
                potentialPlayersList.remove(client);
                gamerList.add(clientPat);
                return true;
            }
        }
        return false;
    }

    @Override
    public void serverMainLoop(Client client) throws IOException {
        while (true) {
            Message message = client.getConnection().receive();
            if (message != null) {
                if (message.getType() == MessageType.TEST_WORK) {
                    client.setOnline(true);
                } else if (message.getType() == MessageType.W) {
                    client.setZ(client.getZ() + 1);
                    checkEnemyAndChest(client);
                    client.setOnline(true);
                } else if (message.getType() == MessageType.A) {
                    client.setX(client.getX() - 1);
                    checkEnemyAndChest(client);
                    client.setOnline(true);
                } else if (message.getType() == MessageType.S) {
                    client.setZ(client.getZ() - 1);
                    checkEnemyAndChest(client);
                    client.setOnline(true);
                } else if (message.getType() == MessageType.D) {
                    client.setX(client.getX() + 1);
                    checkEnemyAndChest(client);
                    client.setOnline(true);
                } else if (message.getType() == MessageType.DOWN_E && checkChest(client) != null) {
                    chest = checkChest(client);
                    if (!chest.isOpen()) {
                        for (Item item : chest.getListItem()) {
                            client.addListItem(item);
                        }
                        chest.setOpen(true);
                    }
                    client.setOnline(true);
                } else if (message.getType() == MessageType.UP_E && checkChest(client) != null) {
                    client.setOnline(true);
                } else if (message.getType() == MessageType.READY) {
                    client.setReady(true);
                } else {
                    ConsoleHelper.writeMessage("Error type " + message.getType() + " " + message.getData());
                }
            } else {
                clientRemove(client, false);
                System.out.println("main in game close");
                return;
            }
        }
    }

    private Chest checkChest(Client client) {
        for (GameProgress game : gameProgresses) {
            if (game.firstGamer.getConnection().equals(client.getConnection())) {
                chest = game.isTheChestFar1(client.getX(), client.getZ());
                enemy = game.isTheEnemyFar1(client.getX(), client.getZ());
            }

//            if (game.secondGamer.getConnection().equals(client.getConnection())) {
//                chest = game.isTheChestFar1(client.getX(), client.getZ());
//                enemy = game.isTheEnemyFar1(client.getX(), client.getZ());
//            }
        }

        if (chest != null) {
            return chest;
        }
        return null;
    }

    private void checkEnemyAndChest(Client client) {
        for (GameProgress game : gameProgresses) {
            if (game.firstGamer.getConnection().equals(client.getConnection())) { //TODO добавить для второго пользователя
                chest = game.isTheChestFar1(client.getX(), client.getZ());
                enemy = game.isTheEnemyFar1(client.getX(), client.getZ());
            }

//            if (game.secondGamer.getConnection().equals(client.getConnection())) { //TODO добавить для второго пользователя
//                chest = game.isTheChestFar1(client.getX(), client.getZ());
//                enemy = game.isTheEnemyFar1(client.getX(), client.getZ());
//            }
        }

        if (chest != null) {
            try {
                sendMessage(client.getConnection(), new Message(MessageType.GOT_CHEST, chest.getX() + "#" + chest.getZ()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        if (enemy != null) {
            try {
                sendMessage(client.getConnection(), new Message(MessageType.GOT_ENEMY, enemy.getX() + "#" + enemy.getZ()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    public void createAPairOfPlayers() {
        while (true) {
            int listSize = gamerList.size();
            if (listSize > 0) {
                int idGamerInList = GameProgress.rnd(0, listSize - 1);
                Client firstGamer = gamerList.get(idGamerInList);
                gamerList.remove(firstGamer);

                idGamerInList = GameProgress.rnd(0, listSize - 2);
                Client secondGamer = null;//= gamerList.get(idGamerInList);
                gamerList.remove(secondGamer);

                GameProgress thread = new GameProgress(firstGamer, secondGamer);
                thread.start();
                gameProgresses.add(thread);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}