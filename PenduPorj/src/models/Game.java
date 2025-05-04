package models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game {
    private String wordToGuess;
    private Set<Character> guessedLetters;
    private int remainingAttempts;
    private final int maxAttempts = 8;
    private List<String> hints;
    private int hintsUsed = 0;

    public Game(String wordToGuess, List<String> hints) {
        this.wordToGuess = wordToGuess.toUpperCase();
        this.guessedLetters = new HashSet<>();
        this.remainingAttempts = maxAttempts;
        this.hints = hints;
    }

    public String useHint() {
        if (hintsUsed < hints.size()) {
            return hints.get(hintsUsed++);
        }
        return "Pas d'autres indices disponibles";
    }

    public int getRemainingHints() {
        return hints.size() - hintsUsed;
    }

    public boolean guessLetter(char letter) {
        letter = Character.toUpperCase(letter);
        if (guessedLetters.contains(letter)) {
            return false;
        }

        guessedLetters.add(letter);
        if (!wordToGuess.contains(String.valueOf(letter))) {
            remainingAttempts--;
            return false;
        }
        return true;
    }

    public String getCurrentState() {
        StringBuilder currentState = new StringBuilder();
        for (char c : wordToGuess.toCharArray()) {
            if (guessedLetters.contains(c)) {
                currentState.append(c);
            } else {
                currentState.append("_");
            }
            currentState.append(" ");
        }
        return currentState.toString().trim();
    }

    public boolean isWon() {
        for (char c : wordToGuess.toCharArray()) {
            if (!guessedLetters.contains(c)) {
                return false;
            }
        }
        return true;
    }

    public boolean isLost() {
        return remainingAttempts <= 0;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public String getWordToGuess() {
        return wordToGuess;
    }

    public Set<Character> getGuessedLetters() {
        return guessedLetters;
    }
}