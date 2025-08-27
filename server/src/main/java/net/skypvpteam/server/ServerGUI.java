package net.skypvpteam.server;

import javax.swing.*;
import java.util.Map;
import java.awt.*;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private JTextField cardKeyField;
    private JTextField usernameField;
    private JTextField passwordField;
    private JButton addCardKeyButton;
    private JButton addAccountButton;
    private JButton refreshButton;
    private JButton deleteAccountButton;
    private JButton deleteCardKeyButton;
    private JList<String> accountList;
    private JList<String> cardKeyList;
    private JComboBox<String> accountGroupCombo;
    private JComboBox<String> cardKeyGroupCombo;

    private DefaultListModel<String> accountModel;
    private DefaultListModel<String> cardKeyModel;

    private ServerCore server;


    public ServerGUI(ServerCore server) {
        this.server = server;
        setTitle("SkyPVP 服务器管理");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 初始化UI组件
        initComponents();

        // 更新UI状态
        updateUIState();

        // 加载初始数据
        refreshData();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);

        // 账号管理面板
        JPanel accountPanel = new JPanel(new BorderLayout());
        accountPanel.setBorder(BorderFactory.createTitledBorder("账号管理"));
        accountModel = new DefaultListModel<>();
        accountList = new JList<>(accountModel);
        
        // 用户组选择
        accountGroupCombo = new JComboBox<>(new String[]{"user", "author", "admin", "original_fans"});
        cardKeyGroupCombo = new JComboBox<>(new String[]{"user", "author", "admin", "original_fans"});

        // 添加账号控件
        JPanel accountControlPanel = new JPanel(new BorderLayout());
        JPanel addAccountPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        addAccountPanel.add(new JLabel("用户名:"));
        usernameField = new JTextField(20);
        addAccountPanel.add(usernameField);
        
        addAccountPanel.add(new JLabel("密码:"));
        passwordField = new JTextField(20);
        addAccountPanel.add(passwordField);
        
        addAccountPanel.add(new JLabel("用户组:"));
        addAccountPanel.add(accountGroupCombo);
        
        JPanel accountButtonPanel = new JPanel();
        addAccountButton = new JButton("添加账号");
        addAccountButton.addActionListener(e -> addAccount());
        deleteAccountButton = new JButton("删除选中账号");
        deleteAccountButton.addActionListener(e -> deleteAccount());
        accountButtonPanel.add(addAccountButton);
        accountButtonPanel.add(deleteAccountButton);

        accountControlPanel.add(addAccountPanel, BorderLayout.NORTH);
        accountControlPanel.add(accountButtonPanel, BorderLayout.SOUTH);
        
        accountPanel.add(new JScrollPane(accountList), BorderLayout.CENTER);
        accountPanel.add(accountControlPanel, BorderLayout.SOUTH);

        // 卡密管理面板
        JPanel cardKeyPanel = new JPanel(new BorderLayout());
        cardKeyPanel.setBorder(BorderFactory.createTitledBorder("卡密管理"));
        cardKeyModel = new DefaultListModel<>();
        cardKeyList = new JList<>(cardKeyModel);

        // 用户组选择已在前面定义

        // 添加卡密控件
        JPanel cardKeyControlPanel = new JPanel(new BorderLayout());
        JPanel addCardPanel = new JPanel();
        cardKeyField = new JTextField(20);
        addCardKeyButton = new JButton("添加卡密");
        addCardKeyButton.addActionListener(e -> addCardKey());
        addCardPanel.add(cardKeyField);
        addCardPanel.add(cardKeyGroupCombo);
        addCardPanel.add(addCardKeyButton);

        JPanel cardKeyButtonPanel = new JPanel();
        deleteCardKeyButton = new JButton("删除选中卡密");
        deleteCardKeyButton.addActionListener(e -> deleteCardKey());
        cardKeyButtonPanel.add(deleteCardKeyButton);

        cardKeyControlPanel.add(addCardPanel, BorderLayout.NORTH);
        cardKeyControlPanel.add(cardKeyButtonPanel, BorderLayout.SOUTH);

        cardKeyPanel.add(new JScrollPane(cardKeyList), BorderLayout.CENTER);
        cardKeyPanel.add(cardKeyControlPanel, BorderLayout.SOUTH);

        // 操作按钮
        JPanel buttonPanel = new JPanel();
        refreshButton = new JButton("刷新数据");
        refreshButton.addActionListener(e -> refreshData());
        buttonPanel.add(refreshButton);

        // 布局组合
        JPanel dataPanel = new JPanel(new GridLayout(1, 2));
        dataPanel.add(accountPanel);
        dataPanel.add(cardKeyPanel);

        mainPanel.add(logScroll, BorderLayout.CENTER);
        mainPanel.add(dataPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }



    private void updateUIState() {
        refreshButton.setEnabled(true);
        addCardKeyButton.setEnabled(true);
        cardKeyField.setEnabled(true);
    }

    private void refreshData() {
        accountModel.clear();
        cardKeyModel.clear();

        // 获取账号列表
        synchronized (server.getAccounts()) {
            for (String account : server.getAccounts()) {
                accountModel.addElement(account);
            }
        }

        // 获取卡密列表
        synchronized (server.getCardKeyGroups()) {
            for (Map.Entry<String, String> entry : server.getCardKeyGroups().entrySet()) {
                String status = entry.getValue() != null && !entry.getValue().isEmpty() ?
                        "组别: " + entry.getValue() : "未分组";
                cardKeyModel.addElement(entry.getKey() + " - " + status);
            }
        }

        log("数据刷新成功");
    }

    private void addAccount() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String userGroup = (String) accountGroupCombo.getSelectedItem();
        
        if (username.isEmpty()) {
            log("用户名不能为空");
            return;
        }
        
        if (password.isEmpty()) {
            log("密码不能为空");
            return;
        }

        boolean success = server.addAccount(username, password, userGroup);
        if (success) {
            log("账号添加成功: " + username + " (" + userGroup + ")");
            usernameField.setText("");
            passwordField.setText("");
            refreshData();
        } else {
            log("账号添加失败: 用户名已存在");
        }
    }

    private void addCardKey() {
        String key = cardKeyField.getText().trim();
        String userGroup = (String) cardKeyGroupCombo.getSelectedItem();
        if (key.isEmpty()) {
            log("卡密不能为空");
            return;
        }

        boolean success = server.addCardKey(key + ":" + userGroup);
        if (success) {
            log("添加卡密成功: " + key + " (" + userGroup + ")");
            cardKeyField.setText("");
            refreshData();
        } else {
            log("添加卡密失败: 卡密已存在");
        }
    }

    private void deleteAccount() {
        int selectedIndex = accountList.getSelectedIndex();
        if (selectedIndex == -1) {
            log("请先选择要删除的账号");
            return;
        }

        String selectedAccount = accountModel.getElementAt(selectedIndex);
        String username = selectedAccount.split(":")[0];

        boolean success = server.removeAccount(username);
        if (success) {
            log("账号删除成功: " + username);
            refreshData();
        } else {
            log("账号删除失败: 未找到账号");
        }
    }

    private void deleteCardKey() {
        int selectedIndex = cardKeyList.getSelectedIndex();
        if (selectedIndex == -1) {
            log("请先选择要删除的卡密");
            return;
        }

        String selectedCardKey = cardKeyModel.getElementAt(selectedIndex);
        // 从显示格式中提取卡密，格式为"卡密 - 状态"
        int separatorIndex = selectedCardKey.indexOf(" - ");
        if (separatorIndex == -1) {
            log("卡密格式错误");
            return;
        }

        String key = selectedCardKey.substring(0, separatorIndex);

        boolean success = server.removeCardKey(key);
        if (success) {
            log("卡密删除成功: " + key);
            refreshData();
        } else {
            log("卡密删除失败: 未找到卡密");
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerCore server = new ServerCore();
            ServerGUI gui = new ServerGUI(server);
            gui.setVisible(true);
        });
    }
}
