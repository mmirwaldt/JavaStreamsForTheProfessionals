package net.mirwaldt.streams;

import net.mirwaldt.streams.util.SumUtil;

import java.util.stream.IntStream;

public class ParallelStream_04_FindAny {
    public static void main(String[] args) {
        int findFirst = IntStream
                .rangeClosed(1, 100_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) == 60)
                .parallel()
                .findFirst().orElse(-1);
        System.out.println(findFirst);

        int findAny = IntStream
                .rangeClosed(1, 100_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) == 60)
                .parallel()
                .findAny().orElse(-1);
        System.out.println(findAny);
    }

    public static int findFirst() {
        return IntStream
                .rangeClosed(1, 100_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) == 60)
                .parallel()
                .findFirst().orElse(-1);
    }

    public static int findAny() {
        return IntStream
                .rangeClosed(1, 100_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) == 60)
                .parallel()
                .findAny().orElse(-1);
    }
}
