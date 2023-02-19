package net.mirwaldt.streams.experimental;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

import static java.math.BigInteger.ONE;
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.KARATSUBA_THRESHOLD_IN_BITS;

/*
    The perfect split strategy for FJP is to split
    - all factors to groups large enough for Karatsuba multiplication
    - all intermediate results to groups of 3 so that Tom-Cook multiplication can be used
    - on the fly and not just at the beginning like spliterators

    Is it really perfect?
    No, but it is nearly perfect.
    If an intermediate result has many more than 2560 bits, than too many additional bits might be missing in another
    intermediate result to reach the 2560 bits.

    Summary of results:
    I haven't been able to get a fester solution than factorialParallelInForkJoinPoolParallelMultiply()
    of Benchmark_11_ParallelFactorial. However, the result was at least as fast as
    factorialParallelInForkJoinPoolParallelMultiply().
 */

@SuppressWarnings("DuplicatedCode")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_24_PerfectSplitStrategy {

    /*
    Benchmark                                                                       Mode  Cnt   Score   Error  Units
    Benchmark_24_PerfectSplitStrategy.perfectTomCookKaratsubaFactorialForkJoinPool  avgt   25  16.554 ± 0.013  ms/op
     */
    int N = 100_000;

    int[] splits = splits(N);

    @Benchmark
    public BigInteger perfectTomCookKaratsubaFactorialForkJoinPool() {
        return perfectTomCookKaratsubaFactorialForkJoinPool(splits, BigInteger::parallelMultiply);
    }

    public static BigInteger perfectTomCookKaratsubaFactorialForkJoinPool(int n, BinaryOperator<BigInteger> multiply) {
        int[] splits = splits(n);
        return ForkJoinPool.commonPool().invoke(new TomCookKaratsubaFactorialTask(splits, multiply));
    }

    public static BigInteger perfectTomCookKaratsubaFactorialForkJoinPool(
            int[] splits, BinaryOperator<BigInteger> multiply) {
        return ForkJoinPool.commonPool().invoke(new TomCookKaratsubaFactorialTask(splits, multiply));
    }

    static int[] splits(int n) {
//        int additionalBits = 0;
        List<Integer> result = new ArrayList<>();
        BigInteger bigInt = ONE;
        int i = 2;
        for (; i <= n; i++) {
            bigInt = bigInt.multiply(BigInteger.valueOf(i));
            if (KARATSUBA_THRESHOLD_IN_BITS <= bigInt.bitLength()) {
//                System.out.println(bigInt.bitLength());
//                additionalBits += bigInt.bitLength() - KARATSUBA_THRESHOLD_IN_BITS;
                result.add(i + 1); // + 1 for exclusive ends and inclusive starts
                bigInt = ONE;
            }
        }
        if (result.isEmpty() || result.get(result.size() - 1) < n + 1) {
//            System.out.println(bigInt.bitLength());
            result.add(n + 1);
        }
//        System.out.println(additionalBits); // 4305 > 2560 (one Karatsuba number) => 1745 bits too many
        return result.stream().mapToInt(j -> j).toArray();
    }

    /*
        How to implement the perfect strategy:
        - split binary as long as more than 6 split positions
        - split into groups of 3 if only 6 split positions are remaining
        - split groups of 3 into groups of 1 for one Karatsuba-large factor
        only for a split position array which is a multiple of 6.
        Just apply binary splitting for the rest and finally into groups of 1 for one Karatsuba-large factor.
     */
    public static class TomCookKaratsubaFactorialTask extends RecursiveTask<BigInteger> {
        private final int start;
        private final int end;
        private final int splitStart;
        private final int splitEnd;
        private final int[] karatsubaSplits;
        private final BinaryOperator<BigInteger> multiply;

        public TomCookKaratsubaFactorialTask(int[] karatsubaSplits, BinaryOperator<BigInteger> multiply) {
            this(-1, -2, 0, karatsubaSplits.length, karatsubaSplits, multiply);
        }

        private TomCookKaratsubaFactorialTask(
                int splitStart, int splitEnd, int[] karatsubaSplits, BinaryOperator<BigInteger> multiply) {
            this(-1, -2, splitStart, splitEnd, karatsubaSplits, multiply);
        }

        private TomCookKaratsubaFactorialTask(
                int start, int end, BinaryOperator<BigInteger> multiply) {
            this(start, end, -1, -1, null, multiply);
        }

        private TomCookKaratsubaFactorialTask(
                int start, int end, int splitStart, int splitEnd, int[] karatsubaSplits, BinaryOperator<BigInteger> multiply) {
            this.start = start;
            this.end = end;
            this.splitStart = splitStart;
            this.splitEnd = splitEnd;
            this.karatsubaSplits = karatsubaSplits;
            this.multiply = multiply;
        }

        @Override
        protected BigInteger compute() {
            if (start == -1) {
                int length = splitEnd - splitStart;
                if (splitStart <= karatsubaSplits.length - 1 && karatsubaSplits.length - 1 < splitEnd){
                    System.out.println();
                }
                if (length == 1 || karatsubaSplits.length == 1) { // for default multiplication
                    int start = (0 < splitStart) ? karatsubaSplits[splitStart - 1] : 2;
                    int end = karatsubaSplits[splitStart];
                    return calculateToKaratsuba(start, end);
                } else if (length == 2) { // splits for Karatsuba-multiplications
                    int leftStart = (0 < splitStart) ? karatsubaSplits[splitStart - 1] : 2;
                    int leftEnd = karatsubaSplits[splitStart];
                    int rightStart = leftEnd;
                    int rightEnd = karatsubaSplits[splitStart + 1];
                    return multiply.apply(calculateToKaratsuba(leftStart, leftEnd), calculateToKaratsuba(rightStart, rightEnd));
                } else if (length % 3 == 0 && 6 <= length && length % 6 != 0) { // splits for Karatsuba-multiplications
                    int multipleOf6 = length - 3;
                    int leftLength = multipleOf6 / 2 + 3;
                    TomCookKaratsubaFactorialTask leftTask =
                            new TomCookKaratsubaFactorialTask(splitStart, splitStart + leftLength, karatsubaSplits, multiply);
                    leftTask.fork();
                    TomCookKaratsubaFactorialTask rightTask =
                            new TomCookKaratsubaFactorialTask(splitStart + leftLength, splitEnd, karatsubaSplits, multiply);
                    return multiply.apply(rightTask.compute(), leftTask.join());
                } else {
                    int halfLength = length >>> 1;
                    TomCookKaratsubaFactorialTask leftTask =
                            new TomCookKaratsubaFactorialTask(splitStart, splitStart + halfLength, karatsubaSplits, multiply);
                    leftTask.fork();
                    TomCookKaratsubaFactorialTask rightTask =
                            new TomCookKaratsubaFactorialTask(splitStart + halfLength, splitEnd, karatsubaSplits, multiply);
                    return multiply.apply(rightTask.compute(), leftTask.join());
                }
            } else {
                return calculateToKaratsuba(start, end);
            }
        }

        private BigInteger calculateToKaratsuba(int start, int end) {
            int length = end - start;
            if (length == 1) {
                return BigInteger.valueOf(start);
            } else if (length == 2) {
                return multiply.apply(BigInteger.valueOf(start), BigInteger.valueOf(start + 1));
            } else {
                int halfLength = length >>> 1;
                TomCookKaratsubaFactorialTask leftTask =
                        new TomCookKaratsubaFactorialTask(start, start + halfLength, multiply);
                leftTask.fork();
                TomCookKaratsubaFactorialTask rightTask =
                        new TomCookKaratsubaFactorialTask(start + halfLength, end, multiply);
                return multiply.apply(rightTask.compute(), leftTask.join());
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_24_PerfectSplitStrategy.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
