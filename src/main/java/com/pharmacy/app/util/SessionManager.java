package com.pharmacy.app.util;

import com.pharmacy.app.model.User;

/**
 * Holds the currently logged-in user for the duration of the app session.
 * Simple static holder since this is a single-user desktop application.
 */
public class SessionManager {

    private static User currentUser;

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}