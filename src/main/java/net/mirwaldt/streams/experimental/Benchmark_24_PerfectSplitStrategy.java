package net.mirwaldt.streams.experimental;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
Benchmark_24_PerfectSplitStrategy.perfectTomCookKaratsubaFactorialForkJoinPool  avgt   25  16.494 ± 0.059  ms/op
     */
    int N = 100_000;

    int[] factors = factors(N, 1002); // seed 1002 ensures latest group of factors grows to Karatsuba threshold
    int[] splits = splits(N, factors);

    @Benchmark
    public BigInteger perfectTomCookKaratsubaFactorialForkJoinPool() {
        return perfectTomCookKaratsubaFactorialForkJoinPool(factors, splits, BigInteger::parallelMultiply);
    }

    public static BigInteger perfectTomCookKaratsubaFactorialForkJoinPool(int n, BinaryOperator<BigInteger> multiply) {
        int[] factors = factors(n, 1002);
        int[] splits = splits(n, factors);
        return ForkJoinPool.commonPool().invoke(new TomCookKaratsubaFactorialTask(factors, splits, multiply));
    }

    public static BigInteger perfectTomCookKaratsubaFactorialForkJoinPool(
            int[] factors, int[] splits, BinaryOperator<BigInteger> multiply) {
        return ForkJoinPool.commonPool().invoke(new TomCookKaratsubaFactorialTask(factors, splits, multiply));
    }

    static int[] splits(int n, int[] factors) {
        List<Integer> result = new ArrayList<>();
        BigInteger bigInt = ONE;
        int i = 0;
        for (; i < n; i++) {
            int j = factors[i];
            bigInt = bigInt.multiply(BigInteger.valueOf(j));
            if (KARATSUBA_THRESHOLD_IN_BITS <= bigInt.bitLength()) {
                result.add(i + 1); // + 1 for exclusive ends and inclusive starts
                bigInt = ONE;
            }
        }
        if (result.isEmpty() || result.get(result.size() - 1) < n) {
            result.add(n);
        }
        return result.stream().mapToInt(j -> j).toArray();
    }

    static int[] factors(int n, int seed) { // use a randomized array of ints to reduce the waste of bits
        Random random = new Random(seed);
        List<Integer> factors = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            factors.add(i);
        }
        Collections.shuffle(factors, random);
        return factors.stream().mapToInt(j -> j).toArray();
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
        private final int[] karatsubaFactors;
        private final int[] karatsubaSplits;
        private final BinaryOperator<BigInteger> multiply;

        public TomCookKaratsubaFactorialTask(
                int[] karatsubaFactors, int[] karatsubaSplits, BinaryOperator<BigInteger> multiply) {
            this(karatsubaFactors, -1, -2, 0, karatsubaSplits.length, karatsubaSplits, multiply);
        }

        private TomCookKaratsubaFactorialTask(
                int[] karatsubaFactors, int splitStart, int splitEnd, int[] karatsubaSplits, BinaryOperator<BigInteger> multiply) {
            this(karatsubaFactors, -1, -2, splitStart, splitEnd, karatsubaSplits, multiply);
        }

        private TomCookKaratsubaFactorialTask(
                int[] karatsubaFactors, int start, int end, BinaryOperator<BigInteger> multiply) {
            this(karatsubaFactors, start, end, -1, -1, null, multiply);
        }

        private TomCookKaratsubaFactorialTask(
                int[] karatsubaFactors, int start, int end, int splitStart, int splitEnd,
                int[] karatsubaSplits, BinaryOperator<BigInteger> multiply) {
            this.karatsubaFactors = karatsubaFactors;
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
                if (length == 1 || karatsubaSplits.length == 1) { // for default multiplication
                    int start = (0 < splitStart) ? karatsubaSplits[splitStart - 1] : 0;
                    int end = karatsubaSplits[splitStart];
                    return calculateToKaratsuba(start, end);
                }  else if (length % 3 == 0 && 6 <= length && length % 6 != 0) { // splits for Karatsuba-multiplications
                    int multipleOf6 = length - 3;
                    int leftLength = multipleOf6 / 2 + 3;
                    TomCookKaratsubaFactorialTask leftTask =
                            new TomCookKaratsubaFactorialTask(karatsubaFactors, splitStart, splitStart + leftLength, karatsubaSplits, multiply);
                    leftTask.fork();
                    TomCookKaratsubaFactorialTask rightTask =
                            new TomCookKaratsubaFactorialTask(karatsubaFactors, splitStart + leftLength, splitEnd, karatsubaSplits, multiply);
                    return multiply.apply(rightTask.compute(), leftTask.join());
                } else {
                    int halfLength = length >>> 1;
                    TomCookKaratsubaFactorialTask leftTask =
                            new TomCookKaratsubaFactorialTask(karatsubaFactors, splitStart, splitStart + halfLength, karatsubaSplits, multiply);
                    leftTask.fork();
                    TomCookKaratsubaFactorialTask rightTask =
                            new TomCookKaratsubaFactorialTask(karatsubaFactors, splitStart + halfLength, splitEnd, karatsubaSplits, multiply);
                    return multiply.apply(rightTask.compute(), leftTask.join());
                }
            } else {
                return calculateToKaratsuba(start, end);
            }
        }

        private BigInteger calculateToKaratsuba(int start, int end) {
            int length = end - start;
            if (length == 1) {
                return BigInteger.valueOf(karatsubaFactors[start]);
            } else if (length == 2) {
                return multiply.apply(BigInteger.valueOf(karatsubaFactors[start]),
                        BigInteger.valueOf(karatsubaFactors[start + 1]));
            } else {
                int halfLength = length >>> 1;
                TomCookKaratsubaFactorialTask leftTask =
                        new TomCookKaratsubaFactorialTask(karatsubaFactors, start, start + halfLength, multiply);
                leftTask.fork();
                TomCookKaratsubaFactorialTask rightTask =
                        new TomCookKaratsubaFactorialTask(karatsubaFactors, start + halfLength, end, multiply);
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
