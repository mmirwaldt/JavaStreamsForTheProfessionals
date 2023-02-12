package net.mirwaldt.streams.experimental;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

import static java.math.BigInteger.ONE;
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.KARATSUBA_THRESHOLD_IN_BITS;

@SuppressWarnings("DuplicatedCode")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_23_BestSplitStrategy {
    int N = 100_000;

    int[] splits = splits(N);

    @Benchmark
    public BigInteger factorialCompletableFutureSequentialMultiply() {
        return factorialCompletableFutureSequentialMultiply(N, splits, BigInteger::multiply);
    }

    @Benchmark
    public BigInteger factorialCompletableFutureParallelMultiply() {
        return factorialCompletableFutureSequentialMultiply(N, splits, BigInteger::parallelMultiply);
    }

    public static BigInteger factorialCompletableFutureSequentialMultiply(int n, BinaryOperator<BigInteger> multiply) {
        int[] splits = splits(n);
        return factorialCompletableFutureSequentialMultiply(n, splits, multiply);
    }

    public static BigInteger factorialCompletableFutureSequentialMultiply(
            int n, int[] splits, BinaryOperator<BigInteger> multiply) {
        return new FactorialCompletableFuture().calculate(n, splits, multiply);
    }

    static int[] splits(int n) {
        List<Integer> result = new ArrayList<>();
        BigInteger bigInt = ONE;
        for (int i = 2; i <= n; i++) {
            bigInt = bigInt.multiply(BigInteger.valueOf(i));
            if (KARATSUBA_THRESHOLD_IN_BITS <= bigInt.bitLength()) {
                result.add(i);
                bigInt = ONE;
            }
        }
        return result.stream().mapToInt(i -> i).toArray();
    }

    public static class FactorialCompletableFuture {
        public BigInteger calculate(int n, int[] splits, BinaryOperator<BigInteger> multiply) {
            if (n < 0) {
                throw new IllegalArgumentException("n=" + n + " (< 0)");
            }
            if (n == 0) {
                return ONE;
            }
            List<CompletableFuture<BigInteger>> completableFutures = split(n, splits, multiply);
            return completableFutures.stream()
                    .map(CompletableFuture::join)
                    .parallel()
                    .reduce(multiply)
                    .orElse(ONE);
        }

        private List<CompletableFuture<BigInteger>> split(int n, int[] splits, BinaryOperator<BigInteger> multiply) {
            List<CompletableFuture<BigInteger>> completableFutures = new ArrayList<>();
            int start = 2;
            for (int split : splits) {
                completableFutures.add(calculate(start, split, multiply));
                start = split;
            }
            if(0 == splits.length) {
                completableFutures.add(calculate(2, n + 1, multiply));
            } else if (splits[splits.length - 1] < n + 1) {
                completableFutures.add(calculate(splits[splits.length - 1], n + 1, multiply));
            }
            return completableFutures;
        }

        private CompletableFuture<BigInteger> calculate(int start, int end, BinaryOperator<BigInteger> multiply) {
            return CompletableFuture.supplyAsync(() -> {
                BigInteger result = BigInteger.valueOf(start);
                for (int i = start + 1; i < end; i++) {
                    result = multiply.apply(result, BigInteger.valueOf(i));
                }
                return result;
            });
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_23_BestSplitStrategy.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
