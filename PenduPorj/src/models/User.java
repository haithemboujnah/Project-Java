package models;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private static final int INITIAL_COINS = 100;
    private static final int WIN_COIN_REWARD = 50;
    private static final int LOSS_COIN_PENALTY = 10;
    private static final int LEVEL_UP_COIN_BONUS = 20;

    private String username;
    private String password;
    private int wins;
    private int losses;
    private int coins;
    private int level;
    private int xp;
    private int xpToNextLevel;
    private String avatarId;
    private int highestLevelReached;
    private int gamesPlayed;
    private List<String> purchasedItems = new ArrayList<>();
    private String currentKeyboardStyle;
    private String currentWallpaper;

    public User(String username, String password) {
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.password = Objects.requireNonNull(password, "Password cannot be null");
        this.wins = 0;
        this.losses = 0;
        this.coins = INITIAL_COINS;
        this.level = 1;
        this.xp = 0;
        this.xpToNextLevel = calculateXpToNextLevel();
        this.avatarId = "profile-icon";
        this.highestLevelReached = 1;
        this.gamesPlayed = 0;
        this.currentWallpaper = "/resources/styles/default-wallpaper.css";
    }

    // In User.java
    public void resetAccount() {
        this.wins = 0;
        this.losses = 0;
        this.coins = INITIAL_COINS;
        this.level = 1;
        this.xp = 0;
        this.xpToNextLevel = calculateXpToNextLevel();
        this.gamesPlayed = 0;
        // same avatar
    }

    public User(Document doc) {
        this(
                doc.getString("username"),
                doc.getString("password")
        );
        this.wins = doc.getInteger("wins", 0);
        this.losses = doc.getInteger("losses", 0);
        this.coins = doc.getInteger("coins", INITIAL_COINS);
        this.level = doc.getInteger("level", 1);
        this.xp = doc.getInteger("xp", 0);
        this.xpToNextLevel = calculateXpToNextLevel();
        this.avatarId = doc.getString("avatarId");
        this.highestLevelReached = doc.getInteger("highestLevelReached", 1);
        this.gamesPlayed = doc.getInteger("gamesPlayed", 0);
        if (doc.containsKey("purchasedItems")) {
            this.purchasedItems = doc.getList("purchasedItems", String.class);
        }
        this.currentKeyboardStyle = doc.getString("currentKeyboardStyle");
        this.currentWallpaper = doc.getString("currentBackground");
    }

    public String getCurrentKeyboardStyle() {
        return currentKeyboardStyle;
    }

    public void setCurrentKeyboardStyle(String style) {
        this.currentKeyboardStyle = style;
    }

    private int calculateXpToNextLevel() {
        return (level * 100) + 50;
    }

    public void changeAvatar(String newAvatarId) {
        this.avatarId = newAvatarId; // Doit être une simple assignation
        System.out.println("Avatar changé localement pour: " + newAvatarId);
    }

    public void addWin() {
        wins++;
        gamesPlayed++;
        coins += WIN_COIN_REWARD;
        addXp(calculateWinXp());
    }

    public void addLoss() {
        losses++;
        gamesPlayed++;
        coins = Math.max(0, coins - LOSS_COIN_PENALTY);
        addXp(calculateLossXp());
    }

    private int calculateWinXp() {
        return (level * 30); // More XP for wins at higher levels
    }

    private int calculateLossXp() {
        return 2; // Small XP for losses to encourage continued play
    }

    public void addXp(int amount) {
        if (amount <= 0) return;

        xp += amount;
        checkForLevelUp();
    }

    private void checkForLevelUp() {
        while (xp >= xpToNextLevel && xpToNextLevel > 0) {
            levelUp();
        }
    }

    private void levelUp() {
        int remainingXp = xp - xpToNextLevel;
        level++;
        xp = remainingXp;
        xpToNextLevel = calculateXpToNextLevel();
        coins += level * LEVEL_UP_COIN_BONUS;
        highestLevelReached = Math.max(highestLevelReached, level);
    }

    public void addCoins(int amount) {
        if (amount < 0 && coins < Math.abs(amount)) {
            coins = 0;
        } else {
            coins += amount;
        }
    }

    public double getWinRate() {
        return gamesPlayed > 0 ? (double) wins / gamesPlayed * 100 : 0;
    }

    public Document toDocument() {
        return new Document()
                .append("username", username)
                .append("password", password)
                .append("wins", wins)
                .append("losses", losses)
                .append("coins", coins)
                .append("level", level)
                .append("xp", xp)
                .append("xpToNextLevel", xpToNextLevel)
                .append("avatarId", avatarId)
                .append("highestLevelReached", highestLevelReached)
                .append("gamesPlayed", gamesPlayed)
                .append("purchasedItems", purchasedItems)
                .append("currentKeyboardStyle", currentKeyboardStyle)
                .append("currentBackground", currentWallpaper);

    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getCoins() { return coins; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getXpToNextLevel() { return xpToNextLevel; }
    public String getAvatarId() { return avatarId; }
    public int getHighestLevelReached() { return highestLevelReached; }
    public int getGamesPlayed() { return gamesPlayed; }
    public double getWinPercentage() { return getWinRate(); }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
    }

    public void updateUsername(String newUsername) {
        this.username = Objects.requireNonNull(newUsername, "Username cannot be null");
    }

    public void updatePassword(String newPassword) {
        this.password = Objects.requireNonNull(newPassword, "Password cannot be null");
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    public boolean hasPurchased(String itemId) {
        return purchasedItems.contains(itemId);
    }

    public void addPurchasedItem(String itemId) {
        if (!purchasedItems.contains(itemId)) {
            purchasedItems.add(itemId);
        }
    }

    public String getCurrentWallpaper() {
        return currentWallpaper;
    }

    public void setCurrentWallpaper(String wallpaperPath) {
        this.currentWallpaper = wallpaperPath;
    }

    public List<String> getPurchasedItems() {
        return new ArrayList<>(purchasedItems);
    }
}