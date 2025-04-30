import Services.MongoDBConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Initialize MongoDB connection
        try {
            MongoDBConnection.connect();
            System.out.println("Successfully connected to MongoDB!");

            // Load splash screen
            Parent root = FXMLLoader.load(getClass().getResource("/views/SplashScreen.fxml"));
            Scene scene = new Scene(root);

            primaryStage.setTitle("Le Pendu");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("MongoDB connection failed: " + e.getMessage());
            // Show error to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Cannot connect to database");
            alert.setContentText("Please make sure MongoDB is running on localhost:27017");
            alert.showAndWait();
            Platform.exit();
        }

    }

    @Override
    public void stop() {
        // Close MongoDB connection when application exits
        MongoDBConnection.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}