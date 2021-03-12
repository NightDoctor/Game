package Server.Game.Server;

import Server.Client.Client;
import Server.Game.Chest.Chest;
import Server.Game.Enemy.EnemyBot;
import Server.Game.GameProgress;
import Server.Interfaces.SendMessage;
import Server.Message.Message;
import Server.Message.MessageType;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;

// TODO при отключении пользователя нужно перезагружать сервер, иначе клиенту не начать игру

public class GameServer implements SendMessage {
    public static ArrayList<Client> gamerList = new ArrayList<>();
    public static ArrayList<GameProgress> gameProgresses = new ArrayList<>();

    public Chest chest;
    public EnemyBot enemy;

    public GameServer() {
    }

    public Chest checkChest(Client client) {
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

    public void checkEnemyAndChest(Client client) {
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