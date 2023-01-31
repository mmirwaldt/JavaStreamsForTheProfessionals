package net.mirwaldt.streams.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.lang.Math.*;
import static java.util.concurrent.CompletableFuture.completedFuture;

@SuppressWarnings("DuplicatedCode")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_08_ParallelFactorial {
    /*
    100_000
    Benchmark                                                                         Mode  Cnt     Score   Error  Units
    Benchmark_01_ParallelFactorial.factorialCompletableFutureParallelMultiply         avgt   25    16.834 ± 0.019  ms/op
    Benchmark_01_ParallelFactorial.factorialCompletableFutureSequentialMultiply       avgt   25    31.427 ± 0.082  ms/op
    Benchmark_01_ParallelFactorial.factorialParallelInForkJoinPoolParallelMultiply    avgt   25    16.244 ± 0.013  ms/op
    Benchmark_01_ParallelFactorial.factorialParallelInForkJoinPoolSequentialMultiply  avgt   25    29.463 ± 0.325  ms/op
    Benchmark_01_ParallelFactorial.factorialParallelStreamParallelMultiply            avgt   25    39.122 ± 0.038  ms/op
    Benchmark_01_ParallelFactorial.factorialParallelStreamSequentialMultiply          avgt   25    53.308 ± 0.252  ms/op
    Benchmark_01_ParallelFactorial.factorialSequentialStreamParallelMultiply          avgt   25  2064.734 ± 4.456  ms/op
    Benchmark_01_ParallelFactorial.factorialSequentialStreamSequentialMultiply        avgt   25  2064.484 ± 4.045  ms/op
     */

    public int N = 100_000;

//    @Benchmark
//    public BigInteger factorialSequentialStreamSequentialMultiply() {
//        return factorialSequentialStreamSequentialMultiply(N);
//    }
//
//    @Benchmark
//    public BigInteger factorialSequentialStreamParallelMultiply() {
//        return factorialSequentialStreamParallelMultiply(N);
//    }

//    @Benchmark
//    public BigInteger factorialParallelStreamSequentialMultiply() {
//        return factorialParallelStreamSequentialMultiply(N);
//    }

//    @Benchmark
//    public BigInteger factorialParallelStreamParallelMultiply() {
//        return factorialParallelStreamParallelMultiply(N);
//    }

//    @Benchmark
//    public BigInteger factorialParallelInForkJoinPoolKaratsubaSequentialMultiply() {
//        return factorialParallelInForkJoinPoolKaratsubaSequentialMultiply(N);
//    }
//
//    @Benchmark
//    public BigInteger factorialParallelInForkJoinPoolKaratsubaParallelMultiply() {
//        return factorialParallelInForkJoinPoolKaratsubaParallelMultiply(N);
//    }
//
//    @Benchmark
//    public BigInteger factorialParallelInForkJoinPoolSequentialMultiply() {
//        return factorialParallelInForkJoinPoolSequentialMultiply(N);
//    }
//
//    @Benchmark
//    public BigInteger factorialParallelInForkJoinPoolParallelMultiply() {
//        return factorialParallelInForkJoinPoolParallelMultiply(N);
//    }


    @Benchmark
    public BigInteger factorialCompletableFutureParallelMultiply() {
        return factorialCompletableFutureParallelMultiply(N);
    }

//    @Benchmark
//    public BigInteger factorialCompletableFutureSequentialMultiply() {
//        return factorialCompletableFutureSequentialMultiply(N);
//    }

    public BigInteger factorialParallelStreamSequentialMultiply(int n) {
        return LongStream.rangeClosed(2, n)
                .mapToObj(BigInteger::valueOf)
                .parallel()
                .reduce(BigInteger::multiply)
                .orElse(BigInteger.ONE);
    }

    public BigInteger factorialParallelStreamParallelMultiply(int n) {
        return LongStream.rangeClosed(2, n)
                .mapToObj(BigInteger::valueOf)
                .parallel()
                .reduce(BigInteger::parallelMultiply)
                .orElse(BigInteger.ONE);
    }

    public BigInteger factorialSequentialStreamSequentialMultiply(int n) {
        return LongStream.rangeClosed(2, n)
                .mapToObj(BigInteger::valueOf)
                .reduce(BigInteger::multiply)
                .orElse(BigInteger.ONE);
    }

    public BigInteger factorialSequentialStreamParallelMultiply(int n) {
        return LongStream.rangeClosed(2, n)
                .mapToObj(BigInteger::valueOf)
                .reduce(BigInteger::parallelMultiply)
                .orElse(BigInteger.ONE);
    }

    public BigInteger factorialParallelInForkJoinPoolSequentialMultiply(int n) {
        return ForkJoinPool.commonPool()
                .invoke(new FactorialTask(1, n + 1, 100, false));
    }

    public BigInteger factorialParallelInForkJoinPoolParallelMultiply(int n) {
        return ForkJoinPool.commonPool()
                .invoke(new FactorialTask(1, n + 1, 100, true));
    }

    public BigInteger factorialParallelInForkJoinPoolKaratsubaSequentialMultiply(int n) {
        return ForkJoinPool.commonPool()
                .invoke(new KaratsubaFactorialTask(n, false));
    }

    public BigInteger factorialParallelInForkJoinPoolKaratsubaParallelMultiply(int n) {
        KaratsubaFactorialTask task = new KaratsubaFactorialTask(n, true);
        BigInteger result = ForkJoinPool.commonPool()
                .invoke(task);
        System.out.println("max_i=" + KaratsubaFactorialTask.max_i.get());
        return result;
    }

    public BigInteger factorialCompletableFutureParallelMultiply(int n) {
        return Benchmark_08_ParallelFactorial.calculate(n, BigInteger::parallelMultiply);
    }

    public BigInteger factorialCompletableFutureSequentialMultiply(int n) {
        return Benchmark_08_ParallelFactorial.calculate(n, BigInteger::multiply);
    }

    public static class FactorialTask extends RecursiveTask<BigInteger> {
        private final int start;
        private final int end;
        private final int minLength;
        private final boolean parallelMultiply;

        public FactorialTask(int start, int end, int minLength, boolean parallelMultiply) {
            this.start = start;
            this.end = end;
            this.minLength = minLength;
            this.parallelMultiply = parallelMultiply;
        }

        @Override
        protected BigInteger compute() {
            int length = end - start;
            if (length <= minLength) {
                return IntStream.range(start + 1, end).mapToObj(BigInteger::valueOf)
                        .reduce(BigInteger::multiply).orElse(BigInteger.ONE);
            } else {
                int halfLength = length / 2;
                FactorialTask leftTask = new FactorialTask(start, start + halfLength, minLength, parallelMultiply);
                leftTask.fork();
                FactorialTask rightTask = new FactorialTask(start + halfLength, end, minLength, parallelMultiply);
                if (parallelMultiply) {
                    return rightTask.compute().parallelMultiply(leftTask.join());
                } else {
                    return rightTask.compute().multiply(leftTask.join());
                }
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_08_ParallelFactorial.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    // This causes a stackoverflow because it does not split all into lb(n) intervals but even many more. :-(
    // Let's try to translate the recursion into an iteration
    public static class KaratsubaFactorialTask extends RecursiveTask<BigInteger> {
        public static AtomicInteger max_i = new AtomicInteger();
        private final int i;
        private final int start;
        private final int end;
        private final boolean parallelMultiply;

        public KaratsubaFactorialTask(int n, boolean parallelMultiply) {
            this.i = 2;
            this.start = 0;
            this.end = n + 1;
            this.parallelMultiply = parallelMultiply;
        }

        public KaratsubaFactorialTask(int i, int start, int end, boolean parallelMultiply) {
            this.i = i;
            max_i.updateAndGet(j -> max(i, j));
            this.start = start;
            this.end = end;
            this.parallelMultiply = parallelMultiply;
        }

        @Override
        protected BigInteger compute() {
            int length = end - start;
            if (0 < i) {
                int minLength = (int) ceil(pow(2.71d, 1.0d / (i - 1)) * 363d);
                if (minLength < length) {
                    KaratsubaFactorialTask leftTask =
                            new KaratsubaFactorialTask(i + 1, start + minLength, end, parallelMultiply);
                    leftTask.fork();
                    // we cut out what we want to multiply to get a big integer with
                    KaratsubaFactorialTask rightTask =
                            new KaratsubaFactorialTask(0, start, start + minLength, parallelMultiply);
                    if (parallelMultiply) {
//                        BigInteger rightResult = leftTask.join();
//                        BigInteger leftResult = rightTask.compute();
//                        return leftResult.parallelMultiply(rightResult);
                        return rightTask.compute().parallelMultiply(leftTask.join());
                    } else {
                        return rightTask.compute().multiply(leftTask.join());
                    }
                }
            }
            return IntStream.range(start + 1, end).mapToObj(BigInteger::valueOf)
                    .reduce(BigInteger::multiply).orElse(BigInteger.ONE);
        }
    }

    public static BigInteger calculate(int n, BinaryOperator<BigInteger> multiplyOperator) {
        if(650 <= n) {
            int nextLength = 363;
            int nextStart = 1;
            int nextEnd = nextStart + nextLength;
            CompletableFuture<BigInteger> result = CompletableFuture.supplyAsync(calculate(nextStart, nextEnd, multiplyOperator));
            nextLength = 329; // 329 works better than 363 for the shrink ratio
            double shrinkRatio = 287d / nextLength;
            nextStart = nextEnd;
            for (int j = 2; nextStart < n + 1; ) {
                shrinkRatio = pow(shrinkRatio, 1.0d / (j - 1));
                nextLength = min((int) ceil(shrinkRatio * (double) nextLength), n + 1 - nextStart);
                nextEnd = nextStart + nextLength;
                j++;
                CompletableFuture<BigInteger> next = CompletableFuture.supplyAsync(calculate(nextStart, nextEnd, multiplyOperator));
                result = result.thenCombineAsync(next, multiplyOperator);
                nextStart = nextEnd;
            }
            return result.join();
        } else {
            return calculate(1, n + 1, multiplyOperator).get();
        }

    }

    private static Supplier<BigInteger> calculate(int start, int end, BinaryOperator<BigInteger> multiplyOperator) {
        return () -> {
            BigInteger result = BigInteger.valueOf(start);
            for (int i = start + 1; i < end; i++) {
                result = multiplyOperator.apply(result, BigInteger.valueOf(i));
            }
            System.out.println(Thread.currentThread().getName() + ": start=" + start + ", end=" + end
                    + ", result.bitlength()=" + result.bitLength());
            return result;
        };
    }
}
