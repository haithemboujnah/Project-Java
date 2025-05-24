package controllers;

import Services.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.StoreItem;
import models.User;
import Services.SessionManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class StoreController implements Initializable {
    @FXML private TabPane storeTabs;
    @FXML private Label coinsLabel;
    @FXML private Label userLevelLabel;

    private User currentUser;
    private List<StoreItem> storeItems;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        updateUserInfo();

        // Récupérer les items depuis MongoDB
        storeItems = UserService.getAllStoreItems();
        populateStoreTabs();
    }

    private void updateUserInfo() {
        coinsLabel.setText("Pièces: " + currentUser.getCoins());
        userLevelLabel.setText("Niveau " + currentUser.getLevel());
    }

    private void populateStoreTabs() {
        storeTabs.getTabs().clear();

        // Define all supported types with their display names
        Map<String, String> tabTypes = Map.of(
                "KEYBOARD_SKIN", "Claviers",
                "BACKGROUND", "Fonds d'écran",
                "AVATAR", "Avatars"
        );

        // Create tabs for each type that has items
        tabTypes.forEach((type, name) -> {
            List<StoreItem> itemsOfType = storeItems.stream()
                    .filter(item -> item.getType().equals(type))
                    .toList();

            if (!itemsOfType.isEmpty()) {
                Tab tab = new Tab(name);
                tab.setContent(createItemsGrid(itemsOfType));
                storeTabs.getTabs().add(tab);
            }
        });
    }

    private void createTabForType(String type, String tabName) {
        List<StoreItem> itemsOfType = storeItems.stream()
                .filter(item -> item.getType().equals(type))
                .toList();

        if (!itemsOfType.isEmpty()) {
            Tab tab = new Tab(tabName);
            tab.setContent(createItemsGrid(itemsOfType));
            storeTabs.getTabs().add(tab);
        }
    }

    private ScrollPane createItemsGrid(List<StoreItem> items) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Only vertical scroll

        // Use a ListView for better scrolling performance
        ListView<HBox> itemList = new ListView<>();
        itemList.setCellFactory(lv -> new ListCell<HBox>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : item);
            }
        });

        // Add all items to the list
        for (StoreItem item : items) {
            itemList.getItems().add(createStoreItemBox(item));
        }

        // Style the list view
        itemList.setStyle("-fx-background-color: transparent;");
        itemList.setFixedCellSize(100); // Set fixed height for each item

        scrollPane.setContent(itemList);
        return scrollPane;
    }

    private HBox createStoreItemBox(StoreItem item) {
        HBox itemBox = new HBox(15);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(10));
        itemBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        itemBox.setPrefHeight(90); // Fixed height for each item

        // Image
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(item.getImagePath())));
        imageView.setFitWidth(70);
        imageView.setFitHeight(70);

        // Info
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label descLabel = new Label(item.getDescription());
        descLabel.setStyle("-fx-font-size: 12px;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(300);

        Label priceLabel = new Label("Prix: " + item.getPrice() + " pièces");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        infoBox.getChildren().addAll(nameLabel, descLabel, priceLabel);

        // Action button
        Button actionButton = new Button();
        actionButton.setMinWidth(100);
        actionButton.setStyle("-fx-font-weight: bold; -fx-background-radius: 5;");

        if (currentUser.hasPurchased(item.getId())) {
            if (isItemApplied(item)) {
                actionButton.setText("✓ Appliqué");
                actionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            } else {
                actionButton.setText("Appliquer");
                actionButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                actionButton.setOnAction(e -> applyItem(item));
            }
        } else {
            actionButton.setText("Acheter");
            actionButton.setDisable(currentUser.getCoins() < item.getPrice());
            actionButton.setStyle(currentUser.getCoins() >= item.getPrice() ?
                    "-fx-background-color: #FF9800; -fx-text-fill: white;" :
                    "-fx-background-color: #9E9E9E; -fx-text-fill: white;");
            actionButton.setOnAction(e -> purchaseItem(item));
        }

        itemBox.getChildren().addAll(imageView, infoBox, actionButton);
        return itemBox;
    }

    private boolean isItemApplied(StoreItem item) {
        switch (item.getType()) {
            case "AVATAR":
                return currentUser.getAvatarId().equals(item.getResourcePath());
            case "KEYBOARD_SKIN":
                return item.getResourcePath().equals(currentUser.getCurrentKeyboardStyle());
            case "BACKGROUND":
                return item.getResourcePath().equals(currentUser.getCurrentWallpaper());
            default:
                return false;
        }
    }

    private void applyBackground(StoreItem item) {
        HangmanController hangmanController = SessionManager.getInstance().getHangmanController();
        if (hangmanController != null) {
            hangmanController.applyBackground(item.getResourcePath());

            // Update local user reference
            currentUser.setCurrentWallpaper(item.getResourcePath());
            UserService.updateUserStats(currentUser);
            updateUserInfo();
        }
    }


    private void purchaseItem(StoreItem item) {
        if (currentUser.getCoins() >= item.getPrice()) {
            currentUser.addCoins(-item.getPrice());
            currentUser.addPurchasedItem(item.getId());
            UserService.updateUserStats(currentUser);
            updateUserInfo();
            populateStoreTabs(); // Refresh the display

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Achat réussi");
            alert.setHeaderText(null);
            alert.setContentText("Vous avez acheté " + item.getName() + "!");
            alert.showAndWait();
        }
    }

    private void applyItem(StoreItem item) {
        switch (item.getType()) {
            case "AVATAR":
                applyAvatar(item);
                break;
            case "KEYBOARD_SKIN":
                applyKeyboardSkin(item);
                break;
            case "BACKGROUND":
                applyBackground(item);
                break;
        }

        UserService.updateUserStats(currentUser);
        populateStoreTabs(); // Refresh the display
    }

    private void applyAvatar(StoreItem item) {
        currentUser.setAvatarId(item.getResourcePath());
        UserService.updateUserStats(currentUser);

        // Update the avatar in the main game
        HangmanController hangmanController = SessionManager.getInstance().getHangmanController();
        if (hangmanController != null) {
            hangmanController.updateProfileIcon();
        }
    }

    private void applyKeyboardSkin(StoreItem item) {
        HangmanController hangmanController = SessionManager.getInstance().getHangmanController();
        if (hangmanController != null) {
            hangmanController.applyKeyboardStyle(item.getResourcePath());

            // Update local user reference
            currentUser.setCurrentKeyboardStyle(item.getResourcePath());
            updateUserInfo();
        }
    }

    private void applyHangmanLook(StoreItem item) {
        HangmanController hangmanController = SessionManager.getInstance().getHangmanController();
        if (hangmanController != null) {
            hangmanController.applyHangmanStyle(item.getResourcePath());
        }
    }
}