package Services;

import java.util.ResourceBundle;

public class LanguageManager {
    private static LanguageManager instance;
    private String currentLanguage = "en";
    private ResourceBundle resourceBundle;

    private LanguageManager() {
        loadResourceBundle();
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void setLanguage(String language) {
        this.currentLanguage = language;
        loadResourceBundle();
    }

    private void loadResourceBundle() {
        resourceBundle = ResourceBundle.getBundle("messages", new java.util.Locale(currentLanguage));
    }

    public String getString(String key) {
        return resourceBundle.getString(key);
    }

    public void toggleLanguage() {
        currentLanguage = currentLanguage.equals("en") ? "fr" : "en";
        loadResourceBundle();
    }
}