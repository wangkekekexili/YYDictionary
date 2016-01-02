package com.yiyangzhu.yydictionary;

import org.joda.time.LocalDate;

import java.util.Random;

/**
 * Created by kewang on 1/1/16.
 */
public class VocabularyBuilder {

    public static LocalDate nextDate(LocalDate current, int iteration) {
        LocalDate next = null;
        switch (iteration) {
            case 1:
                next = current.plusDays(1);
                break;
            case 2:
                next = current.plusDays(1);
                break;
            case 3:
                next = current.plusDays(2);
                break;
            case 4:
                next = current.plusDays(3);
                break;
            case 5:
                next = current.plusDays(5);
                break;
            case 6:
                next = current.plusDays(8);
                break;
            case 7:
                next = current.plusDays(13);
                break;
            default:
                Random random = new Random();
                next = current.plusDays(random.nextInt(30) + 1);
                break;
        }
        return next;
    }

}
