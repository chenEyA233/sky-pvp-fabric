package cn.cheneya.skypvp.utils

import java.awt.AWTException
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

object WindowsUtil {
    fun info(text: String?) {
        if (SystemTray.isSupported()) {
            val tray = SystemTray.getSystemTray()
            val image = Toolkit.getDefaultToolkit().createImage("")

            val trayIcon = TrayIcon(image, "Java Tray")
            trayIcon.setImageAutoSize(true)

            try {
                tray.add(trayIcon)

                trayIcon.displayMessage("sky pvp", text, TrayIcon.MessageType.INFO)
            } catch (ignored: AWTException) {
            }
        } else {
            println("Success -> " + text)
        }
    }

    fun success(text: String?) {
        info(text)
    }

    fun warn(text: String?) {
        if (SystemTray.isSupported()) {
            val tray = SystemTray.getSystemTray()
            val image = Toolkit.getDefaultToolkit().createImage("")

            val trayIcon = TrayIcon(image, "Java Tray")
            trayIcon.setImageAutoSize(true)

            try {
                tray.add(trayIcon)

                trayIcon.displayMessage("sky pvp", text, TrayIcon.MessageType.WARNING)
            } catch (ignored: AWTException) {
            }
        } else {
            println("Success -> " + text)
        }
    }

    fun error(text: String?) {
        if (SystemTray.isSupported()) {
            val tray = SystemTray.getSystemTray()
            val image = Toolkit.getDefaultToolkit().createImage("")

            val trayIcon = TrayIcon(image, "Java Tray")
            trayIcon.setImageAutoSize(true)

            try {
                tray.add(trayIcon)

                trayIcon.displayMessage("sky pvp", text, TrayIcon.MessageType.ERROR)
            } catch (ignored: AWTException) {
            }
        } else {
            println("Success -> " + text)
        }
    }

    fun clearTheConsole() {
        try {
            ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor()
        } catch (ignored: Throwable) {
        }
    }
}