package utils;

public class session {
    private static int currentUserId = -1;

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    public static boolean isLoggedIn() {
        return currentUserId != -1;
    }

    public static void clear() {
        currentUserId = -1;
    }
}
