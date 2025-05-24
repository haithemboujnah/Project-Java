package Services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import models.StoreItem;
import models.User;
import org.bson.Document;
import com.mongodb.client.result.UpdateResult;

import java.util.ArrayList;
import java.util.List;

public class UserService {
    private static final String COLLECTION_NAME = "users";
    private static MongoCollection<Document> collection;
    private static MongoCollection<Document> getStoreItemsCollection() {
        return MongoDBConnection.getCollection("store_items");
    }

    static {
        collection = MongoDBConnection.getCollection(COLLECTION_NAME);
    }

    public static boolean registerUser(String username, String password) {
        if (usernameExists(username)) {
            return false;
        }

        User newUser = new User(username, password);
        collection.insertOne(newUser.toDocument());
        return true;
    }

    public static User loginUser(String username, String password) {
        Document userDoc = collection.find(Filters.and(
                Filters.eq("username", username),
                Filters.eq("password", password)
        )).first();

        return userDoc != null ? new User(userDoc) : null;
    }

    public static boolean usernameExists(String username) {
        return collection.find(Filters.eq("username", username)).first() != null;
    }

    public static boolean updateUser(User user) {
        try {
            Document updateDoc = new Document("$set", user.toDocument());
            UpdateResult result = collection.updateOne(
                    Filters.eq("username", user.getUsername()),
                    updateDoc
            );
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUsername(String currentUsername, String newUsername) {
        // Check if new username already exists
        if (usernameExists(newUsername)) {
            return false;
        }

        try {
            UpdateResult result = collection.updateOne(
                    Filters.eq("username", currentUsername),
                    new Document("$set", new Document("username", newUsername))
            );
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePassword(String username, String newPassword) {
        try {
            UpdateResult result = collection.updateOne(
                    Filters.eq("username", username),
                    new Document("$set", new Document("password", newPassword))
            );
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateUserStats(User user) {
        Document updateDoc = new Document("$set", new Document()
                .append("wins", user.getWins())
                .append("losses", user.getLosses())
                .append("coins", user.getCoins())
                .append("level", user.getLevel())
                .append("xp", user.getXp())
                .append("xpToNextLevel", user.getXpToNextLevel())
                .append("avatarId", user.getAvatarId())
                .append("highestLevelReached", user.getHighestLevelReached())
                .append("gamesPlayed", user.getGamesPlayed())
                .append("purchasedItems", user.getPurchasedItems())
                .append("currentKeyboardStyle", user.getCurrentKeyboardStyle())
                .append("currentWallpaper", user.getCurrentWallpaper())
        );

        collection.updateOne(
                Filters.eq("username", user.getUsername()),
                updateDoc
        );
    }

    public static List<StoreItem> getAllStoreItems() {
        List<StoreItem> items = new ArrayList<>();
        MongoCollection<Document> collection = getStoreItemsCollection();

        for (Document doc : collection.find()) {
            items.add(new StoreItem(
                    doc.getString("id"),
                    doc.getString("name"),
                    doc.getString("description"),
                    doc.getInteger("price"),
                    doc.getString("type"),
                    doc.getString("imagePath"),
                    doc.getString("resourcePath")
            ));
        }
        return items;
    }
}