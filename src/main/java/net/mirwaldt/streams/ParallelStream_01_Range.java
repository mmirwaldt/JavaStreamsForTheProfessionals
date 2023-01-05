package net.mirwaldt.streams;

public class ParallelStream_01_Range {
    public static void main(String[] args) {

    }



    public static long checkSum(long n) {
        long result = 0;
        long remaining = n;
        while (0 < remaining) {
            long remainder = remaining % 10;
            result += remainder;
            remaining = (remaining - remainder) / 10;
        }
        return result;
    }
}
