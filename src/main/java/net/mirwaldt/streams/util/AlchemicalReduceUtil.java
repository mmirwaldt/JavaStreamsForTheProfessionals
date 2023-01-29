package net.mirwaldt.streams.util;

import java.util.Random;

import static java.lang.Character.*;

public class AlchemicalReduceUtil {
    public static String createString(int n) {
        String[] letters = new String[] { "a", "b", "c", "A", "B", "C", "aA", "Aa", "bB", "Bb", "cC", "Cc"};
        StringBuilder polymer = new StringBuilder();
        Random random = new Random(12345);
        for (int i = 0; i < n; i++) {
            polymer.append(letters[random.nextInt(letters.length)]);
        }
        return polymer.toString();
    }

    public static String reduce(String result, char c) {
        return (0 < result.length()
                && result.charAt(result.length() - 1) == toOppositeCase(c))
                ? result.substring(0, result.length() - 1)
                : result + c;
    }

    public static String combine(String left, String right) {
        String result = left;
        for (int i = 0; i < right.length(); i++) {
            result = reduce(result, right.charAt(i));
        }
        return result;
    }

    public static void reduce(StringBuilder result, char c) {
        if (0 < result.length()
                && result.charAt(result.length() - 1) == toOppositeCase(c)) {
            result.delete(result.length() - 1, result.length());
        } else {
            result.append(c);
        }
    }

    public static void combine(StringBuilder left, StringBuilder right) {
        for (int i = 0; i < right.length(); i++) {
            reduce(left, right.charAt(i));
        }
    }

    public static char toOppositeCase(char c) {
        return isLowerCase(c) ? toUpperCase(c) : toLowerCase(c);
    }

    /**
     * scans patterns of the input string but ignoring patterns which are the results after reductions
     * @param polymer the input string
     * @return the number of patterns
     */
    public static int scan(String polymer) {
        final String[] patterns = new String[]{"aA", "Aa", "bB", "Bb", "cC", "Cc"};
        int result = 0;
        for (String pattern : patterns) {
            for (int i = 0; i < polymer.length(); i++) {
                i = polymer.indexOf(pattern, i);
                if(i == -1) {
                    break;
                } else {
                    result++;
                }
            }
        }
        return result;
    }
}
