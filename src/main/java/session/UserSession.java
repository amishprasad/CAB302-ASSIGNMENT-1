package com.kineticfitness.session;

import com.kineticfitness.model.User;

/** Holds the currently active user so every page can load/save that user's data. */
public final class UserSession {
    private static User currentUser;
    private UserSession() {}

    public static void setCurrentUser(User user) { currentUser = user; }
    public static User getCurrentUser() { return currentUser; }
    public static boolean isLoggedIn() { return currentUser != null; }
    public static void clear() { currentUser = null; }
}