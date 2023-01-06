package net.mirwaldt.streams;

import net.mirwaldt.streams.util.SumUtil;

import java.util.Arrays;
import java.util.stream.IntStream;

public class ParallelStream_03_Range_DescendingOrder {
    public static void main(String[] args) {
        int[] descendingOrderParallelStream = IntStream
                .rangeClosed(1, 10_000_000)
                .map(i -> 10_000_000 - i + 1) // reverses the order
                .filter(i -> SumUtil.sumOfDigits(i) == 40)
                .limit(100)
                .parallel()
                .toArray();
        System.out.println(Arrays.toString(descendingOrderParallelStream));
    }
}
