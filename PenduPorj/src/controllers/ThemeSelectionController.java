package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import models.WordDictionary;

import java.io.IOException;
import java.io.InputStream;

public class ThemeSelectionController {
    @FXML private Button schoolButton, animalsButton, jobsButton,
            vehiclesButton, techButton, bodyButton,
            countriesButton, fruitsButton, sportsButton;

    private WordDictionary dictionary;

    public void initialize() {
        try {
            InputStream wordStream = getClass().getResourceAsStream("../resources/words.txt");
            dictionary = new WordDictionary(wordStream);
            setupButtonActions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDictionary(WordDictionary dictionary) {
        this.dictionary = dictionary;
        setupButtonActions();
    }

    private void setupButtonActions() {
        schoolButton.setOnAction(e -> startGame("École"));
        animalsButton.setOnAction(e -> startGame("Animaux"));
        jobsButton.setOnAction(e -> startGame("Métiers"));
        vehiclesButton.setOnAction(e -> startGame("Véhicules"));
        techButton.setOnAction(e -> startGame("Technologie"));
        bodyButton.setOnAction(e -> startGame("Corps"));
        countriesButton.setOnAction(e -> startGame("Pays"));
        fruitsButton.setOnAction(e -> startGame("Fruits"));
        sportsButton.setOnAction(e -> startGame("Sports"));
    }

    private void startGame(String theme) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/hangman.fxml"));
            Parent root = loader.load();

            HangmanController controller = loader.getController();
            controller.setTheme(theme, dictionary);

            Stage stage = (Stage) schoolButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}