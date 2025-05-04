package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Difficulty;
import Services.SessionManager;
import models.User;

import java.io.IOException;

public class DifficultySelectionController {
    @FXML private VBox difficultyContainer;

    private SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        setupDifficultyButtons();
    }

    private void setupDifficultyButtons() {
        difficultyContainer.getChildren().clear();

        for (Difficulty difficulty : Difficulty.values()) {
            Button button = new Button(difficulty.toString());
            button.getStyleClass().add("difficulty-button");

            // Add specific class based on difficulty
            switch(difficulty) {
                case EASY:
                    button.getStyleClass().add("easy-difficulty");
                    break;
                case MEDIUM:
                    button.getStyleClass().add("medium-difficulty");
                    break;
                case HARD:
                    button.getStyleClass().add("hard-difficulty");
                    break;
                case EXTRA_HARD:
                    button.getStyleClass().add("extra-hard-difficulty");
                    break;
            }

            button.setOnAction(e -> {
                sessionManager.setCurrentDifficulty(difficulty);
                navigateToThemeSelection();
            });
            difficultyContainer.getChildren().add(button);
        }
    }

    public void setCurrentUser(User user) {
        sessionManager.setCurrentUser(user);
    }

    private void navigateToThemeSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/theme-selection.fxml"));
            Parent root = loader.load();

            ThemeSelectionController controller = loader.getController();
            controller.setCurrentUser(sessionManager.getCurrentUser());

            Stage stage = (Stage) difficultyContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}