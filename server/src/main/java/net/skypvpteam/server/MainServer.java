package net.skypvpteam.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainServer {
    public static final String USER_GROUP_USER = "user";
    public static final String USER_GROUP_AUTHOR = "author";
    public static final String USER_GROUP_ADMIN = "admin";
    public static final String USER_GROUP_ORIGINAL_FANS = "original_fans";
    
    private static final int PORT = 14520;
    private static final Map<String, String> accounts = new ConcurrentHashMap<>();
    private static final Map<String, String> accountGroups = new ConcurrentHashMap<>();
    private static final Map<String, AbstractMap.SimpleEntry<Boolean, String>> cardKeys = new ConcurrentHashMap<>();

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("服务器已启动，监听端口: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (DataInputStream input = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream())) {

            String command = input.readUTF();

            switch (command) {
                case "AUTH":
                    handleAuth(input, output);
                    break;
                case "REGISTER":
                    handleRegister(input, output);
                    break;
                case "GET_ACCOUNTS":
                    output.writeInt(accounts.size());
                    for (Map.Entry<String, String> entry : accounts.entrySet()) {
                        output.writeUTF(entry.getKey() + ":" + entry.getValue());
                    }
                    break;
                case "GET_CARD_KEYS":
                    output.writeInt(cardKeys.size());
                    for (Map.Entry<String, AbstractMap.SimpleEntry<Boolean, String>> entry : cardKeys.entrySet()) {
                        output.writeUTF(entry.getKey());
                        output.writeBoolean(entry.getValue().getKey());
                        output.writeUTF(entry.getValue().getValue());
                    }
                    break;
                case "ADD_CARD_KEY":
                    String newKey = input.readUTF();
                    if (!cardKeys.containsKey(newKey)) {
                        cardKeys.put(newKey, new AbstractMap.SimpleEntry<>(false, "default"));
                        output.writeBoolean(true);
                    } else {
                        output.writeBoolean(false);
                    }
                    break;
                default:
                    output.writeUTF("UNKNOWN_COMMAND");
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleAuth(DataInputStream input, DataOutputStream output) throws IOException {
        String username = input.readUTF();
        String password = input.readUTF();

        System.out.println("验证账号: " + username + " 输入密码: " + password + " 存储密码: " + accounts.get(username));
        if (accounts.containsKey(username) && accounts.get(username).equals(password)) {
            output.writeBoolean(true);
            output.writeUTF("SUCCESS");
            output.writeUTF(accountGroups.getOrDefault(username, USER_GROUP_USER));
        } else {
            output.writeBoolean(false);
            output.writeUTF("用户名或密码错误");
        }
    }

    private static void handleRegister(DataInputStream input, DataOutputStream output) throws IOException {
        String username = input.readUTF();
        String password = input.readUTF();
        String cardKey = input.readUTF();

        if (accounts.containsKey(username)) {
            output.writeUTF("FAILURE");
            output.writeUTF("USERNAME_EXISTS");
            return;
        }

        if (!cardKeys.containsKey(cardKey) || cardKeys.get(cardKey).getKey()) {
            output.writeUTF("FAILURE");
            output.writeUTF("INVALID_CARD_KEY");
            return;
        }

        // 注册成功
        String userGroup = cardKeys.get(cardKey).getValue();
        accounts.put(username, password);
        accountGroups.put(username, userGroup);
        cardKeys.put(cardKey, new AbstractMap.SimpleEntry<>(true, userGroup)); // 标记卡密为已使用并保留用户组信息
        output.writeUTF("SUCCESS");
        output.writeUTF(userGroup);
    }
}
