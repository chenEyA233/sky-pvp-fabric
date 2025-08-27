package cn.cheneya.skypvp.api.utils;

public class LoginStateManager {
    private static boolean isFirstLaunch = true;

    public static boolean isFirstLaunch() {
        return isFirstLaunch;
    }

    public static void setFirstLaunch(boolean state) {
        isFirstLaunch = state;
    }

    public static void resetFirstLaunch() {
        isFirstLaunch = true;
    }
}
