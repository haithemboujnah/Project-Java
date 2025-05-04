package controllers;

import javafx.animation.*;
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

    public void initialize() {
        // Initial state
        startButton.setVisible(false);
        startButton.setOpacity(0);
        progressBar.setProgress(0);

        // Create smoother loading animation
        loadingTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(2.5),
                        new KeyValue(progressBar.progressProperty(), 1.0))
        );

        loadingTimeline.setOnFinished(e -> onLoadingComplete());
        loadingTimeline.play();

        // Percentage text animation
        Timeline textTimeline = new Timeline(
                new KeyFrame(Duration.millis(50), e -> {
                    int percent = (int)(progressBar.getProgress() * 100);
                    progressText.setText(String.format("%d%%", percent));
                }
                ));
        textTimeline.setCycleCount(Timeline.INDEFINITE);
        textTimeline.play();
    }

    private void onLoadingComplete() {
        progressText.setText("Prêt!");

        // Animate button appearance
        startButton.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), startButton);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Add bounce effect
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), startButton);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    @FXML
    private void handleStartGame() {
        try {
            // Load theme selection directly
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/LoginScreen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}