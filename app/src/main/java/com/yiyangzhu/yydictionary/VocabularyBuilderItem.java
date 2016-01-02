package com.yiyangzhu.yydictionary;

import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kewang on 1/1/16.
 */
public class VocabularyBuilderItem {
    private String word;
    private LocalDate nextDate;
    private int iteration;

    public VocabularyBuilderItem(){}

    public VocabularyBuilderItem(String word) {
        this.word = word;
        this.nextDate = LocalDate.now();
        this.iteration = 1;
    }

    public void nextIteration() {
        this.nextDate = VocabularyBuilder.nextDate(nextDate, iteration);
        iteration++;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setNextDate(String nextDate) {
        this.nextDate = LocalDate.parse(nextDate);
    }

    public void setNextDate(LocalDate nextDate) {
        this.nextDate = nextDate;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public LocalDate getNextDate() {
        return nextDate;
    }

    public int getIteration() {
        return iteration;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> value = new HashMap<>();
        value.put("word", word);
        value.put("nextDate", nextDate.toString());
        value.put("iteration", iteration);
        return value;
    }

    @Override
    public String toString() {
        return "VocabularyBuilderItem{" +
                "word='" + word + '\'' +
                ", nextDate=" + nextDate +
                ", iteration=" + iteration +
                '}';
    }
}
