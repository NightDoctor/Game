package MenuAndChat;

import MenuAndChat.Message.Message;
import MenuAndChat.Message.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Connection implements Closeable, Serializable {
    private final Scanner scanner;
    private final PrintWriter pr;
    private static Socket client;

    public Connection(Socket client) throws IOException {
        scanner = new Scanner(client.getInputStream());
        pr = new PrintWriter(client.getOutputStream(), true);
        this.client = client;
    }

    public void send(Message message) {
        synchronized (pr) {
            pr.println(message.getXml());
        }
    }

    public Message receive() throws IOException {
        Message message;

        synchronized (scanner) {
            String s;
            try {
                s = scanner.nextLine();
                if (s != null) {
                    message = Converter.xmlToMessage(s);
                    return message;
                } else {
                    return null;
                }
            } catch (NoSuchElementException | IllegalStateException e) {
                return null;
            }
        }
    }

    public void close() {
        scanner.close();
        pr.close();
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}