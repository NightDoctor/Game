package Server;

import Server.Database.DatabaseHelper;
import Server.Game.Server.GameServer;
import Server.MainServer.MainServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        DatabaseHelper.connectDatabase();

        Server mainServer = new MainServer();
        Thread threadStartMainServer = new Thread(() -> {
            try {
                mainServer.start(3000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        threadStartMainServer.start();

        GameServer gameServer = new GameServer();
        Thread threadStartGameServer = new Thread(() -> {
            try {
                gameServer.start(3001);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        threadStartGameServer.start();

        Thread threadCreateAPairOfPlayers = new Thread(() -> gameServer.createAPairOfPlayers());
        threadCreateAPairOfPlayers.start();
    }
}
