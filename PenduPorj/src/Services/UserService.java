package Services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import models.User;
import org.bson.Document;
import Services.MongoDBConnection;

public class UserService {
    private static final String COLLECTION_NAME = "users";
    private static MongoCollection<Document> collection;

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

    public static void updateUserStats(User user) {
        collection.updateOne(
                Filters.eq("username", user.getUsername()),
                new Document("$set", new Document()
                        .append("wins", user.getWins())
                        .append("losses", user.getLosses())
                        .append("coins", user.getCoins()))
        );
    }
}