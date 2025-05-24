package models;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class WordDictionary {
    private Map<String, List<WordWithHints>> themedWords;
    private Random random;

    public static class WordWithHints {
        private String word;
        private List<String> hints;

        public WordWithHints(String word, List<String> hints) {
            this.word = word;
            this.hints = hints;
        }

        public String getWord() {
            return word;
        }

        public List<String> getHints() {
            return hints;
        }
    }

    public WordDictionary(InputStream wordStream) throws IOException {
        this.themedWords = new HashMap<>();
        this.random = new Random();
        loadCate(wordStream);
    }

    private void loadCate(InputStream wordStream) throws IOException {
        Scanner scanner = new Scanner(wordStream);
        String currentTheme = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("[") && line.endsWith("]")) {
                currentTheme = line.substring(1, line.length() - 1);
                themedWords.put(currentTheme, new ArrayList<>());
            } else if (currentTheme != null && !line.isEmpty()) {
                String[] parts = line.split("\\|");
                String word = parts[0];
                List<String> hints = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    hints.add(parts[i].trim());
                }
                themedWords.get(currentTheme).add(new WordWithHints(word, hints));
            }
        }
        scanner.close();
    }

    public WordWithHints getRandomWordWithHints(String theme, Difficulty difficulty) {
        List<WordWithHints> words = themedWords.get(theme);
        if (words == null || words.isEmpty()) {
            throw new IllegalArgumentException("Theme not found or empty: " + theme);
        }

        List<WordWithHints> filteredWords = filterWordsByDifficulty(words, difficulty);
        if (filteredWords.isEmpty()) {
            return words.get(random.nextInt(words.size()));
        }

        return filteredWords.get(random.nextInt(filteredWords.size()));
    }

    private List<WordWithHints> filterWordsByDifficulty(List<WordWithHints> words, Difficulty difficulty) {
        List<WordWithHints> filtered = new ArrayList<>();
        for (WordWithHints wordWithHints : words) {
            int length = wordWithHints.getWord().length();
            switch (difficulty) {
                case EASY:
                    if (length >= 3 && length <= 4) filtered.add(wordWithHints);
                    break;
                case MEDIUM:
                    if (length >= 5 && length <= 6) filtered.add(wordWithHints);
                    break;
                case HARD:
                    if (length >= 7 && length <= 8) filtered.add(wordWithHints);
                    break;
                case EXTRA_HARD:
                    if (length > 8) filtered.add(wordWithHints);
                    break;
            }
        }
        return filtered;
    }

    public Set<String> getAvailableThemes() {
        return themedWords.keySet();
    }
}