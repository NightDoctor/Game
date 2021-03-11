package Server.Game;

import Server.Client.Client;
import Server.ConsoleHelper;
import Server.Game.Chest.Chest;
import Server.Game.Chest.ChestType;
import Server.Game.Enemy.EnemyBot;
import Server.Game.Enemy.EnemyType;
import Server.Game.Item.Item;
import Server.Game.Item.ItemType;
import Server.Game.Server.GameServer;
import Server.Interfaces.ServerMainLoop;
import Server.Message.Converter;
import Server.Message.Message;
import Server.Message.MessageType;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameProgress extends Thread {
    private List<Chest> listChests;
    private List<EnemyBot> listEnemy;
    public Client firstGamer;
    public Client secondGamer;
    private GameServer gameServer;

    public GameProgress(Client firstGamer, Client secondGamer) {
        this.firstGamer = firstGamer;
        this.secondGamer = secondGamer;
    }

    @Override
    public void run() {
        gameServer = new GameServer();

        loadingGame();
        startGame();

        firstRound();
        secondRound();
    }

    private void firstRound() {
        int timeRound = 0;

        try {
            gameServer.sendMessage(firstGamer.getConnection(), new Message(MessageType.START));
            //gameServer.sendMessage(secondGamer.getConnection(), new Message(MessageType.START));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        while (timeRound != 10) {
            try {
                Thread.sleep(1000);
                timeRound++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            gameServer.sendMessage(firstGamer.getConnection(), new Message(MessageType.ROUND_END));
            //gameServer.sendMessage(secondGamer.getConnection(), new Message(MessageType.ROUND_END));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void secondRound() {
        sendInfo(MessageType.LOADING_GAME, null);
        sendInfo(MessageType.SET_CARD_START, null);

        createHand(firstGamer);
        //createHand(secondGamer);

        sendHand(firstGamer);
        //sendHand(secondGamer);

        sendInfo(MessageType.SET_END, null);
    }

    private void sendHand(Client gamer) {
        try {
            for (Item item : gamer.getListHand()) {
                gameServer.sendMessage(gamer.getConnection(), new Message(MessageType.SET_CARD_HAND, parseCard(item)));
            }
            gameServer.sendMessage(gamer.getConnection(), new Message(MessageType.SET_CARD_DECK,
                    String.valueOf(gamer.getListDeck().size())));

//            if (gamer.getConnection().equals(firstGamer.getConnection())) {
//                gameServer.sendMessage(gamer.getConnection(), new Message(MessageType.SET_ENEMY_DECK,
//                        String.valueOf(secondGamer.getListDeck().size())));
//                gameServer.sendMessage(gamer.getConnection(), new Message(MessageType.SET_ENEMY_HAND,
//                        String.valueOf(secondGamer.getListHand().size())));
//                gameServer.sendMessage(gamer.getConnection(), new Message(MessageType.SET_ENEMY_TABLE,
//                        String.valueOf(secondGamer.getListTable().size())));
//            } else {
//                gameServer.sendMessage(gamer.getConnection(), new Message(MessageType.SET_ENEMY_DECK,
//                        String.valueOf(firstGamer.getListDeck().size())));
//                gameServer.sendMessage(gamer.getConnection(), new Message(MessageType.SET_ENEMY_HAND,
//                        String.valueOf(firstGamer.getListHand().size())));
//                gameServer.sendMessage(gamer.getConnection(), new Message(MessageType.SET_ENEMY_TABLE,
//                        String.valueOf(firstGamer.getListTable().size())));
//            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void createHand(Client gamer) {
        if (gamer.getListItem().size() > 4) {
            for (int i = 0; i < 4; i++) {
                gamer.addListHand(gamer.getListItem().get(i));
                gamer.getListItem().remove(i);
            }
            gamer.setListDeck(gamer.getListItem());
        } else {
            gamer.setListHand(gamer.getListItem());
        }
        gamer.setListItem(null);
    }

    private void startGame() {
//        for (EnemyBot enemy : firstGamer.getListEnemy()) {
//            enemy.start();
//        }

//        for (EnemyBot enemy: secondGamer.getListEnemy()) {
//            enemy.start();
//        }

//        while (!firstGamer.isReady()) { //&& secondGamer.isReady()) {
//            System.out.println(firstGamer.isReady());
//
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        System.out.println("Start the game!");
    }

    private void loadingGame() {
        listChests = new ArrayList<>();
        listEnemy = new ArrayList<>();
        sendInfo(MessageType.LOADING_GAME, null);

        createChests();
        createEnemy();

        sendInfo(MessageType.SET_INFO, null);

        setInfo();
    }

    private String parseEnemy(EnemyBot enemy) {
        String enemyInfo = "";
        try {
            enemyInfo = Converter.objectToXml(enemy);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return enemyInfo;
    }

    private String parseChests(Chest chest) {
        String chestInfo = "";
        try {
            chestInfo = Converter.objectToXml(chest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return chestInfo;
    }

    private String parseCard(Item item) {
        String enemyInfo = "";
        try {
            enemyInfo = Converter.objectToXml(item);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return enemyInfo;
    }

    private void createEnemy() {
        float x, z;
        int typeEnemy;
        EnemyBot enemy = null;

        for (int i = 0; i < 10; ) {
            x = rnd(0, 3000);
            z = rnd(0, 1000);
            typeEnemy = rnd(0, 2);

            if (isTheChestFar(x, z) && isTheEnemyFar(x, z)) {
                switch (typeEnemy) {
                    case 0:
                        enemy = new EnemyBot(EnemyType.ACOLYTE, rnd(10, 20), rnd(1, 3), x, z);
                        break;
                    case 1:
                        enemy = new EnemyBot(EnemyType.WARRIOR, rnd(20, 35), rnd(5, 10), x, z);
                        break;
                    case 2:
                        enemy = new EnemyBot(EnemyType.HEADMAN, rnd(50, 70), rnd(25, 50), x, z);
                        break;
                }
                listEnemy.add(enemy);
                i++;
            }
        }
    }

    private void createChests() {
        List<Item> listItems = new ArrayList<>();
        int x, z;
        int typeChest;

        for (int i = 0; i < 35; ) {
            x = rnd(0, 3000);
            z = rnd(0, 1000);
            typeChest = rnd(0, 3);

            ChestType type = null;

            if (isTheChestFar(x, z)) {
                switch (typeChest) {
                    case 0:
                        listItems = createListItem(ChestType.COMMON);
                        break;
                    case 1:
                        listItems = createListItem(ChestType.RARE);
                        break;
                    case 2:
                        listItems = createListItem(ChestType.EPIC);
                        break;
                    case 3:
                        listItems = createListItem(ChestType.ENCHANTED);
                        break;
                }
                Chest chest = new Chest(x, z, listItems);

                listChests.add(chest);
                i++;
            }
        }
    }

    public boolean isTheEnemyFar(float x, float z) {
        for (EnemyBot elem : listEnemy) {
            if (elem != null) {
                if (elem.getX() >= x) {
                    if (elem.getX() - x <= 30) {
                        if (elem.getZ() >= z) {
                            if (elem.getZ() - z <= 30) return false;
                        } else {
                            if (z - elem.getZ() <= 30) return false;
                        }
                    }
                } else {
                    if (x - elem.getX() <= 30) {
                        if (elem.getZ() >= z) {
                            if (elem.getZ() - z <= 30) return false;
                        } else {
                            if (z - elem.getZ() <= 30) return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private List<Item> createListItem(ChestType type) {
        List<Item> listItems = new ArrayList<>();
        int countItems = rnd(1, 3);

        for (int i = 0; i < countItems; i++) {
            int rand = rnd(0, 3);
            if (rand == 0)
                listItems.add(new Item("wolf", rnd(1, 20), rnd(1, 20)));
            else if (rand == 1)
                listItems.add(new Item("snake", rnd(1, 20), rnd(1, 20)));
            else if (rand == 2)
                listItems.add(new Item("parrot", rnd(1, 20), rnd(1, 20)));
            else if (rand == 3)
                listItems.add(new Item("squirrel", rnd(1, 20), rnd(1, 20)));
        }
        return listItems;
    }

    public boolean isTheChestFar(float x, float z) {
        for (Chest elem : listChests) {
            if (elem.getX() >= x) {
                if (elem.getX() - x <= 30) {
                    if (elem.getZ() >= z) {
                        if (elem.getZ() - z <= 30) return false;
                    } else {
                        if (z - elem.getZ() <= 30) return false;
                    }
                }
            } else {
                if (x - elem.getX() <= 30) {
                    if (elem.getZ() >= z) {
                        if (elem.getZ() - z <= 30) return false;
                    } else {
                        if (z - elem.getZ() <= 30) return false;
                    }
                }
            }
        }
        return true;
    }

    public Chest isTheChestFar1(float x, float z) {
        for (Chest elem : listChests) {
            if (elem.getX() >= x) {
                if (elem.getX() - x <= 30) {
                    if (elem.getZ() >= z) {
                        if (elem.getZ() - z <= 30) return elem;
                    } else {
                        if (z - elem.getZ() <= 30) return elem;
                    }
                }
            } else {
                if (x - elem.getX() <= 30) {
                    if (elem.getZ() >= z) {
                        if (elem.getZ() - z <= 30) return elem;
                    } else {
                        if (z - elem.getZ() <= 30) return elem;
                    }
                }
            }
        }
        return null;
    }

    public EnemyBot isTheEnemyFar1(float x, float z) {
        for (EnemyBot elem : listEnemy) {
            if (elem != null) {
                if (elem.getX() >= x) {
                    if (elem.getX() - x <= 30) {
                        if (elem.getZ() >= z) {
                            if (elem.getZ() - z <= 30) return elem;
                        } else {
                            if (z - elem.getZ() <= 30) return elem;
                        }
                    }
                } else {
                    if (x - elem.getX() <= 30) {
                        if (elem.getZ() >= z) {
                            if (elem.getZ() - z <= 30) return elem;
                        } else {
                            if (z - elem.getZ() <= 30) return elem;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void sendInfo(MessageType type, String message) {
        try {
            gameServer.sendMessage(firstGamer.getConnection(), new Message(type, message));
            //gameServer.sendMessage(secondGamer.getConnection(), new Message(type, message));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void setInfo() {
        for (Chest chest : listChests) {
            try {
                gameServer.sendMessage(firstGamer.getConnection(), new Message(MessageType.SET_CHEST, parseChests(chest)));
                //gameServer.sendMessage(secondGamer.getConnection(), new Message(MessageType.SET_CHEST, parseChests(chest)));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        for (EnemyBot enemy : listEnemy) {
            try {
                gameServer.sendMessage(firstGamer.getConnection(), new Message(MessageType.SET_ENEMY, parseEnemy(enemy)));
                //gameServer.sendMessage(secondGamer.getConnection(), new Message(MessageType.SET_ENEMY, parseEnemy(enemy)));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        try {
            gameServer.sendMessage(firstGamer.getConnection(), new Message(MessageType.SET_END, null));
            //gameServer.sendMessage(secondGamer.getConnection(), new Message(MessageType.SET_END, null));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        firstGamer.setListChest(listChests);
        firstGamer.setListEnemy(listEnemy);

//        secondGamer.setListChest(listChests);
//        secondGamer.setListEnemy(listEnemy);
    }

    public static int rnd(int min, int max) {
        max -= min;
        return (int) (Math.random() * ++max) + min;
    }
}