package com.yiyangzhu.yydictionary;

import java.util.ArrayList;
import java.util.List;

/**
 * Represnets a word and its definitions from Internet.
 */
public class Word {

    private String word;
    private List<String> definitions;

    public Word(String word) {
        this(word, new ArrayList<String>());
    }

    public Word(String word, List<String> definitions) {
        this.word = word;
        this.definitions = definitions;
    }

    public String getWord() {
        return word;
    }

    public List<String> getDefinitions() {
        return definitions;
    }

    @Override
    public String toString() {
        return "Word{" +
                "word='" + word + '\'' +
                ", definitions=" + definitions +
                '}';
    }
}
