package controllers;

import javafx.animation.*;
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

    @FXML private Label wordLabel;
    @FXML private Label attemptsLabel;
    @FXML private Label guessedLettersLabel;
    @FXML private GridPane keyboardGrid;
    @FXML private Canvas hangmanCanvas;

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
            // Load dictionary from resources
            InputStream wordStream = getClass().getResourceAsStream("../resources/words.txt");
            if (wordStream == null) {
                throw new IOException("Could not find words.txt in resources");
            }
            dictionary = new WordDictionary(wordStream);
            startNewGame();
        } catch (IOException e) {
            showAlert("Error", "Could not load dictionary: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void setTheme(String theme, WordDictionary dictionary) {
        this.currentTheme = theme;
        this.dictionary = dictionary;
        startNewGame(); // Start game immediately with the selected theme
    }


    private void startNewGame() {
        if (dictionary == null) {
            showAlert("Error", "Dictionary not initialized");
            return;
        }

        if (currentTheme == null) {
            // Default to first theme if none selected
            currentTheme = dictionary.getAvailableThemes().iterator().next();
        }

        game = new Game(dictionary.getRandomWord(currentTheme));
        updateUI();
        setupKeyboard();

        // Show the current theme to the player
        wordLabel.setText("Thème: " + currentTheme + "\n" + game.getCurrentState());
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
            playWinAnimation();
            showAlert("Félicitations!", "Vous avez gagné!\n\nLe mot était: " + game.getWordToGuess());
            startNewGame();
        } else if (game.isLost()) {
            playLoseAnimation();
            showAlert("Game Over", "Vous avez perdu!\n\nLe mot était: " + game.getWordToGuess());
            startNewGame();
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
        game.guessLetter(letter);
        updateUI();
        setupKeyboard(); // Refresh keyboard buttons
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
    }

    private void showAlert(String title, String message) {
        // Create custom alert
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);

        // Custom dialog pane styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("../resources/styles.css").toExternalForm()
        );
        dialogPane.getStyleClass().add(title.equals("Félicitations!") ? "win-alert" : "lose-alert");

        // Create custom content with attractive background
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: linear-gradient(to bottom right, #f5f7fa, #c3cfe2);" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;" +
                "-fx-border-width: 3;" +
                "-fx-border-color: " + (title.equals("Félicitations!") ? "#4CAF50" : "#f44336") + ";");

        // Add decorative elements
        Pane decorationTop = new Pane();
        decorationTop.setPrefSize(200, 10);
        decorationTop.setStyle("-fx-background-color: linear-gradient(to right, transparent, " +
                (title.equals("Félicitations!") ? "#4CAF50" : "#f44336") + ", transparent);");

        Pane decorationBottom = new Pane();
        decorationBottom.setPrefSize(200, 10);
        decorationBottom.setStyle("-fx-background-color: linear-gradient(to right, transparent, " +
                (title.equals("Félicitations!") ? "#4CAF50" : "#f44336") + ", transparent);");

        // Add icon based on alert type
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

        // Create message label with better typography
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #2c3e50; -fx-wrap-text: true; -fx-font-weight: bold;");
        messageLabel.setMaxWidth(350);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setPadding(new Insets(0, 20, 0, 20));

        // Add elements to content
        content.getChildren().addAll(decorationTop, icon, messageLabel, decorationBottom);
        dialogPane.setContent(content);

        // Custom button
        ButtonType okButton = new ButtonType("Continuer", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        // Center the alert on screen
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.centerOnScreen();

        // Animation effects
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), dialogPane);
        scaleIn.setFromX(0.9);
        scaleIn.setFromY(0.9);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dialogPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition openTransition = new ParallelTransition(scaleIn, fadeIn);

        // Show and animate
        alert.show();
        openTransition.play();

        // Add pulse effect for win alerts
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
    }

    @FXML
    private void handleBackToThemes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/theme-selection.fxml"));
            Parent root = loader.load();

            // Pass the dictionary back to maintain state
            ThemeSelectionController controller = loader.getController();
            controller.setDictionary(dictionary);

            Stage stage = (Stage) keyboardGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}