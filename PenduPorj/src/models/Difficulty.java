package models;

public enum Difficulty {
    EASY("Facile (3-4 lettres)"),
    MEDIUM("Moyen (5-6 lettres)"),
    HARD("Difficile (7-8 lettres)"),
    EXTRA_HARD("Expert (+8 lettres)");

    private final String displayName;

    Difficulty(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}