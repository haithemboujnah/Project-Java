package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
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
import models.User;
import Services.UserService;
import Services.SessionManager;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private VBox formContainer;
    @FXML private ImageView logoImageView;

    @FXML
    public void initialize() {
        animateForm();
    }

    private void animateForm() {
        // Initial state for fade-in
        formContainer.setOpacity(0);
        logoImageView.setOpacity(0);

        // Logo animation
        FadeTransition logoFade = new FadeTransition(Duration.millis(800), logoImageView);
        logoFade.setFromValue(0);
        logoFade.setToValue(1);

        ScaleTransition logoScale = new ScaleTransition(Duration.millis(1000), logoImageView);
        logoScale.setFromX(0.5);
        logoScale.setFromY(0.5);
        logoScale.setToX(1);
        logoScale.setToY(1);

        RotateTransition logoRotate = new RotateTransition(Duration.millis(1200), logoImageView);
        logoRotate.setFromAngle(-5);
        logoRotate.setToAngle(5);
        logoRotate.setCycleCount(2);
        logoRotate.setAutoReverse(true);

        // Form animation
        FadeTransition formFade = new FadeTransition(Duration.millis(600), formContainer);
        formFade.setFromValue(0);
        formFade.setToValue(1);
        formFade.setDelay(Duration.millis(300));

        // Play animations in sequence
        logoFade.play();
        logoScale.play();
        logoRotate.play();
        formFade.play();
    }

    @FXML
    private void handleLogin() {
        // Button click animation
        ScaleTransition clickAnim = new ScaleTransition(Duration.millis(100), loginButton);
        clickAnim.setFromX(1);
        clickAnim.setFromY(1);
        clickAnim.setToX(0.95);
        clickAnim.setToY(0.95);
        clickAnim.setCycleCount(2);
        clickAnim.setAutoReverse(true);
        clickAnim.play();

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            shakeError();
            return;
        }

        User user = UserService.loginUser(username, password);
        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            navigateToThemeSelection();
        } else {
            showError("Nom d'utilisateur ou mot de passe incorrect");
            shakeError();
        }
    }

    private void navigateToThemeSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/theme-selection.fxml"));
            Parent root = loader.load();

            ThemeSelectionController controller = loader.getController();
            controller.setCurrentUser(SessionManager.getInstance().getCurrentUser());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));

            // Add fade transition between scenes
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), usernameField.getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> stage.show());
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
    private void switchToRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../views/RegisterScreen.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();

            // Add scene transition animation
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