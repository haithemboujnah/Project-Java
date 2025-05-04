package controllers;

import Services.UserService;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.User;
import Services.SessionManager;

import java.io.IOException;
import java.util.Optional;

public class ProfileController {
    @FXML private Circle profileCircle;
    @FXML private Label usernameLabel;
    @FXML private Label gamesPlayedLabel;
    @FXML private Label winRateLabel;
    @FXML private Label winsLabel;
    @FXML private Label lossesLabel;
    @FXML private Label coinsLabel;
    @FXML private Label levelLabel;
    @FXML private ProgressBar levelProgress;
    @FXML private Label xpLabel;
    @FXML private VBox achievementsContainer;
    @FXML private StackPane avatarContainer;

    private Stage hangmanStage;
    private Stage stage;
    private Scene previousScene;
    private HangmanController hangmanController;

    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            usernameLabel.setText(user.getUsername().toUpperCase());
            createAvatar(user);  // Pass the user object now
            setUserStats(user);
            animateProgressBar(user);
            loadAchievements(user);
        }
    }

    public void setPreviousScene(Scene previousScene) {
        this.previousScene = previousScene;
    }

    public void setHangmanController(HangmanController controller) {
        this.hangmanController = controller;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleBack() {
        if (previousScene != null && stage != null) {
            // Resume game timers
            if (hangmanController != null) {
                hangmanController.resumeGame();
            }

            // Apply transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(previousScene);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), previousScene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        }
    }

    public void setHangmanStage(Stage hangmanStage) {
        this.hangmanStage = hangmanStage;
    }

    private void createAvatar(String username) {
        // Create avatar with user initials
        String initials = username.substring(0, Math.min(2, username.length())).toUpperCase();

        // Create background circle (or use the existing profileCircle)
        Circle background = new Circle(50);
        background.setFill(Color.web("#4b6cb7"));
        background.setStroke(Color.WHITE);
        background.setStrokeWidth(3);

        // Add initials text
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: white;");

        // Clear and add to container
        avatarContainer.getChildren().clear();
        avatarContainer.getChildren().addAll(background, initialsLabel);

        // Center the contents
        StackPane.setAlignment(background, Pos.CENTER);
        StackPane.setAlignment(initialsLabel, Pos.CENTER);
    }

    private void setUserStats(User user) {
        int totalGames = user.getWins() + user.getLosses();
        gamesPlayedLabel.setText(String.format("%,d", totalGames));

        double winRate = totalGames > 0 ? (double) user.getWins() / totalGames * 100 : 0;
        winRateLabel.setText(String.format("%.1f%%", winRate));

        winsLabel.setText(String.format("%,d", user.getWins()));
        lossesLabel.setText(String.format("%,d", user.getLosses()));
        coinsLabel.setText(String.format("%,d", user.getCoins()));
        levelLabel.setText("NIVEAU " + user.getLevel());
    }

    private void animateProgressBar(User user) {
        double targetProgress = (double) user.getXp() / user.getXpToNextLevel();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(levelProgress.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(levelProgress.progressProperty(), targetProgress))
        );
        timeline.play();

        xpLabel.setText(String.format("%,d / %,d XP (%.1f%%)",
                user.getXp(), user.getXpToNextLevel(), targetProgress * 100));
    }

    private void loadAchievements(User user) {
        achievementsContainer.getChildren().clear();

        // Create achievement shapes using SVG paths
        addAchievement("Première victoire", "Gagnez votre première partie", user.getWins() > 0);
        addAchievement("Maître du pendu", "Gagnez 10 parties", user.getWins() >= 10);
        addAchievement("Collectionneur", "Gagnez 1000 pièces", user.getCoins() >= 1000);
    }

    private void addAchievement(String title, String description, boolean unlocked) {
        HBox achievementBox = new HBox(15);
        achievementBox.setAlignment(Pos.CENTER_LEFT);
        achievementBox.setStyle("-fx-background-color: " +
                (unlocked ? "rgba(255,215,0,0.1)" : "rgba(100,100,100,0.1)") + ";" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1;" +
                "-fx-border-color: " + (unlocked ? "gold" : "#555") + ";");

        // Create trophy shape
        SVGPath trophy = new SVGPath();
        trophy.setContent("M30,10 L70,10 L65,30 L75,40 L60,45 L50,80 L40,45 L25,40 L35,30 Z");
        trophy.setFill(unlocked ? Color.GOLD : Color.GRAY);
        trophy.setStroke(unlocked ? Color.DARKGOLDENROD : Color.DARKGRAY);
        trophy.setStrokeWidth(2);

        VBox textBox = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
                (unlocked ? "gold" : "#777") + "; -fx-font-size: 14px;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: " + (unlocked ? "#ddd" : "#666") + "; -fx-font-size: 12px;");
        descLabel.setWrapText(true);

        textBox.getChildren().addAll(titleLabel, descLabel);
        achievementBox.getChildren().addAll(trophy, textBox);
        achievementsContainer.getChildren().add(achievementBox);
    }

    @FXML
    private void handleResetAccount() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de réinitialisation");
        confirmation.setHeaderText(null);
        confirmation.setGraphic(null);

        // Custom dialog content
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        ImageView warningIcon = new ImageView(new Image(getClass().getResourceAsStream("../resources/Images/login-arrow.png")));
        warningIcon.setFitWidth(50);
        warningIcon.setFitHeight(50);

        Label warningLabel = new Label("Êtes-vous sûr de vouloir réinitialiser votre compte?\nToutes vos données seront perdues.");
        warningLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f; -fx-font-size: 14px;");
        warningLabel.setAlignment(Pos.CENTER);

        content.getChildren().addAll(warningIcon, warningLabel);
        confirmation.getDialogPane().setContent(content);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Reset user data
            User user = SessionManager.getInstance().getCurrentUser();
            user.resetAccount();

            // Update MongoDB
            UserService.updateUserStats(user);

            // Logout
            SessionManager.getInstance().logout();

            // Close all windows
            if (stage != null) stage.close();

            // Show login screen
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/LoginScreen.fxml"));
                Parent root = loader.load();

                Stage loginStage = new Stage();
                loginStage.setScene(new Scene(root));
                loginStage.show();

                // Add fade animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showLoginScreen() {
        try {
            // Close any existing stages first
            if (stage != null) stage.close();
            if (hangmanStage != null) hangmanStage.close();

            // Load and show login screen
            Parent root = FXMLLoader.load(getClass().getResource("/views/LoginScreen.fxml"));
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.show();

            // Add fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createAvatar(User user) {
        avatarContainer.getChildren().clear();

        // Try to load custom avatar if path exists
        if (user.getAvatarId() != null && !user.getAvatarId().isEmpty()) {
            try {
                ImageView avatarImage = new ImageView(new Image(
                        getClass().getResourceAsStream("../resources/Images/" + user.getAvatarId() + ".png")));
                avatarImage.setFitWidth(100);
                avatarImage.setFitHeight(100);
                avatarImage.setClip(new Circle(50, 50, 50));
                avatarContainer.getChildren().add(avatarImage);
                return;
            } catch (Exception e) {
                System.err.println("Could not load custom avatar, using default");
            }
        }

        // Fallback to initials avatar
        String initials = user.getUsername().substring(0, Math.min(2, user.getUsername().length())).toUpperCase();
        Circle background = new Circle(50, Color.web("#4b6cb7"));
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: white;");

        StackPane.setAlignment(background, Pos.CENTER);
        StackPane.setAlignment(initialsLabel, Pos.CENTER);
        avatarContainer.getChildren().addAll(background, initialsLabel);
    }

    @FXML
    private void handleClose() {
        // Use the same back navigation logic as handleBack()
        if (previousScene != null && stage != null) {
            // Resume game timers
            if (hangmanController != null) {
                hangmanController.resumeGame();
            }

            // Apply transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(previousScene);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), previousScene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        }
    }
}