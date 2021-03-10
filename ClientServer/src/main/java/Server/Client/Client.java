package Server.Client;

import Server.Connection;
import Server.Game.Chest.Chest;
import Server.Game.Enemy.EnemyBot;
import Server.Game.Item.Item;

import java.util.ArrayList;
import java.util.List;

public class Client {
    private int id;
    private String nickname;
    private Connection connection;
    private boolean status;
    private String password;
    private String email;
    private int gold;
    private int health;
    private boolean isReady;

    private List<Chest> listChest;
    private List<EnemyBot> listEnemy;
    private List<Item> listItem;

    private List<Item> listDeck;
    private List<Item> listHand;
    private List<Item> listTable;

    private float x;
    private float z;

    public Client() {
    }

    public Client(Connection connection) {
        this.connection = connection;
        status = true;
        listChest = new ArrayList<>();
        listEnemy = new ArrayList<>();
        listItem = new ArrayList<>();

        listDeck = new ArrayList<>();
        listHand = new ArrayList<>();
        listTable = new ArrayList<>();
    }

    public Client(String nickname, String password, String email, int gold, boolean status) {
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.gold = gold;
        this.status = status;
        this.health = 100;
        isReady = false;
    }

    public Client(int id, String nickname, String password, String email, int gold, boolean status) {
        this.id = id;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.gold = gold;
        this.status = status;
        this.health = 100;
        isReady = false;
    }

    public int getId() {
        return id;
    }

    public boolean isStatus() {
        return status;
    }

    public String getNickname() {
        return nickname;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) { this.connection = connection; }

    public boolean isOnline() {
        return status;
    }

    public void setOnline(boolean online) {
        this.status = online;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public int getGold() {
        return gold;
    }

    public List<Item> getListItem() {
        return listItem;
    }

    public void addListItem(Item item) {
        this.listItem.add(item);
    }

    public List<Chest> getListChest() {
        return listChest;
    }

    public void setListChest(List<Chest> listChest) {
        this.listChest = listChest;
    }

    public List<EnemyBot> getListEnemy() {
        return listEnemy;
    }

    public void setListEnemy(List<EnemyBot> listEnemy) {
        this.listEnemy = listEnemy;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public void setListItem(List<Item> listItem) {
        this.listItem = listItem;
    }

    public List<Item> getListDeck() {
        return listDeck;
    }

    public void setListDeck(List<Item> listDeck) {
        this.listDeck = listDeck;
    }

    public List<Item> getListHand() {
        return listHand;
    }

    public void setListHand(List<Item> listHand) {
        this.listHand = listHand;
    }

    public List<Item> getListTable() {
        return listTable;
    }

    public void setListTable(List<Item> listTable) {
        this.listTable = listTable;
    }

    public void addListDeck(Item item) {
        this.listDeck.add(item);
    }

    public void addListHand(Item item) {
        this.listHand.add(item);
    }

    public void addListTable(Item item) {
        this.listTable.add(item);
    }

    public void close() {
        connection.close();
    }
}
