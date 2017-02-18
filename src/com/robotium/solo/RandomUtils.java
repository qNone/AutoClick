package com.robotium.solo;

import java.util.Random;

public class RandomUtils {

    private static final String LETTER_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER_CHAR = "0123456789";
    private static final String REGULAR_CHAR = "^$()[]{}.+?*/|\\";
    private static final String ALL_CHAR = LETTER_CHAR + NUMBER_CHAR + REGULAR_CHAR;
    private static final String ALL_CHAR_NOT_SYMBOL = LETTER_CHAR + NUMBER_CHAR;

    static String getRandomPhone(){
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        sb.append("138");
        for (int i = 0; i < 8; i++) {
            sb.append(NUMBER_CHAR.charAt(random.nextInt(NUMBER_CHAR.length() - 1)));
        }
        return sb.toString();
    }

    static String getRandomNumber(int length){
        if (length <= 0) return NUMBER_CHAR;
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBER_CHAR.charAt(random.nextInt(NUMBER_CHAR.length() - 1)));
        }
        return sb.toString();
    }

    static String getRandomText(int length){
        if (length <= 0) length = 10;
        if (length >= 200) length = 200;
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALL_CHAR.charAt(random.nextInt(ALL_CHAR.length() - 1)));
        }
        return sb.toString();
    }

    static String getRandomEmail(int length){
        if (length < 5) return "";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length - 2; i++) {
            sb.append(ALL_CHAR_NOT_SYMBOL.charAt(random.nextInt(ALL_CHAR_NOT_SYMBOL.length() - 1)));
        }
        sb.insert((length - 2) / 3, "@");
        sb.insert((length - 2) / 3 * 2 + 1, ".");
        return sb.toString();
    }

    static String getRandomUrl(int length){
        if (length < 10) return "https://www.baidu.com/";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        sb.append("http://");
        for (int i = 0; i < length - 7; i++) {
            sb.append(ALL_CHAR_NOT_SYMBOL.charAt(random.nextInt(ALL_CHAR_NOT_SYMBOL.length() - 1)));
        }
        sb.insert(length - 2, ".");
        return sb.toString();
    }


    public static void main(String[] args) {
        // TODO Auto-generated method stub
        for (int i=0;i <10000;i++){
            System.out.println(getRandomNumber(2));
        }
    }
}
