package net.mirwaldt.streams;

import net.mirwaldt.streams.util.SumUtil;

import java.util.Arrays;
import java.util.stream.IntStream;

public class ParallelStream_03_RangeClosed_DescendingOrder {
    public static void main(String[] args) {
        int[] top10ForSumOfDigits55 = IntStream
                .rangeClosed(1, 150_000_000)
                .map(i -> 150_000_000 - i + 1) // reverses the order
                .filter(i -> SumUtil.sumOfDigits(i) == 55)
                .filter(i -> (i % 20021) == 0)
                .limit(10)
                .parallel()
                .toArray();
        System.out.println(Arrays.toString(top10ForSumOfDigits55));
    }
}
