package models;

import org.bson.Document;

public class User {
    private String username;
    private String password;
    private int wins;
    private int losses;
    private int coins;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.wins = 0;
        this.losses = 0;
        this.coins = 100; // Starting coins
    }

    public User(Document doc) {
        this.username = doc.getString("username");
        this.password = doc.getString("password");
        this.wins = doc.getInteger("wins", 0);
        this.losses = doc.getInteger("losses", 0);
        this.coins = doc.getInteger("coins", 100);
    }

    // Getters and setters
    public Document toDocument() {
        return new Document()
                .append("username", username)
                .append("password", password)
                .append("wins", wins)
                .append("losses", losses)
                .append("coins", coins);
    }

    // Add methods for incrementing stats
    public void addWin() {
        wins++;
        coins += 50; // Reward for winning
    }

    public void addLoss() {
        losses++;
        coins += 10; // Small consolation for losing
    }

    public void addLoss2() {
        losses++;
        if (coins < 10) {
            coins = 0;
        } else {
            coins -= 10;
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getCoins() {
        return coins;
    }
}