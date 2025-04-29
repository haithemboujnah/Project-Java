package controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.WordDictionary;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ThemeSelectionController {
    @FXML private Button schoolButton, animalsButton, jobsButton,
            vehiclesButton, techButton, bodyButton,
            countriesButton, fruitsButton, sportsButton;

    @FXML private GridPane themeGrid;

    private WordDictionary dictionary;
    private Map<String, Image> themeImages = new HashMap<>();

    public void initialize() {
        try {
            // Load dictionary
            InputStream wordStream = getClass().getResourceAsStream("../resources/words.txt");
            dictionary = new WordDictionary(wordStream);

            // Load theme images
            loadThemeImages();

            // Setup button actions
            setupButtonActions();

            // Setup hover animations
            setupHoverAnimations();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDictionary(WordDictionary dictionary) {
        this.dictionary = dictionary;
        setupButtonActions(); }

    private void loadThemeImages() {
        try {
            themeImages.put("École", loadImage("school.png"));
            themeImages.put("Animaux", loadImage("animals.png"));
            themeImages.put("Métiers", loadImage("jobs.png"));
            themeImages.put("Véhicules", loadImage("vehicles.png"));
            themeImages.put("Technologie", loadImage("tech.png"));
            themeImages.put("Corps", loadImage("body.png"));
            themeImages.put("Pays", loadImage("countries.png"));
            themeImages.put("Fruits", loadImage("fruits.png"));
            themeImages.put("Sports", loadImage("sports.png"));
        } catch (Exception e) {
            System.err.println("Error loading theme images: " + e.getMessage());
        }
    }

    private Image loadImage(String filename) {
        try (InputStream is = getClass().getResourceAsStream("../resources/Images/" + filename)) {
            if (is != null) {
                return new Image(is);
            } else {
                System.err.println("Image not found: " + filename);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + filename);
            return null;
        }
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

    private void setupHoverAnimations() {
        Button[] buttons = {schoolButton, animalsButton, jobsButton,
                vehiclesButton, techButton, bodyButton,
                countriesButton, fruitsButton, sportsButton};

        for (Button button : buttons) {
            String theme = button.getText();
            ImageView imageView = new ImageView(themeImages.get(theme));
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);
            imageView.setVisible(false);

            // Set up rotation transforms for 3D effect
            Rotate imageRotate = new Rotate(0, Rotate.Y_AXIS);
            imageView.getTransforms().add(imageRotate);

            Rotate buttonRotate = new Rotate(0, Rotate.Y_AXIS);
            button.getTransforms().add(buttonRotate);

            StackPane buttonContainer = new StackPane();
            buttonContainer.getChildren().addAll(button, imageView);
            buttonContainer.setStyle("-fx-background-color: transparent;");

            // Replace original button with the container
            int col = GridPane.getColumnIndex(button);
            int row = GridPane.getRowIndex(button);
            themeGrid.getChildren().remove(button);
            themeGrid.add(buttonContainer, col, row);

            // Animation for hover enter
            button.setOnMouseEntered(e -> {
                // Flip animation (text to image)
                Timeline flipOut = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(buttonRotate.angleProperty(), 0)),
                        new KeyFrame(Duration.millis(500), new KeyValue(buttonRotate.angleProperty(), 90))
                );
                flipOut.setOnFinished(ev -> {
                    button.setVisible(false);
                    imageView.setVisible(true);
                    Timeline flipIn = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(imageRotate.angleProperty(), 90)),
                            new KeyFrame(Duration.millis(500), new KeyValue(imageRotate.angleProperty(), 0))
                    );
                    flipIn.play();
                });
                flipOut.play();
            });

            // Animation for hover exit
            buttonContainer.setOnMouseExited(e -> {
                // Flip animation (image to text)
                Timeline flipOut = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(imageRotate.angleProperty(), 0)),
                        new KeyFrame(Duration.millis(500), new KeyValue(imageRotate.angleProperty(), 90))
                );
                flipOut.setOnFinished(ev -> {
                    imageView.setVisible(false);
                    button.setVisible(true);
                    Timeline flipIn = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(buttonRotate.angleProperty(), 90)),
                            new KeyFrame(Duration.millis(500), new KeyValue(buttonRotate.angleProperty(), 0))
                    );
                    flipIn.play();
                });
                flipOut.play();
            });

            // Handle click on both button and image
            button.setOnMouseClicked(e -> startGame(theme));
            imageView.setOnMouseClicked(e -> startGame(theme));
        }
    }

    private void startGame(String theme) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/hangman.fxml"));
            Parent root = loader.load();

            HangmanController controller = loader.getController();
            controller.setTheme(theme, dictionary);

            Stage stage = (Stage) themeGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}