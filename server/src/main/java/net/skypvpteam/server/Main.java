package net.skypvpteam.server;

public class Main {
    public static void main(String[] args) {
        // 启动服务器核心
        ServerCore server = new ServerCore();
        new Thread(server::start).start();

        // 启动GUI界面
        ServerGUI gui = new ServerGUI(server);
        javax.swing.SwingUtilities.invokeLater(() -> gui.setVisible(true));
    }
}
