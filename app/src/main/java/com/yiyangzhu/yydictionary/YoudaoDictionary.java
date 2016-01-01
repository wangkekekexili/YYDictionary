package com.yiyangzhu.yydictionary;

import java.util.HashMap;
import java.util.Map;

/**
 * This contains a dictionary from firebase which saves
 * definitions that were looked up before.
 */
public class YoudaoDictionary {
    private static Map<String, Word> dictionary = new HashMap<>();

    synchronized public static void add(Word word) {
        dictionary.put(word.getWord(), word);
    }

    synchronized public static Word get(String word) {
        return dictionary.get(word);
    }

}
