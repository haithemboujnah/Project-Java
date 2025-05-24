package Services;

import controllers.HangmanController;
import models.Difficulty;
import models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Difficulty currentDifficulty;
    private HangmanController hangmanController;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }


    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void setCurrentDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
    }

    public Difficulty getCurrentDifficulty() {
        return currentDifficulty != null ? currentDifficulty : Difficulty.MEDIUM;
    }

    public void setHangmanController(HangmanController controller) {
        this.hangmanController = controller;
    }

    public HangmanController getHangmanController() {
        return hangmanController;
    }
}
