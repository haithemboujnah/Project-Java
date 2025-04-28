package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class SplashController {
    @FXML private ProgressBar progressBar;
    @FXML private Label progressText;
    @FXML private Button startButton;

    private Timeline loadingTimeline;
    private double progress = 0;

    public void initialize() {
        // Hide start button initially
        startButton.setVisible(false);

        // Create loading animation
        loadingTimeline = new Timeline(
                new KeyFrame(Duration.millis(100), event -> {
                    progress += 0.02;
                    if (progress >= 1.0) {
                        progress = 1.0;
                        loadingTimeline.stop();
                        onLoadingComplete();
                    }
                    progressBar.setProgress(progress);
                    progressText.setText(String.format("%d%%", (int)(progress * 100)));
                })
        );
        loadingTimeline.setCycleCount(Timeline.INDEFINITE);
        loadingTimeline.play();
    }

    private void onLoadingComplete() {
        startButton.setVisible(true);
        progressText.setText("Prêt!");
    }

    @FXML
    private void handleStartGame() {
        try {
            // Load main game scene
            Parent root = FXMLLoader.load(getClass().getResource("../views/hangman.fxml"));
            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Le Pendu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}