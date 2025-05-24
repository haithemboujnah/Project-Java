package controllers;

import Services.UserService;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import models.User;
import Services.SessionManager;

import java.io.IOException;
import java.io.InputStream;
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
                (unlocked ? "gold" : "#777") + "; -fx-font-size: 14px; -fx-font-family: 'Comic Sans MS', cursive;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: " + (unlocked ? "#b1b1b1" : "#2e2e2e") + "; -fx-font-size: 12px; -fx-font-family: 'Comic Sans MS', cursive;");
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

        // Custom dialog content with improved styling
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f9f9f9;");

        ImageView warningIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/Images/hint.png")));
        warningIcon.setFitWidth(60);
        warningIcon.setFitHeight(60);

        Label warningLabel = new Label("Êtes-vous sûr de vouloir réinitialiser votre compte?\nToutes vos données seront perdues.");
        warningLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f; -fx-font-size: 14px; -fx-alignment: CENTER;");
        warningLabel.setTextAlignment(TextAlignment.CENTER);

        content.getChildren().addAll(warningIcon, warningLabel);
        confirmation.getDialogPane().setContent(content);

        // Apply custom styles
        confirmation.getDialogPane().getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());

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

        try {
            // Chemin vers l'avatar dans le dossier store
            String avatarPath = "/resources/Images/store/" + user.getAvatarId() + ".png";
            InputStream imageStream = getClass().getResourceAsStream(avatarPath);

            if (imageStream != null) {
                ImageView avatarImage = new ImageView(new Image(imageStream));
                avatarImage.setFitWidth(100);
                avatarImage.setFitHeight(100);

                // Créer un masque circulaire
                Circle clip = new Circle(50, 50, 50);
                avatarImage.setClip(clip);

                avatarContainer.getChildren().add(avatarImage);
            } else {
                // Fallback si l'avatar n'existe pas
                createInitialsAvatar(user);
            }
        } catch (Exception e) {
            System.err.println("Error loading avatar: " + e.getMessage());
            createInitialsAvatar(user);
        }
    }

    private void createInitialsAvatar(User user) {
        String initials = user.getUsername().substring(0, Math.min(2, user.getUsername().length())).toUpperCase();

        Circle background = new Circle(50, Color.web("#4b6cb7"));
        background.setStroke(Color.WHITE);
        background.setStrokeWidth(3);

        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: white;");

        avatarContainer.getChildren().addAll(background, initialsLabel);
        StackPane.setAlignment(background, Pos.CENTER);
        StackPane.setAlignment(initialsLabel, Pos.CENTER);
    }


    @FXML
    private void handleUpdateProfile() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        // Create a custom dialog with improved styling
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Modifier le profil");
        dialog.setHeaderText("Mise à jour du profil");

        // Set a modern, clean style class
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        // Add application icon
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/Images/user-icon.png")));

        // Set a modern, clean style class
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        // Set the button types
        ButtonType updateButtonType = new ButtonType("Mettre à jour", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Create a styled GridPane with modern spacing
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(25));
        grid.setStyle("-fx-background-color: transparent;");

        // Create modern input fields
        TextField usernameField = new TextField(user.getUsername());
        usernameField.getStyleClass().add("dialog-text-field");
        usernameField.setPromptText("Nouveau nom d'utilisateur");
        usernameField.setPrefWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("dialog-text-field");
        passwordField.setPromptText("Nouveau mot de passe");
        passwordField.setPrefWidth(250);

        // Create modern labels with icons
        HBox usernameBox = createFormField("Nom d'utilisateur", "user-icon.png");
        HBox passwordBox = createFormField("Mot de passe", "password-icon.png");

        grid.add(usernameBox, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passwordBox, 0, 1);
        grid.add(passwordField, 1, 1);

        // Add a modern graphic
        ImageView dialogIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/Images/login-icon.jpg")));
        dialogIcon.setFitWidth(80);
        dialogIcon.setFitHeight(80);
        dialog.setGraphic(dialogIcon);
        dialog.getDialogPane().setContent(grid);

        // Load custom stylesheet
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/resources/styles.css").toExternalForm()
        );

        // Request focus and select all text in username field
        Platform.runLater(() -> {
            usernameField.requestFocus();
            usernameField.selectAll();
        });

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });

        // Show and wait for response
        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            String newUsername = usernamePassword.getKey();
            String newPassword = usernamePassword.getValue();

            // Validate inputs
            if (newUsername.isEmpty()) {
                showAlert("Erreur", "Le nom d'utilisateur ne peut pas être vide");
                return;
            }

            boolean usernameChanged = !newUsername.equals(user.getUsername());
            boolean passwordChanged = !newPassword.isEmpty();

            if (!usernameChanged && !passwordChanged) {
                showAlert("Information", "Aucune modification détectée");
                return;
            }

            // Handle username change
            if (usernameChanged) {
                if (UserService.usernameExists(newUsername)) {
                    showAlert("Erreur", "Ce nom d'utilisateur est déjà pris");
                    return;
                }

                // Update username in database first
                if (!UserService.updateUsername(user.getUsername(), newUsername)) {
                    showAlert("Erreur", "Échec de la mise à jour du nom d'utilisateur");
                    return;
                }

                // Then update local user object
                user.updateUsername(newUsername);
            }

            // Handle password change
            if (passwordChanged) {
                if (!UserService.updatePassword(user.getUsername(), newPassword)) {
                    showAlert("Erreur", "Échec de la mise à jour du mot de passe");
                    return;
                }
                user.updatePassword(newPassword);
            }

            // Update session if username changed
            if (usernameChanged) {
                SessionManager.getInstance().setCurrentUser(user);
            }

            // Update UI
            usernameLabel.setText(user.getUsername().toUpperCase());
            createInitialsAvatar(user); // In case initials changed

            showAlert("Succès", "Profil mis à jour avec succès");
        });
    }

    private HBox createFormField(String labelText, String iconName) {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);

        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(
                    "/resources/Images/" + iconName)));
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            Label label = new Label(labelText);
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #147120; -fx-font-size: 14px; -fx-font-family: 'Comic Sans MS', cursive;");

            hbox.getChildren().addAll(icon, label);
        } catch (Exception e) {
            // Fallback if icon not found
            Label label = new Label(labelText);
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #147120; -fx-font-size: 14px; -fx-font-family: 'Comic Sans MS', cursive;");
            hbox.getChildren().add(label);
        }
        return hbox;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply custom style
        alert.getDialogPane().getStyleClass().add("alert");

        // Set stage icon
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/Images/user-icon.png")));

        // Create custom graphic based on alert type
        ImageView icon = new ImageView();
        if (title.equals("Erreur")) {
            icon.setImage(new Image(getClass().getResourceAsStream("/resources/Images/error-icon.png")));
            alert.getDialogPane().setStyle("-fx-border-color: #d32f2f;");
        } else if (title.equals("Succès")) {
            icon.setImage(new Image(getClass().getResourceAsStream("/resources/Images/success-icon.png")));
            alert.getDialogPane().setStyle("-fx-border-color: #2e7d32;");
        } else {
            icon.setImage(new Image(getClass().getResourceAsStream("/resources/Images/login-icon.png")));
        }

        icon.setFitWidth(48);
        icon.setFitHeight(48);
        alert.setGraphic(icon);

        // Load custom stylesheet
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/resources/styles.css").toExternalForm()
        );

        alert.showAndWait();
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