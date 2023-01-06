package net.mirwaldt.streams;

import net.mirwaldt.streams.util.SumUtil;

import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class ParallelStream_01_Range_NoIterate {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        long flawedParallelStream =
                IntStream.iterate(1, i -> i + 1)
                .limit(10_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) <= 40)
                .parallel()
                .count();
        System.out.println(flawedParallelStream);

        long betterParallelStream = IntStream
                .rangeClosed(1, 10_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) <= 40)
                .parallel()
                .count();
        System.out.println(betterParallelStream);
    }

    public static long flawedParallelStream() {
        return IntStream
                .iterate(1, i -> i + 1)
                .limit(10_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) <= 40)
                .parallel()
                .count();
    }

    public static long betterParallelStream() {
        return IntStream
                .rangeClosed(1, 10_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) <= 40)
                .parallel()
                .count();
    }
}
