package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import Services.UserService;

import java.io.IOException;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;
    @FXML private VBox formContainer;
    @FXML private ImageView logoImageView;

    @FXML
    public void initialize() {
        animateForm();
    }

    private void animateForm() {
        // Initial state
        formContainer.setOpacity(0);
        logoImageView.setOpacity(0);

        // Logo animation
        FadeTransition logoFade = new FadeTransition(Duration.millis(800), logoImageView);
        logoFade.setFromValue(0);
        logoFade.setToValue(1);

        ScaleTransition logoScale = new ScaleTransition(Duration.millis(1000), logoImageView);
        logoScale.setFromX(0.8);
        logoScale.setFromY(0.8);
        logoScale.setToX(1);
        logoScale.setToY(1);

        // Form field animations
        FadeTransition formFade = new FadeTransition(Duration.millis(600), formContainer);
        formFade.setFromValue(0);
        formFade.setToValue(1);
        formFade.setDelay(Duration.millis(300));

        // Play animations
        ParallelTransition logoAnim = new ParallelTransition(logoFade, logoScale);
        logoAnim.play();
        formFade.play();
    }

    @FXML
    private void handleRegister() {
        // Button animation
        ScaleTransition clickAnim = new ScaleTransition(Duration.millis(100), registerButton);
        clickAnim.setFromX(1);
        clickAnim.setFromY(1);
        clickAnim.setToX(0.95);
        clickAnim.setToY(0.95);
        clickAnim.setCycleCount(2);
        clickAnim.setAutoReverse(true);
        clickAnim.play();

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            shakeError();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            shakePasswordFields();
            return;
        }

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            shakePasswordFields();
            return;
        }

        if (UserService.registerUser(username, password)) {
            showSuccessAndSwitch();
        } else {
            showError("Ce nom d'utilisateur est déjà pris");
            shakeUsernameField();
        }
    }

    private void shakeUsernameField() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), usernameField);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void shakePasswordFields() {
        TranslateTransition shake1 = new TranslateTransition(Duration.millis(50), passwordField);
        TranslateTransition shake2 = new TranslateTransition(Duration.millis(50), confirmPasswordField);

        shake1.setFromX(0);
        shake1.setByX(10);
        shake1.setCycleCount(6);
        shake1.setAutoReverse(true);

        shake2.setFromX(0);
        shake2.setByX(10);
        shake2.setCycleCount(6);
        shake2.setAutoReverse(true);

        shake1.play();
        shake2.play();
    }

    private void showSuccessAndSwitch() {
        ScaleTransition successAnim = new ScaleTransition(Duration.millis(300), registerButton);
        successAnim.setFromX(1);
        successAnim.setFromY(1);
        successAnim.setToX(1.1);
        successAnim.setToY(1.1);
        successAnim.setAutoReverse(true);
        successAnim.setCycleCount(2);

        successAnim.setOnFinished(e -> switchToLogin());
        successAnim.play();
    }

    private void shakeError() {
        FadeTransition fade = new FadeTransition(Duration.millis(100), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), errorLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);

        fade.play();
        shake.play();
    }

    @FXML
    private void switchToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../views/LoginScreen.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();

            // Scene transition animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), usernameField.getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(new Scene(root));

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}