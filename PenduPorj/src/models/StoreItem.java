package models;

public class StoreItem {
    private String id;
    private String name;
    private String description;
    private int price;
    private String type; // "KEYBOARD_SKIN", "HANGMAN_LOOK", "AVATAR", etc.
    private String imagePath;
    private String resourcePath;

    public StoreItem(String id, String name, String description, int price,
                     String type, String imagePath, String resourcePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.type = type;
        this.imagePath = imagePath;
        this.resourcePath = resourcePath;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPrice() { return price; }
    public String getType() { return type; }
    public String getImagePath() { return imagePath; }
    public String getResourcePath() { return resourcePath; }
}