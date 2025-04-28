package models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordDictionary {
    private List<String> words;
    private Random random;

    public WordDictionary(InputStream wordStream) throws IOException {
        this.words = new ArrayList<>();
        this.random = new Random();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(wordStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim());
            }
        }
    }

    public String getRandomWord() {
        return words.get(random.nextInt(words.size())).toUpperCase();
    }

    public Random getRandom() {
        return random;
    }
}