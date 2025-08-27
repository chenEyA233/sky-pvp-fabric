package net.skypvpteam.server;

import java.util.Date;
import java.io.*;
import java.net.*;
import java.util.*;

public class ServerCore {
    private ServerSocket serverSocket;
    private boolean running;
    private final List<String> accounts = new ArrayList<>();
    private final Map<String, String> cardKeyGroups = new HashMap<>(); // key:卡密, value:用户组
    private final Set<String> usedCardKeys = new HashSet<>();
    private final File configFile = new File("server_config.dat");
    
    public ServerCore() {
        // 初始化时不添加任何默认卡密
        loadConfig();
    }
    
    private void loadConfig() {
        if (configFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(configFile))) {
                accounts.clear();
                accounts.addAll((List<String>) ois.readObject());
                cardKeyGroups.clear();
                cardKeyGroups.putAll((Map<String, String>) ois.readObject());
            } catch (Exception e) {
                System.err.println("加载配置失败: " + e.getMessage());
            }
        }
    }
    
    public void saveConfig() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(configFile))) {
            oos.writeObject(accounts);
            oos.writeObject(cardKeyGroups);
        } catch (Exception e) {
            System.err.println("保存配置失败: " + e.getMessage());
        }
    }
    
    public List<String> getAccounts() {
        return accounts;
    }
    
    public Map<String, String> getCardKeyGroups() {
        return cardKeyGroups;
    }
    private int port = 14520;

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("服务器启动，监听端口: " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        String clientIP = clientSocket.getInetAddress().getHostAddress();
        System.out.println("[" + new Date() + "] 客户端连接: " + clientIP);
        
        try (DataInputStream input = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream())) {

            String command = input.readUTF();
            switch (command) {
                case "REGISTER":
                    String username = input.readUTF();
                    String password = input.readUTF();
                    
                    synchronized (accounts) {
                        boolean exists = false;
                        for (String account : accounts) {
                            if (account.startsWith(username + ":")) {
                                exists = true;
                                break;
                            }
                        }
                        
                        if (exists) {
                            output.writeBoolean(false);
                            output.writeUTF("用户名已存在");
                            System.out.println("[" + new Date() + "] 注册失败 - 用户名已存在: " + username);
                        } else {
                            // 使用默认用户组"user"
                            accounts.add(username + ":" + password + ":user");
                            saveConfig();
                            output.writeBoolean(true);
                            output.writeUTF("注册成功");
                            System.out.println("[" + new Date() + "] 新用户注册成功: " + username);
                        }
                    }
                    break;
                    
                case "LOGIN":
                case "AUTH":
                    String loginUser = input.readUTF();
                    String loginPass = input.readUTF();
                    
                    synchronized (accounts) {
                        System.out.println("开始验证账号: " + loginUser);
                        boolean valid = false;
                        String userGroup = "user";
                        for (String account : accounts) {
                            String[] parts = account.split(":");
                            System.out.println("检查账号: " + parts[0]);
                            if (parts[0].equals(loginUser) && parts[1].equals(loginPass)) {
                                valid = true;
                                userGroup = parts.length > 2 ? parts[2] : "user";
                                System.out.println("账号验证成功: " + loginUser + " 用户组: " + userGroup);
                                break;
                            }
                        }
                        if (!valid) {
                            System.out.println("账号验证失败: " + loginUser);
                        }
                        output.writeBoolean(valid);
                        output.writeUTF(valid ? "SUCCESS" : "用户名或密码错误");
                        if (valid) {
                            output.writeUTF(userGroup);
                        }
                        output.flush();
                        System.out.println("已发送验证结果给客户端");
                    }
                    break;

                case "GET_ACCOUNTS":
                    synchronized (accounts) {
                        output.writeInt(accounts.size());
                        for (String account : accounts) {
                            output.writeUTF(account);
                        }
                    }
                    break;

                case "GET_CARD_KEYS":
                    synchronized (cardKeyGroups) {
                        output.writeInt(cardKeyGroups.size());
                        for (Map.Entry<String, String> entry : cardKeyGroups.entrySet()) {
                            output.writeUTF(entry.getKey());
                            output.writeUTF(entry.getValue());
                        }
                    }
                    break;

                case "ADD_CARD_KEY":
                    String key = input.readUTF();
                    synchronized (cardKeyGroups) {
                        boolean exists = cardKeyGroups.containsKey(key);
                        output.writeBoolean(!exists);
                        if (!exists) {
                            cardKeyGroups.put(key, "default");
                        }
                    }
                    break;

                default:
                    output.writeBoolean(false);
                    output.writeUTF("未知命令");
            }
        } catch (IOException e) {
            System.err.println("客户端处理错误: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("关闭客户端连接失败: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("关闭服务器失败: " + e.getMessage());
        }
    }

    public boolean addCardKey(String keyWithGroup) {
        synchronized (cardKeyGroups) {
            String[] parts = keyWithGroup.split(":");
            String key = parts[0];
            String group = parts.length > 1 ? parts[1] : "default";
            
            if (cardKeyGroups.containsKey(key)) {
                return false;
            }
            cardKeyGroups.put(key, group);
            saveConfig();
            return true;
        }
    }
    
    public boolean addAccount(String username, String password, String group) {
        synchronized (accounts) {
            // 检查用户名是否已存在
            for (String account : accounts) {
                if (account.startsWith(username + ":")) {
                    return false; // 用户名已存在
                }
            }
            
            // 添加新账号
            accounts.add(username + ":" + password + ":" + group);
            saveConfig();
            System.out.println("[" + new Date() + "] 新账号已添加: " + username + " (组别: " + group + ")");
            return true;
        }
    }
    
    public boolean removeAccount(String username) {
        synchronized (accounts) {
            Iterator<String> iterator = accounts.iterator();
            while (iterator.hasNext()) {
                String account = iterator.next();
                if (account.startsWith(username + ":")) {
                    iterator.remove();
                    saveConfig();
                    System.out.println("[" + new Date() + "] 用户已删除: " + username);
                    return true;
                }
            }
            return false;
        }
    }
    
    public boolean removeCardKey(String key) {
        synchronized (cardKeyGroups) {
            if (cardKeyGroups.containsKey(key)) {
                cardKeyGroups.remove(key);
                saveConfig();
                System.out.println("[" + new Date() + "] 卡密已删除: " + key);
                return true;
            }
            return false;
        }
    }
}
