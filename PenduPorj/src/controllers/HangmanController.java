package controllers;

import Services.SessionManager;
import Services.UserService;
import javafx.animation.*;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import models.Game;
import models.User;
import models.WordDictionary;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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

        // Initialize timer first
        setupTimer();
        updateTimerDisplay();
        bonusTimeLabel.setVisible(false);
        startGameWithTimer();
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
        timeRemaining = 90;
        updateTimerDisplay();
        if (countdownTimeline != null) {
            countdownTimeline.stop();
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
            if (timeWarningSoundPlayer != null && timeRemaining <= 6) {
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


    private void startNewGame() {
        if (dictionary == null) {
            showAlert("Error", "Dictionary not initialized", true);
            return;
        }

        if (currentTheme == null) {
            currentTheme = dictionary.getAvailableThemes().iterator().next();
        }

        game = new Game(dictionary.getRandomWord(currentTheme));
        resetTimer();
        updateUI();
        setupKeyboard();

        wordLabel.setText("Thème: " + currentTheme + "\n" + game.getCurrentState());

        // Start timer only when the game is ready
        startGameWithTimer();
    }

    private void startGameWithTimer() {
        if (countdownTimeline != null) {
            countdownTimeline.playFromStart();
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
            winSoundPlayer.stop();
            winSoundPlayer.play();
        }
    }

    private void playLoseSound() {
        if (loseSoundPlayer != null) {
            loseSoundPlayer.stop();
            loseSoundPlayer.play();
        }
    }

    private void setupKeyboard() {
        keyboardGrid.getChildren().clear();

        for (int row = 0; row < keyboardRows.length; row++) {
            String[] letters = keyboardRows[row].split(" ");
            for (int col = 0; col < letters.length; col++) {

                final char letter = letters[col].charAt(0);

                Button button = new Button(letters[col]);
                button.setPrefSize(40, 40);
                button.setOnAction(e -> handleLetterGuess(letter));

                if (game != null && game.getGuessedLetters().contains(letter)) {
                    button.setDisable(true);
                }

                keyboardGrid.add(button, col, row);
            }
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

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("../resources/styles.css").toExternalForm()
        );
        dialogPane.getStyleClass().add(title.equals("Félicitations!") ? "win-alert" : "lose-alert");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: linear-gradient(to bottom right, #f5f7fa, #c3cfe2);" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;" +
                "-fx-border-width: 3;" +
                "-fx-border-color: " + (title.equals("Félicitations!") ? "#4CAF50" : "#f44336") + ";");

        Pane decorationTop = new Pane();
        decorationTop.setPrefSize(200, 10);
        decorationTop.setStyle("-fx-background-color: linear-gradient(to right, transparent, " +
                (title.equals("Félicitations!") ? "#4CAF50" : "#f44336") + ", transparent);");

        Pane decorationBottom = new Pane();
        decorationBottom.setPrefSize(200, 10);
        decorationBottom.setStyle("-fx-background-color: linear-gradient(to right, transparent, " +
                (title.equals("Félicitations!") ? "#4CAF50" : "#f44336") + ", transparent);");

        ImageView icon = new ImageView();
        icon.setFitWidth(100);
        icon.setFitHeight(100);

        if (title.equals("Félicitations!")) {
            icon.setImage(new Image(getClass().getResourceAsStream("../resources/Images/trophy.png")));
            playWinSound();
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
        }
    }

    // Add logout button handler
    @FXML
    private void handleLogout() {
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