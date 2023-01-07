package net.mirwaldt.streams;

import net.mirwaldt.streams.util.SumUtil;

import java.util.Arrays;
import java.util.stream.IntStream;

public class ParallelStream_02_Range_LargerSteps {
    public static void main(String[] args) {
        long largerStepsParallelStream = IntStream
                .rangeClosed(1, 10_000_000)
                .map(i -> i * 3) // in steps of 3, i.e. 3, 6, 9, ...
                .filter(i -> SumUtil.sumOfDigits(i) == 12)
                .parallel()
                .count();
        System.out.println(largerStepsParallelStream);
    }
}
