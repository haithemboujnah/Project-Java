package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import models.Game;
import models.WordDictionary;

import java.io.IOException;
import java.io.InputStream;
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

    public void initialize() {
        gc = hangmanCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

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
            showAlert("Félicitations!", "Vous avez gagné! Le mot était: " + game.getWordToGuess());
            startNewGame();
        } else if (game.isLost()) {
            showAlert("Game Over", "Vous avez perdu! Le mot était: " + game.getWordToGuess());
            startNewGame();
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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