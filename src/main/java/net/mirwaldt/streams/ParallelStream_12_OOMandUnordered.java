package net.mirwaldt.streams;

import java.util.stream.IntStream;

import static net.mirwaldt.streams.util.SumUtil.sumOfDigits;

public class ParallelStream_12_OOMandUnordered {
    public static void main(String[] args) {
        // Doesn't lead to any OOM because unordered() was called
        System.out.println(IntStream
                .iterate(0, i -> i + 1)
                .unordered()
                .skip(2)
                .limit(10_000_000)
                .filter(i -> sumOfDigits(i) <= 20)
                .parallel()
                .count());

        // Doesn't lead to any OOM because skip() is called AFTER limit()
        System.out.println(IntStream
                .iterate(0, i -> i + 1)
                .limit(10_000_000)
                .skip(2)
                .filter(i -> sumOfDigits(i) <= 20)
                .parallel()
                .count());

        // Warning: leads to OOM!
        // See https://stackoverflow.com/q/75012814/2023533 for more information
        System.out.println(IntStream
                .iterate(0, i -> i + 1)
                .skip(2)
                .limit(10_000_000)
                .filter(i -> sumOfDigits(i) <= 20)
                .parallel()
                .count());
    }
}
