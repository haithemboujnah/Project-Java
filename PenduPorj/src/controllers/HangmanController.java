package controllers;

import Services.SessionManager;
import Services.UserService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import models.Difficulty;
import models.Game;
import models.User;
import models.WordDictionary;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class HangmanController {

    private Game game;
    private WordDictionary dictionary;
    private String currentTheme;
    private User currentUser;
    private SessionManager sessionManager = SessionManager.getInstance();
    private MediaPlayer timeWarningSoundPlayer;

    private Timeline countdownTimeline;
    private Timeline bonusTimeDisplayTimeline;
    private int timeRemaining = 90;
    @FXML private Label timerLabel;
    @FXML private Label bonusTimeLabel;

    @FXML private Label wordLabel;
    @FXML private Label attemptsLabel;
    @FXML private Label guessedLettersLabel;
    @FXML private GridPane keyboardGrid;
    @FXML private Canvas hangmanCanvas;
    @FXML private Label userInfoLabel;

    @FXML private Label difficultyLabel;
    @FXML private HBox heartsContainer;
    @FXML private Button hintButton;
    @FXML private Label levelLabel;
    @FXML private Label xpLabel;
    @FXML private ProgressBar xpProgressBar;
    @FXML private ImageView profileIcon;
    public ImageView storeIcon;
    private String currentKeyboardStyle;
    private String currentBackground;

    private List<ImageView> heartImages = new ArrayList<>();;

    private GraphicsContext gc;
    private final String[] keyboardRows = {
            "A Z E R T Y U I O P",
            "Q S D F G H J K L M",
            "W X C V B N"
    };

    private MediaPlayer winSoundPlayer;
    private MediaPlayer loseSoundPlayer;

    public void initialize() {
        gc = hangmanCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        // Initialiser l'avatar au démarrage
        if (sessionManager.isLoggedIn()) {
            updateProfileIcon();
            User user = sessionManager.getCurrentUser();

            if (user.getCurrentKeyboardStyle() != null && !user.getCurrentKeyboardStyle().isEmpty()) {
                applyKeyboardStyle(user.getCurrentKeyboardStyle());
            }

            // Add listener for scene changes
            keyboardGrid.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null && user.getCurrentWallpaper() != null) {
                    // Small delay to ensurescene is fully initialized
                    PauseTransition delay = new PauseTransition(Duration.millis(100));
                    delay.setOnFinished(e -> applyBackground(user.getCurrentWallpaper()));
                    delay.play();
                }
            });

            // Apply immediately ifscene is already available
            if (keyboardGrid.getScene() != null && user.getCurrentWallpaper() != null) {
                applyBackground(user.getCurrentWallpaper());
            }
        }

        SessionManager.getInstance().setHangmanController(this);

        // Initialize timer first
        setupTimer();
        updateTimerDisplay();
        bonusTimeLabel.setVisible(false);
        startGameWithTimer();
        initializeHearts();
        // Load sounds
        try {
            String winSoundPath = getClass().getResource("/resources/Sounds/win.mp3").toExternalForm();
            String loseSoundPath = getClass().getResource("/resources/Sounds/lose.mp3").toExternalForm();

            Media winSound = new Media(winSoundPath);
            Media loseSound = new Media(loseSoundPath);

            winSoundPlayer = new MediaPlayer(winSound);
            loseSoundPlayer = new MediaPlayer(loseSound);
        } catch (Exception e) {
            System.err.println("Error loading sounds: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            String timeWarningPath = getClass().getResource("/resources/Sounds/time-warning.mp3").toExternalForm();
            Media timeWarningSound = new Media(timeWarningPath);
            timeWarningSoundPlayer = new MediaPlayer(timeWarningSound);
        } catch (Exception e) {
            System.err.println("Error loading time warning sound: " + e.getMessage());
        }

        try {
            // Load dictionary from resources
            InputStream wordStream = getClass().getResourceAsStream("../resources/words.txt");
            if (wordStream == null) {
                throw new IOException("Could not find words.txt in resources");
            }
            dictionary = new WordDictionary(wordStream);
            startNewGame();
        } catch (IOException e) {
            showAlert("Error", "Could not load dictionary: " + e.getMessage(),false);
            e.printStackTrace();
        }

    }

    private void setupTimer() {
        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    if (timeRemaining > 0) {
                        timeRemaining--;
                        updateTimerDisplay();

                        // Check for time expiration after updating
                        if (timeRemaining <= 0) {
                            timeExpired();
                        }
                    }
                })
        );
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void resetTimer() {
        // Keep the current timeRemaining value that was set in updateDifficultyDisplay
        updateTimerDisplay();
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
    }

    private void stopTimeWarningSound() {
        if (timeWarningSoundPlayer != null) {
            timeWarningSoundPlayer.stop();
        }
    }

    private void updateTimerDisplay() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

        // Reset all timer styles
        timerLabel.getStyleClass().removeAll("warning", "critical");

        // Apply appropriate style and sounds
        if (timeRemaining <= 10) {
            timerLabel.getStyleClass().add("critical");
            if (timeWarningSoundPlayer != null && timeRemaining <= 10) {
                timeWarningSoundPlayer.play();

            }
        } else if (timeRemaining <= 30) {
            timerLabel.getStyleClass().add("warning");
        }
    }

    private void showBonusTime(int seconds) {
        bonusTimeLabel.setText("+" + seconds + "s");
        bonusTimeLabel.setVisible(true);

        if (bonusTimeDisplayTimeline != null) {
            bonusTimeDisplayTimeline.stop();
        }

        bonusTimeDisplayTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> {
                    bonusTimeLabel.setVisible(false);
                })
        );
        bonusTimeDisplayTimeline.play();
    }

    private void timeExpired() {
        // Stop the timeline first
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        // Update user stats if logged in
        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getCurrentUser();
            user.addLoss();
            UserService.updateUserStats(user);
            updateUserInfo();
        }

        // Play lose animation and show alert
        playLoseAnimation();

        // Use Platform.runLater to ensure UI updates happen on the JavaFX thread
        Platform.runLater(() -> {
            showAlert("Temps écoulé", "Le temps est écoulé!\n\nLe mot était: " + game.getWordToGuess(), true);
        });
    }


    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setTheme(String theme, WordDictionary dictionary) {
        this.currentTheme = theme;
        this.dictionary = dictionary;
        startNewGame(); // Start game immediately with the selected theme
    }


    private void initializeHearts() {
        heartsContainer.getChildren().clear();
        heartImages.clear();

        for (int i = 0; i < 3; i++) {
            ImageView heart = new ImageView(new Image(getClass().getResourceAsStream("../resources/Images/heart.png")));
            heart.setFitWidth(30);
            heart.setFitHeight(30);
            heart.getStyleClass().add("heart");

            heart.setOnMouseClicked(e -> handleHint());

            heartsContainer.getChildren().add(heart);
            heartImages.add(heart);
        }

        updateHintButton();
    }

    private void showHintAlert(Runnable onConfirm) {
        // Create confirmation alert
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation d'achat");
        confirmation.setHeaderText(null);
        confirmation.setGraphic(null);

        // Custom dialog styling
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("../resources/styles.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("hint-confirmation");

        // Create custom content
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #fff3e0;" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;" +
                "-fx-border-width: 2;" +
                "-fx-border-color: #ffb74d;");

        // Add icon
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("../resources/Images/coin.png")));
        icon.setFitWidth(50);
        icon.setFitHeight(50);

        // Add hint text
        Label hintLabel = new Label("Cet indice coûte 20 pièces\n\nVoulez-vous vraiment acheter cet indice?");
        hintLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e65100; -fx-font-weight: bold; -fx-wrap-text: true;");
        hintLabel.setMaxWidth(300);
        hintLabel.setAlignment(Pos.CENTER);

        content.getChildren().addAll(icon, hintLabel);
        dialogPane.setContent(content);

        // Set button types
        ButtonType confirmButton = new ButtonType("Acheter", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(confirmButton, cancelButton);

        // Show and wait for response
        confirmation.showAndWait().ifPresent(response -> {
            if (response == confirmButton) {
                onConfirm.run();
            }
        });
    }

    private void updateHintButton() {
        if (game != null) {
            if (game.getRemainingHints() > 0) {
                hintButton.setDisable(true);
                hintButton.setText("Indice (" + game.getRemainingHints() + ") - 20 pièces");
                hintButton.setStyle("-fx-background-color: #4CAF50; " + // Green background
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-radius: 5; " +
                        "-fx-border-color: #2E7D32; " +
                        "-fx-border-width: 2;");
            }
            } else {
                hintButton.setDisable(true);
                hintButton.setText("Pas d'indices disponibles");
                hintButton.setStyle("-fx-background-color: #9E9E9E; " + // Gray background
                        "-fx-text-fill: #616161; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-radius: 5; " +
                        "-fx-border-color: #757575; " +
                        "-fx-border-width: 2;");
        }
    }

    @FXML
    private void handleHint() {
        if (game == null || game.getRemainingHints() <= 0) {
            return;
        }

        if (!sessionManager.isLoggedIn()) {
            showAlert("Information", "Vous devez être connecté pour utiliser des indices", false);
            return;
        }

        User user = sessionManager.getCurrentUser();
        final int hintCost = 20; // Cost per hint in coins

        if (user.getCoins() < hintCost) {
            showAlert("paspièces", "Vous n'avez pas assez de pièces pour cet indice", false);
            return;
        }

        // Show confirmation dialog first
        showHintAlert(() -> {
            // Only get the hint after confirmation
            String hint = game.useHint();

            // Deduct coins
            user.addCoins(-hintCost);
            UserService.updateUserStats(user);
            updateUserInfo();

            // Remove one heart
            if (!heartImages.isEmpty()) {
                heartsContainer.getChildren().remove(heartImages.remove(heartImages.size() - 1));
            }

            // Update hint button
            updateHintButton();

            // Show the hint
            showAlert("Indice", hint, false);
        });
    }

    private void startNewGame() {
        if (dictionary == null) {
            showAlert("Error", "Dictionary not initialized", true);
            return;
        }

        if (currentTheme == null) {
            currentTheme = dictionary.getAvailableThemes().iterator().next();
        }

        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getCurrentUser();
            if (user.getCurrentKeyboardStyle() != null) {
                applyKeyboardStyle(user.getCurrentKeyboardStyle());
            }
        }

        // Get difficulty from session
        Difficulty difficulty = sessionManager.getCurrentDifficulty();
        WordDictionary.WordWithHints wordWithHints = dictionary.getRandomWordWithHints(currentTheme, difficulty);
        game = new Game(wordWithHints.getWord(), wordWithHints.getHints());

        initializeHearts();

        // Update difficulty display and set initial time
        updateDifficultyDisplay(difficulty);

        // Reset timer with the new time
        resetTimer();
        updateUI();
        setupKeyboard();

        wordLabel.setText("Thème: " + currentTheme + "\n" + game.getCurrentState());
        startGameWithTimer();
    }

    private void updateDifficultyDisplay(Difficulty difficulty) {
        String styleClass = "";
        String emoji = "";

        switch(difficulty) {
            case EASY:
                timeRemaining = 90;
                styleClass = "easy-difficulty";
                emoji = "😊";
                break;
            case MEDIUM:
                timeRemaining = 60;
                styleClass = "medium-difficulty";
                emoji = "😐";
                break;
            case HARD:
                timeRemaining = 45;
                styleClass = "hard-difficulty";
                emoji = "😓";
                break;
            case EXTRA_HARD:
                timeRemaining = 30;
                styleClass = "extra-hard-difficulty";
                emoji = "😰";
                break;
        }

        difficultyLabel.getStyleClass().setAll("difficulty-label", styleClass);
        difficultyLabel.setText(difficulty.toString() + " " + emoji);
    }

    private void startGameWithTimer() {
        if (countdownTimeline != null) {
            countdownTimeline.playFromStart();
        }
    }


    public void updateProfileIcon() {
        try {
            User user = sessionManager.getCurrentUser();
            String avatarPath = "/resources/Images/store/" + user.getAvatarId() + ".png";
            InputStream imageStream = getClass().getResourceAsStream(avatarPath);

            if (imageStream != null) {
                profileIcon.setImage(new Image(imageStream));
            } else {
                // Fallback si l'image n'existe pas
                profileIcon.setImage(new Image(
                        getClass().getResourceAsStream("/resources/Images/profile-icon.png")));
            }
        } catch (Exception e) {
            System.err.println("Error loading avatar: " + e.getMessage());
        }
    }

    @FXML
    private void handleProfileClick() {
        stopTimeWarningSound();
        try {
            // Pause game timers
            if (countdownTimeline != null) {
                countdownTimeline.pause();
            }

            // Load profile view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/profile-view.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage currentStage = (Stage) keyboardGrid.getScene().getWindow();

            // Create newscene
            Scene profileScene = new Scene(root);
            profileScene.getStylesheets().add(getClass().getResource("../resources/styles.css").toExternalForm());

            // Set controller references
            ProfileController profileController = loader.getController();
            profileController.setPreviousScene(keyboardGrid.getScene());
            profileController.setHangmanController(this);
            profileController.setStage(currentStage); // Pass the stage reference

            // Apply fade transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), keyboardGrid.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                currentStage.setScene(profileScene);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
            // Resume game if error occurs
            if (countdownTimeline != null) {
                countdownTimeline.play();
            }
        }
    }

    public void resumeGame() {
        if (countdownTimeline != null) {
            countdownTimeline.play();
        }
        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getCurrentUser();
            if (user.getCurrentWallpaper() != null) {
                // Small delay to ensurescene is ready
                PauseTransition delay = new PauseTransition(Duration.millis(100));
                delay.setOnFinished(e -> applyBackground(user.getCurrentWallpaper()));
                delay.play();
            }
        }
    }

    private void drawHangman(int wrongAttempts) {
        gc.clearRect(0, 0, hangmanCanvas.getWidth(), hangmanCanvas.getHeight());

        // Draw gallows
        gc.strokeLine(0, 180, 80, 180);  // Base
        gc.strokeLine(30, 180, 30, 20);    // Pole
        gc.strokeLine(30, 20, 100, 20);    // Top
        gc.strokeLine(100, 20, 100, 40);   // Rope

        // Draw body parts based on wrong attempts
        if (wrongAttempts > 0) gc.strokeOval(90, 40, 20, 20);  // Head
        if (wrongAttempts > 1) gc.strokeLine(100, 60, 100, 100); // Body
        if (wrongAttempts > 2) gc.strokeLine(100, 70, 80, 60);  // Left arm
        if (wrongAttempts > 3) gc.strokeLine(100, 70, 120, 60);  // Right arm
        if (wrongAttempts > 4) gc.strokeLine(100, 100, 80, 120); // Left leg
        if (wrongAttempts > 5) gc.strokeLine(100, 100, 120, 120); // Right leg
        if (wrongAttempts > 6) gc.strokeLine(95, 45, 100, 50);   // Left eye
        if (wrongAttempts > 7) gc.strokeLine(105, 45, 100, 50);   // Right eye
    }

    private void updateUI() {
        wordLabel.setText("Thème: " + currentTheme + "\n" + game.getCurrentState());
        attemptsLabel.setText("Essais restants: " + game.getRemainingAttempts());
        guessedLettersLabel.setText("Lettres essayées: " + game.getGuessedLetters().toString());
        drawHangman(8 - game.getRemainingAttempts());

        if (game.isWon()) {
            countdownTimeline.stop();
            if (sessionManager.isLoggedIn()) {
                User user = sessionManager.getCurrentUser();
                user.addWin();
                UserService.updateUserStats(user);
                updateUserInfo(); // Immediate update
                //showLevelUpAnimation();
            }
            playWinAnimation();
            showAlert("Félicitations!", "Vous avez gagné!\n\nLe mot était: " + game.getWordToGuess(), true);
        } else if (game.isLost()) {
            countdownTimeline.stop();
            if (sessionManager.isLoggedIn()) {
                User user = sessionManager.getCurrentUser();
                user.addLoss();
                UserService.updateUserStats(user);
                updateUserInfo(); // Immediate update
            }
            playLoseAnimation();
            showAlert("Game Over", "Vous avez perdu!\n\nLe mot était: " + game.getWordToGuess(), true);
        }
    }

    private void playWinAnimation() {
        Pane rootPane = (Pane) keyboardGrid.getScene().getRoot();
        Pane confettiContainer = new Pane();
        rootPane.getChildren().add(confettiContainer);

        // Create a fade-out effect for the game UI
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), rootPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.3);

        // Create confetti
        Timeline confettiTimeline = new Timeline();
        for (int i = 0; i < 30; i++) {
            Label confetti = new Label("★");
            confetti.setStyle("-fx-text-fill: " + getRandomColor() + "; -fx-font-size: 20px;");
            confetti.setLayoutX(Math.random() * rootPane.getWidth());
            confetti.setLayoutY(-20);
            confettiContainer.getChildren().add(confetti);

            KeyFrame kf = new KeyFrame(
                    Duration.millis(i * 100),
                    new KeyValue(confetti.layoutYProperty(), rootPane.getHeight()),
                    new KeyValue(confetti.rotateProperty(), 360),
                    new KeyValue(confetti.opacityProperty(), 0)
            );
            confettiTimeline.getKeyFrames().add(kf);
        }

        // Create a fade-in effect after confetti
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), rootPane);
        fadeIn.setFromValue(0.3);
        fadeIn.setToValue(1.0);

        // Sequence the animations
        SequentialTransition sequence = new SequentialTransition(
                fadeOut,
                confettiTimeline,
                fadeIn
        );

        // Clean up after all animations
        sequence.setOnFinished(e -> {
            rootPane.getChildren().remove(confettiContainer);
            rootPane.setOpacity(1.0); // Ensure full opacity
        });

        sequence.play();
    }

    private void playLoseAnimation() {
        // Shake animation
        Timeline shakeTimeline = new Timeline(
                new KeyFrame(Duration.millis(0), new KeyValue(hangmanCanvas.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(100), new KeyValue(hangmanCanvas.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(200), new KeyValue(hangmanCanvas.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(300), new KeyValue(hangmanCanvas.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(400), new KeyValue(hangmanCanvas.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(500), new KeyValue(hangmanCanvas.translateXProperty(), 0))
        );
        shakeTimeline.play();
    }

    private String getRandomColor() {
        String[] colors = {"#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF"};
        return colors[(int)(Math.random() * colors.length)];
    }

    private void playWinSound() {
        if (winSoundPlayer != null) {
            // Stop other sounds first
            if (loseSoundPlayer != null) loseSoundPlayer.stop();
            if (timeWarningSoundPlayer != null) timeWarningSoundPlayer.stop();

            winSoundPlayer.stop();
            winSoundPlayer.play();
        }
    }

    private void playLoseSound() {
        if (loseSoundPlayer != null) {
            // Stop other sounds first
            if (winSoundPlayer != null) winSoundPlayer.stop();
            if (timeWarningSoundPlayer != null) timeWarningSoundPlayer.stop();

            loseSoundPlayer.stop();
            loseSoundPlayer.play();
        }
    }

    private void setupKeyboard() {
        keyboardGrid.getChildren().clear();

        // Add style class to the grid itself
        keyboardGrid.getStyleClass().add("keyboard-grid");

        for (int row = 0; row < keyboardRows.length; row++) {
            String[] letters = keyboardRows[row].split(" ");
            for (int col = 0; col < letters.length; col++) {
                final char letter = letters[col].charAt(0);

                Button button = new Button(letters[col]);
                button.setPrefSize(40, 40);
                button.getStyleClass().add("button"); // Important - must match CSS selector
                button.setOnAction(e -> handleLetterGuess(letter));

                if (game != null && game.getGuessedLetters().contains(letter)) {
                    button.setDisable(true);
                }

                keyboardGrid.add(button, col, row);
            }
        }

        // Re-apply current style
        if (currentKeyboardStyle != null) {
            applyKeyboardStyle(currentKeyboardStyle);
        }
    }

    private void handleLetterGuess(char letter) {
        boolean correctGuess = game.guessLetter(letter);
        if (correctGuess) {
            // Add time bonus for correct guess
            timeRemaining += 5;
            if (timeRemaining > 90) timeRemaining = 90;
            updateTimerDisplay();
            showBonusTime(5);
        }
        updateUI();
        setupKeyboard();
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getText().length() == 1) {
            char letter = event.getText().toUpperCase().charAt(0);
            if (Character.isLetter(letter)) {
                handleLetterGuess(letter);
            }
        }
    }

    @FXML
    private void handleNewGame() {
        startNewGame();
        startGameWithTimer();
    }

    private void showAlert(String title, String message, boolean startNewGameAfter) {

        if (countdownTimeline != null) {
            countdownTimeline.pause();
        }

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);

        alert.setOnHidden(e -> {
            // Stop all sounds when dialog closes
            if (winSoundPlayer != null) {
                winSoundPlayer.stop();
            }
            if (loseSoundPlayer != null) {
                loseSoundPlayer.stop();
            }
            if (timeWarningSoundPlayer != null) {
                timeWarningSoundPlayer.stop();
            }
        });

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("../resources/styles.css").toExternalForm()
        );
        dialogPane.getStyleClass().add(title.equals("Félicitations!") || title.equals("Indice") ? "win-alert" : "lose-alert");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: linear-gradient(to bottom right, #f5f7fa, #c3cfe2);" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;" +
                "-fx-border-width: 3;" +
                "-fx-border-color: " +
                (title.equals("Félicitations!") || title.equals("Indice") ? "#4CAF50" : "#f44336") + ";");


        Pane decorationTop = new Pane();
        decorationTop.setPrefSize(200, 10);
        decorationTop.setStyle("-fx-background-color: linear-gradient(to right, transparent, " +
                (title.equals("Félicitations!") || title.equals("Indice") ? "#4CAF50" : "#f44336") + ", transparent);");

        Pane decorationBottom = new Pane();
        decorationBottom.setPrefSize(200, 10);
        decorationBottom.setStyle("-fx-background-color: linear-gradient(to right, transparent, " +
                (title.equals("Félicitations!") || title.equals("Indice") ? "#4CAF50" : "#f44336") + ", transparent);");

        ImageView icon = new ImageView();
        icon.setFitWidth(100);
        icon.setFitHeight(100);

        if (title.equals("Félicitations!")) {
            icon.setImage(new Image(getClass().getResourceAsStream("../resources/Images/trophy.png")));
            playWinSound();
        } else if (title.equals("paspièces")) {
            icon.setImage(new Image(getClass().getResourceAsStream("../resources/Images/moneyover.png")));
            playLoseSound();
        } else if (title.equals("Temps écoulé")) {
            icon.setImage(new Image(getClass().getResourceAsStream("../resources/Images/timeover.png")));
            playLoseSound();
        } else if (title.equals("Indice")) {
            icon.setImage(new Image(getClass().getResourceAsStream("../resources/Images/hint.png")));
        } else {
            icon.setImage(new Image(getClass().getResourceAsStream("../resources/Images/skull.png")));
            playLoseSound();
        }

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #2c3e50; -fx-wrap-text: true; -fx-font-weight: bold;");
        messageLabel.setMaxWidth(350);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setPadding(new Insets(0, 20, 0, 20));

        content.getChildren().addAll(decorationTop, icon, messageLabel, decorationBottom);
        dialogPane.setContent(content);

        ButtonType okButton = new ButtonType("Continuer", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.centerOnScreen();

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), dialogPane);
        scaleIn.setFromX(0.9);
        scaleIn.setFromY(0.9);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dialogPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition openTransition = new ParallelTransition(scaleIn, fadeIn);
        openTransition.play();

        if (title.equals("Félicitations!")) {
            ScaleTransition pulse = new ScaleTransition(Duration.millis(800), icon);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.1);
            pulse.setToY(1.1);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();
        }

        alert.showAndWait().ifPresent(response -> {
            if (startNewGameAfter) {
                startNewGame();

            } else {
                // Resume timer if not starting new game
                if (countdownTimeline != null) {
                    countdownTimeline.play();
                }
            }
        });
    }

    @FXML
    private void handleBackToThemes() {
        stopTimeWarningSound();
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/theme-selection.fxml"));
            Parent root = loader.load();

            ThemeSelectionController controller = loader.getController();
            controller.setDictionary(dictionary);
            controller.updateUserInfo();

            Stage stage = (Stage) keyboardGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add this method to update UI
    public void updateUserInfo() {
        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getCurrentUser();
            userInfoLabel.setText(String.format("%s | %d 💰 | %d ✅ | %d ❌",
                    user.getUsername(),
                    user.getCoins(),
                    user.getWins(),
                    user.getLosses()));

            levelLabel.setText("Niveau " + user.getLevel());
            xpProgressBar.setProgress((double) user.getXp() / user.getXpToNextLevel());
            int xpPercent = (int) ((double) user.getXp() / user.getXpToNextLevel() * 100);
            xpLabel.setText(user.getXp() + "/" + user.getXpToNextLevel() + " XP" + " (" + xpPercent + "%)");

        }
    }

    private void showLevelUpAnimation() {
        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getCurrentUser();

            // Check if user just leveled up (XP was reset)
            if (user.getXp() == 0 && user.getLevel() > 1) {
                Pane rootPane = (Pane) keyboardGrid.getScene().getRoot();

                // Create level up label
                Label levelUpLabel = new Label("Niveau " + user.getLevel() + " atteint!");
                levelUpLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: gold; -fx-font-weight: bold;");
                levelUpLabel.setLayoutX(rootPane.getWidth()/2 - 100);
                levelUpLabel.setLayoutY(rootPane.getHeight()/2 - 50);
                levelUpLabel.setOpacity(0);

                rootPane.getChildren().add(levelUpLabel);

                // Animation sequence
                ScaleTransition scale = new ScaleTransition(Duration.millis(1000), levelUpLabel);
                scale.setFromX(0.5);
                scale.setFromY(0.5);
                scale.setToX(1.5);
                scale.setToY(1.5);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), levelUpLabel);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), levelUpLabel);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setDelay(Duration.millis(1500));

                SequentialTransition sequence = new SequentialTransition(
                        fadeIn,
                        scale,
                        new PauseTransition(Duration.millis(1000)),
                        fadeOut
                );

                sequence.setOnFinished(e -> rootPane.getChildren().remove(levelUpLabel));
                sequence.play();
            }
        }
    }

    public void updateAvatar(String newAvatarId) {
        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getCurrentUser();
            user.changeAvatar(newAvatarId);
            UserService.updateUserStats(user);
            updateProfileIcon(); // Mettre à jour l'image
        }
    }

    public String getCurrentKeyboardStyle() {
        return currentKeyboardStyle;
    }

    public void applyKeyboardStyle(String cssPath) {
        try {
            keyboardGrid.getStylesheets().clear();
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                keyboardGrid.getStylesheets().add(cssUrl.toExternalForm());
                this.currentKeyboardStyle = cssPath;

                // Save to user profile
                if (sessionManager.isLoggedIn()) {
                    User user = sessionManager.getCurrentUser();
                    user.setCurrentKeyboardStyle(cssPath);
                    UserService.updateUserStats(user);
                }
            }
        } catch (Exception e) {
            System.err.println("Error applying keyboard style: " + e.getMessage());
        }
    }

    public void applyBackground(String cssPath) {
        try {
            // Save the background preference regardless of scene availability
            if (sessionManager.isLoggedIn()) {
                User user = sessionManager.getCurrentUser();
                user.setCurrentWallpaper(cssPath);
                UserService.updateUserStats(user);
            }


            if (keyboardGrid.getScene() != null && keyboardGrid.getScene().getRoot() != null) {
                Pane rootPane = (Pane) keyboardGrid.getScene().getRoot();
                Platform.runLater(() -> {
                    rootPane.getStylesheets().removeIf(s -> s.contains("background"));
                    URL cssUrl = getClass().getResource(cssPath);
                    if (cssUrl != null) {
                        rootPane.getStylesheets().add(cssUrl.toExternalForm());
                    } else {
                        applyDefaultBackground();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error applying background: " + e.getMessage());
            e.printStackTrace();
            applyDefaultBackground();
        }
    }

    private void applyDefaultBackground() {
        try {
            String defaultPath = "/resources/styles/default-wallpaper.css";
            if (keyboardGrid.getScene() != null && keyboardGrid.getScene().getRoot() != null) {
                Pane rootPane = (Pane) keyboardGrid.getScene().getRoot();
                rootPane.getStylesheets().removeIf(s -> s.contains("background"));

                URL defaultUrl = getClass().getResource(defaultPath);
                if (defaultUrl != null) {
                    rootPane.getStylesheets().add(defaultUrl.toExternalForm());
                }
            }
        } catch (Exception e) {
            System.err.println("Error applying default background: " + e.getMessage());
        }
    }

    public void applyHangmanStyle(String cssPath) {
        try {
            // Clear existing styles
            hangmanCanvas.getStyleClass().clear();

            // Add the new style
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                hangmanCanvas.getStyleClass().add("custom-hangman");
            }
        } catch (Exception e) {
            System.err.println("Error loading hangman style: " + e.getMessage());
        }
    }

    @FXML
    private void openStore() {
        stopTimeWarningSound();
        try {
            if (countdownTimeline != null) {
                countdownTimeline.pause();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/store-view.fxml"));
            Parent root = loader.load();

            Stage storeStage = new Stage();
            storeStage.initModality(Modality.APPLICATION_MODAL);
            storeStage.initOwner(keyboardGrid.getScene().getWindow());
            storeStage.setTitle("Boutique");
            storeStage.setScene(new Scene(root));

            storeStage.setOnHidden(e -> {
                if (countdownTimeline != null) {
                    countdownTimeline.play();
                }
                updateUserInfo();
            });

            storeStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applySelectedAvatar(String avatarId) {
        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getCurrentUser();
            user.changeAvatar(avatarId);
            UserService.updateUserStats(user);

            // Update profile icon
            try {
                Image avatarImage = new Image(getClass().getResourceAsStream("/resources/Images/" + avatarId + ".png"));
                profileIcon.setImage(avatarImage);
            } catch (Exception e) {
                System.err.println("Error loading avatar image: " + e.getMessage());
            }
        }
    }

    // Add logout button handler
    @FXML
    private void handleLogout() {
        stopTimeWarningSound();
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        sessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../views/LoginScreen.fxml"));
            Stage stage = (Stage) keyboardGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
