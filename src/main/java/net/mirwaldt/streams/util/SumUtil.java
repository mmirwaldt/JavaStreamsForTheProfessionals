package net.mirwaldt.streams.util;

public class SumUtil {
    public static long sumOfDigits(long n) {
        long result = 0;
        long remaining = n;
        while (0 < remaining) {
            result += remaining % 10;
            remaining = remaining / 10;
        }
        return result;
    }
}
