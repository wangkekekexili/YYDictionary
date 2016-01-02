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
        StringBuilder sb = new StringBuilder();
        sb.append(word);
        sb.append("\n");
        for (int index = 0; index < definitions.size(); index++) {
            sb.append(String.format("%d. %s\n", index+1, definitions.get(index)));
        }
        return sb.toString();
    }
}
