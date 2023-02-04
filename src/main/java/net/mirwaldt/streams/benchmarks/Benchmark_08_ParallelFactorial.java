package net.mirwaldt.streams.benchmarks;

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
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@SuppressWarnings("DuplicatedCode")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_08_ParallelFactorial {
    /*
Benchmark                                                                          Mode  Cnt   Score   Error  Units
Benchmark_08_ParallelFactorial.factorialParallelInForkJoinPoolParallelMultiply     avgt   25  16.631 ± 0.009  ms/op
Benchmark_08_ParallelFactorial.factorialParallelInForkJoinPoolSequentialMultiply   avgt   25  29.983 ± 0.456  ms/op
Benchmark_08_ParallelFactorial.factorialParallelStreamParallelMultiply             avgt   25  39.139 ± 0.041  ms/op
Benchmark_08_ParallelFactorial.factorialParallelStreamSequentialMultiply           avgt   25  53.641 ± 0.168  ms/op
Benchmark_08_ParallelFactorial.karatsubaFactorialForkJoinPoolParallelMultiply      avgt   25  17.191 ± 0.015  ms/op
Benchmark_08_ParallelFactorial.karatsubaFactorialForkJoinPoolSequentialMultiply    avgt   25  30.619 ± 0.446  ms/op
Benchmark_08_ParallelFactorial.karatsubaFactorialParallelStreamParallelMultiply    avgt   25  21.382 ± 0.050  ms/op
Benchmark_08_ParallelFactorial.karatsubaFactorialParallelStreamSequentialMultiply  avgt   25  34.700 ± 0.148  ms/op
     */

    public int N = 100_000;

    @Benchmark
    public BigInteger factorialParallelStreamSequentialMultiply() {
        return factorialParallelStream(N, BigInteger::multiply);
    }

    @Benchmark
    public BigInteger factorialParallelStreamParallelMultiply() {
        return factorialParallelStream(N, BigInteger::parallelMultiply);
    }

    @Benchmark
    public BigInteger factorialParallelInForkJoinPoolSequentialMultiply() {
        return factorialParallelInForkJoinPool(N, BigInteger::multiply);
    }

    @Benchmark
    public BigInteger factorialParallelInForkJoinPoolParallelMultiply() {
        return factorialParallelInForkJoinPool(N, BigInteger::parallelMultiply);
    }

    @Benchmark
    public BigInteger karatsubaFactorialParallelStreamSequentialMultiply() {
        return karatsubaFactorialParallelStream2(N, BigInteger::multiply);
    }

    @Benchmark
    public BigInteger karatsubaFactorialParallelStreamParallelMultiply() {
        return karatsubaFactorialParallelStream2(N, BigInteger::parallelMultiply);
    }

    @Benchmark
    public BigInteger karatsubaFactorialForkJoinPoolSequentialMultiply() {
        return karatsubaFactorialForkJoinPool(N, BigInteger::multiply);
    }

    @Benchmark
    public BigInteger karatsubaFactorialForkJoinPoolParallelMultiply() {
        return karatsubaFactorialForkJoinPool(N, BigInteger::parallelMultiply);
    }

    public static BigInteger factorialParallelStream(int n, BinaryOperator<BigInteger> multiply) {
        return LongStream.rangeClosed(2, n)
                .mapToObj(BigInteger::valueOf)
                .parallel()
                .reduce(multiply)
                .orElse(BigInteger.ONE);
    }

    public static BigInteger factorialParallelInForkJoinPool(int n, BinaryOperator<BigInteger> multiply) {
        return ForkJoinPool.commonPool().invoke(new FactorialTask(1, n + 1, 100, multiply));
    }

    public static BigInteger karatsubaFactorialForkJoinPool(int n,BinaryOperator<BigInteger> multiply) {
        return ForkJoinPool.commonPool()
                .invoke(new KaratsubaFactorialTask(1, n + 1, 1000, multiply));
    }

    /*
        This is my first sequential solution for multiplying all factors of factorial in a karatsuba-friendly way.
     */
    BigInteger calc(int n) {
        BigInteger result = BigInteger.ONE;
        List<BigInteger> results = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
            if (2560 < result.bitLength()) {
                results.add(result);
                result = BigInteger.ONE;
            } else if (i == n) {
                results.add(result);
            }
        }
        return results.stream().reduce(BigInteger::multiply).orElse(BigInteger.ONE);
    }

    /*
        This is my second sequential solution for multiplying all factors of factorial in a karatsuba-friendly way.
        Splitting the interval into half enables parallelization.
    */
    BigInteger calc2(int n) {
        int half = n / 2;
        BigInteger leftResult = BigInteger.ONE;
        List<BigInteger> leftResults = new ArrayList<>();
        for (int i = 2; i < half; i++) {
            leftResult = leftResult.multiply(BigInteger.valueOf(i));
            if (2560 < leftResult.bitLength()) {
                leftResults.add(leftResult);
                leftResult = BigInteger.ONE;
            } else if (i == half - 1) {
                leftResults.add(leftResult);
            }
        }
        BigInteger rightResult = BigInteger.ONE;
        List<BigInteger> rightResults = new ArrayList<>();
        for (int i = half; i < n; i++) {
            rightResult = rightResult.multiply(BigInteger.valueOf(i));
            if (2560 < rightResult.bitLength()) {
                rightResults.add(rightResult);
                rightResult = BigInteger.ONE;
            } else if (i == n - 1) {
                rightResults.add(rightResult);
            }
        }
        return Stream.of(leftResults.stream(), rightResults.stream())
                .flatMap(s -> s)
                .reduce(BigInteger::multiply)
                .orElse(BigInteger.ONE);
    }

    /*
        First solution with a parallel stream to calculate factorial in a karatsuba-friendly way.
     */
    public static BigInteger karatsubaFactorialParallelStream(int n) {
        List<BigInteger> results =
                IntStream.rangeClosed(2, n)
                        .mapToObj(BigInteger::valueOf)
                        .parallel()
                        .collect(() -> new ArrayList<BigInteger>(),
                                (list, i) -> accumulate(list, i),
                                (left, right) -> combine(left, right)
                        );
        return results.get(0);
    }

    static void combine(List<BigInteger> left, List<BigInteger> right) {
        BigInteger result =
                Stream.of(left.stream(), right.stream())
                        .flatMap(s -> s)
                        .parallel()
                        .reduce(BigInteger::parallelMultiply)
                        .orElse(BigInteger.ONE);
        left.clear();
        left.add(result);
    }


    static void accumulate(List<BigInteger> list, BigInteger i) {
        if (list.isEmpty() || 2560 < list.get(list.size() - 1).bitLength()) {
            list.add(i);
        } else {
            list.set(list.size() - 1, list.get(list.size() - 1).multiply(i));
        }
    }

    /*
        Second solution with a parallel stream to calculate factorial in a karatsuba-friendly way.
        The list was replaced by an array.
     */
    public static BigInteger karatsubaFactorialParallelStream2(int n, BinaryOperator<BigInteger> multiply) {
        BigInteger[] results = IntStream.rangeClosed(2, n)
                        .mapToObj(BigInteger::valueOf)
                        .parallel()
                        .collect(() -> new BigInteger[]{BigInteger.ONE, BigInteger.ONE},
                                (array, i) -> accumulate2(array, i, multiply),
                                (left, right) -> combine2(left, right, multiply)
                        );
        return results[0].multiply(results[1]);
    }

    static void accumulate2(BigInteger[] bigInts, BigInteger i, BinaryOperator<BigInteger> multiply) {
        if (2560 < bigInts[0].bitLength()) {
            if (2560 < bigInts[1].bitLength()) {
                bigInts[0] = multiply.apply(bigInts[0], bigInts[1]);
                bigInts[1] = BigInteger.ONE;
            } else {
                bigInts[1] = bigInts[1].multiply(i);
            }
        } else {
            bigInts[0] = bigInts[0].multiply(i);
        }
    }

    static void combine2(BigInteger[] left, BigInteger[] right, BinaryOperator<BigInteger> multiply) {
        left[0] = Stream.of(left[0], left[1], right[0], right[1])
                        .parallel()
                        .reduce(multiply)
                        .orElse(BigInteger.ONE);
        left[1] = BigInteger.ONE;
    }

    /*
        This is the application of the karatsuba-friendly solution from parallel stream to a ForkJoinPool solution.
        First results show me the optimization has no effect.
        The CPU appears to be fully saturated so that no further optimization has got any effect.
     */
    static class KaratsubaFactorialTask extends RecursiveTask<BigInteger> {
        private final int start;
        private final int end;
        private final int minLength;
        private final BinaryOperator<BigInteger> multiply;

        public KaratsubaFactorialTask(int start, int end, int minLength, BinaryOperator<BigInteger> multiply) {
            this.start = start;
            this.end = end;
            this.minLength = minLength;
            this.multiply = multiply;
        }

        @Override
        protected BigInteger compute() {
            int length = end - start;
            if (length <= minLength) {
                BigInteger result = BigInteger.ONE;
                List<BigInteger> results = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    result = result.multiply(BigInteger.valueOf(i));
                    if (2560 < result.bitLength()) {
                        results.add(result);
                        result = BigInteger.ONE;
                    } else if (i == end - 1) {
                        results.add(result);
                    }
                }
                return results.stream()
                        .parallel()
                        .reduce(BigInteger::parallelMultiply)
                        .orElse(BigInteger.ONE);
            } else {
                int halfLength = length / 2;
                KaratsubaFactorialTask leftTask =
                        new KaratsubaFactorialTask(start, start + halfLength, minLength, multiply);
                leftTask.fork();
                KaratsubaFactorialTask rightTask =
                        new KaratsubaFactorialTask(start + halfLength, end, minLength, multiply);
                return multiply.apply(rightTask.compute(), leftTask.join());
            }
        }
    }

    public static class FactorialTask extends RecursiveTask<BigInteger> {
        private final int start;
        private final int end;
        private final int minLength;
        private final BinaryOperator<BigInteger> multiply;

        public FactorialTask(int start, int end, int minLength, BinaryOperator<BigInteger> multiply) {
            this.start = start;
            this.end = end;
            this.minLength = minLength;
            this.multiply = multiply;
        }

        @Override
        protected BigInteger compute() {
            int length = end - start;
            if (length <= minLength) {
                BigInteger result = BigInteger.valueOf(start);
                for (int i = start + 1; i < end; i++) { result = result.multiply(BigInteger.valueOf(i)); }
                return result;
            } else {
                int halfLength = length / 2;
                FactorialTask leftTask =
                        new FactorialTask(start, start + halfLength, minLength, multiply);
                leftTask.fork();
                FactorialTask rightTask =
                        new FactorialTask(start + halfLength, end, minLength, multiply);
                return multiply.apply(rightTask.compute(), leftTask.join());
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_08_ParallelFactorial.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
