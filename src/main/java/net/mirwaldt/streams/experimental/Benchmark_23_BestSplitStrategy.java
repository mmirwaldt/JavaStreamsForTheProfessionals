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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

import static java.math.BigInteger.ONE;
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.*;

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
        BigInteger[] result = ForkJoinPool.commonPool().invoke(new TomCookKaratsubaFactorialTask(n, multiply));
        return multiply.apply(multiply.apply(result[0], result[1]), result[2]);
//        int[] splits = splits(n);
//        return factorialCompletableFutureSequentialMultiply(n, splits, multiply);
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
                result.add(i + 1);
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

//            List<CompletableFuture<AtomicReference<BigInteger>>> joinedCompletableFutures = join(completableFutures, multiply);
            BigInteger[] results = completableFutures.stream()
                    .map(CompletableFuture::join)
                    .parallel()
                    .collect(() -> new BigInteger[]{ONE, ONE, ONE},
                            (array, i) -> accumulate(array, i, multiply),
                            (left, right) -> combine(left, right, multiply)
                    );
            return results[0].multiply(results[1]).multiply(results[2]);


//            FactorialCompletableFuturesTask task = new FactorialCompletableFuturesTask(completableFutures, multiply);
//            return ForkJoinPool.commonPool().invoke(task);
        }

//        private List<CompletableFuture<AtomicReference<BigInteger>>> join(
//                List<CompletableFuture<BigInteger>> completableFutures, BinaryOperator<BigInteger> multiply) {
//            // In packs of 3 for Tom-Cook
//            List<CompletableFuture<AtomicReference<BigInteger>>> result = new ArrayList<>();
//
//            AtomicReference<BigInteger> atomicReference = new AtomicReference<>(ONE);
//            for (int i = 1; i <= completableFutures.size(); i++) {
//                CompletableFuture<BigInteger> completableFuture = completableFutures.get(i - 1);
//                if(0 < i % 3) {
//                    completableFuture.thenAcceptAsync(bigInt -> atomicReference.up)
//                }
//            }
//            return result;
//        }

        public static void accumulate(BigInteger[] result, BigInteger i, BinaryOperator<BigInteger> multiply) {
            result[2] = result[2].multiply(i);
            if (KARATSUBA_THRESHOLD_IN_BITS <= result[2].bitLength()) {
                result[1] = multiply.apply(result[1], result[2]);
                result[2] = ONE;
            }
            if(TOM_COOK_THRESHOLD_IN_BITS <= result[1].bitLength()) {
                result[0] = multiply.apply(result[0], result[1]);
                result[1] = ONE;
            }
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

    static class FactorialCompletableFuturesTask extends RecursiveTask<BigInteger> {
        private final int start;
        private final int end;
        private final List<CompletableFuture<BigInteger>> completableFutures;
        private final BinaryOperator<BigInteger> multiply;

        public FactorialCompletableFuturesTask(
                List<CompletableFuture<BigInteger>> completableFutures,
                BinaryOperator<BigInteger> multiply) {
            this.start = 0;
            this.end = completableFutures.size();
            this.completableFutures = completableFutures;
            this.multiply = multiply;
        }

        public FactorialCompletableFuturesTask(
                int start,
                int end,
                List<CompletableFuture<BigInteger>> completableFutures,
                BinaryOperator<BigInteger> multiply) {
            this.start = start;
            this.end = end;
            this.completableFutures = completableFutures;
            this.multiply = multiply;
        }

        @Override
        protected BigInteger compute() {
            int length = end - start;
            if (length <= 6) {
                BigInteger result = completableFutures.get(start).join();
                for (int i = start + 1; i < end; i++) {
                    result = multiply.apply(result, completableFutures.get(i).join());
                }
                return result;
            } else {
                int halfLength = length / 2;
                FactorialCompletableFuturesTask leftTask =
                        new FactorialCompletableFuturesTask(start, start + halfLength, completableFutures, multiply);
                leftTask.fork();
                FactorialCompletableFuturesTask rightTask =
                        new FactorialCompletableFuturesTask(start + halfLength, end, completableFutures, multiply);
                return multiply.apply(rightTask.compute(), (leftTask.join()));
            }
        }
    }

    public static class TomCookKaratsubaFactorialTask extends RecursiveTask<BigInteger[]> {
        private final int start;
        private final int end;
        private final BinaryOperator<BigInteger> multiply;

        public TomCookKaratsubaFactorialTask(int n, BinaryOperator<BigInteger> multiply) {
            this.start = 1;
            this.end = n + 1;
            this.multiply = multiply;
        }

        public TomCookKaratsubaFactorialTask(int start, int end, BinaryOperator<BigInteger> multiply) {
            this.start = start;
            this.end = end;
            this.multiply = multiply;
        }

        @Override
        protected BigInteger[] compute() {
            int length = end - start;
            if (length == 1) {
                return new BigInteger[] { ONE, ONE, BigInteger.valueOf(start) };
            } else {
                int halfLength = length / 2;
                TomCookKaratsubaFactorialTask leftTask = new TomCookKaratsubaFactorialTask(start, start + halfLength, multiply);
                leftTask.fork();
                TomCookKaratsubaFactorialTask rightTask = new TomCookKaratsubaFactorialTask(start + halfLength, end, multiply);
                BigInteger[] rightResult = rightTask.compute();
                BigInteger[] leftResult = leftTask.join();
                leftResult[2] = multiply.apply(leftResult[2], rightResult[2]);
                if(KARATSUBA_THRESHOLD_IN_BITS <= leftResult[2].bitLength()) {
                    leftResult[1] = multiply.apply(leftResult[1], leftResult[2]);
                    leftResult[2] = ONE;
                }
                leftResult[1] = multiply.apply(leftResult[1], rightResult[1]);
                if(TOM_COOK_THRESHOLD_IN_BITS <= leftResult[1].bitLength()) {
                    leftResult[0] = multiply.apply(leftResult[0], leftResult[1]);
                    leftResult[1] = ONE;
                }
                leftResult[0] = multiply.apply(leftResult[0], rightResult[0]);
                return leftResult;
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_23_BestSplitStrategy.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
