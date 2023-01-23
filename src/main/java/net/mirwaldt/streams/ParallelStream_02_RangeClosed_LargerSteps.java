package net.mirwaldt.streams;

import net.mirwaldt.streams.util.SumUtil;

import java.util.stream.IntStream;

public class ParallelStream_02_RangeClosed_LargerSteps {
    public static void main(String[] args) {
        long countMultiplesOf3AndSumOfDigits12 = IntStream
                .rangeClosed(1, 200_000_000)
                .map(i -> i * 3) // in steps of 3, i.e. 3, 6, 9, ...
                .filter(i -> SumUtil.sumOfDigits(i) == 12)
                .parallel()
                .count();
        System.out.println(countMultiplesOf3AndSumOfDigits12);
    }
}
