package models;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class WordDictionary {
    private Map<String, List<String>> themedWords;
    private Random random;

    public WordDictionary(InputStream wordStream) throws IOException {
        this.themedWords = new HashMap<>();
        this.random = new Random();
        loadThemes(wordStream);
    }

    private void loadThemes(InputStream wordStream) throws IOException {
        Scanner scanner = new Scanner(wordStream);
        String currentTheme = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("[") && line.endsWith("]")) {
                currentTheme = line.substring(1, line.length() - 1);
                themedWords.put(currentTheme, new ArrayList<>());

            } else if (currentTheme != null && !line.isEmpty()) {
                themedWords.get(currentTheme).add(line);
            }
        }
        scanner.close();
    }

    public String getRandomWord(String theme) {
        List<String> words = themedWords.get(theme);
        if (words == null || words.isEmpty()) {
            throw new IllegalArgumentException("Theme not found or empty: " + theme);
        }
        return words.get(random.nextInt(words.size())).toUpperCase();
    }

    public Set<String> getAvailableThemes() {
        return themedWords.keySet();
    }
}