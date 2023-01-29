package net.mirwaldt.streams.util;

public class CollatzUtil {
    public static int follow(int n) {
        int i = n;
        int result = 0;
        while(1 < i) {
            i = collatz(i);
            result++;
        }
        return result;
    }

    private static int collatz(int i) {
        if(i % 2 == 0) {
            return i / 2;
        } else {
            return 3 * i + 1;
        }
    }
}
