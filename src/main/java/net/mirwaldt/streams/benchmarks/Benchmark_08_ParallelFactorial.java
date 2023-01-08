package net.mirwaldt.streams.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

import static java.util.concurrent.CompletableFuture.completedFuture;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_08_ParallelFactorial {
    /*
    Benchmark                                                                         Mode  Cnt     Score   Error  Units
    Benchmark_01_ParallelFactorial.factorialCompletableFutureParallelMultiply         avgt   25    16.834 ± 0.019  ms/op
    Benchmark_01_ParallelFactorial.factorialCompletableFutureSequentialMultiply       avgt   25    31.427 ± 0.082  ms/op
    Benchmark_01_ParallelFactorial.factorialParallelInForkJoinPoolParallelMultiply    avgt   25    16.244 ± 0.013  ms/op
    Benchmark_01_ParallelFactorial.factorialParallelInForkJoinPoolSequentialMultiply  avgt   25    29.463 ± 0.325  ms/op
    Benchmark_01_ParallelFactorial.factorialParallelStreamParallelMultiply            avgt   25    39.122 ± 0.038  ms/op
    Benchmark_01_ParallelFactorial.factorialParallelStreamSequentialMultiply          avgt   25    53.308 ± 0.252  ms/op
    Benchmark_01_ParallelFactorial.factorialParallelStreamWithSpliterator             avgt   25    53.625 ± 0.192  ms/op
    Benchmark_01_ParallelFactorial.factorialSequentialStreamParallelMultiply          avgt   25  2064.734 ± 4.456  ms/op
    Benchmark_01_ParallelFactorial.factorialSequentialStreamSequentialMultiply        avgt   25  2064.484 ± 4.045  ms/op
     */

    public int N = 100000;

    @Benchmark
    public BigInteger factorialSequentialStreamSequentialMultiply() {
        return factorialSequentialStreamSequentialMultiply(N);
    }

    @Benchmark
    public BigInteger factorialSequentialStreamParallelMultiply() {
        return factorialSequentialStreamParallelMultiply(N);
    }

    @Benchmark
    public BigInteger factorialParallelStreamSequentialMultiply() {
        return factorialParallelStreamSequentialMultiply(N);
    }

    @Benchmark
    public BigInteger factorialParallelStreamParallelMultiply() {
        return factorialParallelStreamParallelMultiply(N);
    }

    @Benchmark
    public BigInteger factorialParallelInForkJoinPoolSequentialMultiply() {
        return factorialParallelInForkJoinPoolSequentialMultiply(N);
    }

    @Benchmark
    public BigInteger factorialParallelInForkJoinPoolParallelMultiply() {
        return factorialParallelInForkJoinPoolParallelMultiply(N);
    }

    @Benchmark
    public BigInteger factorialParallelStreamWithSpliterator() {
        return factorialParallelStreamWithSpliterator(N);
    }

    @Benchmark
    public BigInteger factorialCompletableFutureParallelMultiply() {
        return factorialCompletableFutureParallelMultiply(N);
    }

    @Benchmark
    public BigInteger factorialCompletableFutureSequentialMultiply() {
        return factorialCompletableFutureSequentialMultiply(N);
    }

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

    public BigInteger factorialParallelStreamWithSpliterator(int n) {
        return StreamSupport.stream(new FactorialSpliterator(1, n + 1, 100), true)
                .reduce(BigInteger::multiply)
                .orElse(BigInteger.ONE);
    }

    public BigInteger factorialCompletableFutureParallelMultiply(int n) {
        return new FactorialCompletableFuture(100, true).calculate(n + 1);
    }

    public BigInteger factorialCompletableFutureSequentialMultiply(int n) {
        return new FactorialCompletableFuture(100, false).calculate(n + 1);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_08_ParallelFactorial.class.getSimpleName())
                .build();
        new Runner(opt).run();
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
                if(parallelMultiply) {
                    return rightTask.compute().parallelMultiply(leftTask.join());
                } else {
                    return rightTask.compute().multiply(leftTask.join());
                }
            }
        }
    }

    public static class FactorialSpliterator implements Spliterator<BigInteger> {
        private int start;
        private final int end;
        private final int minLength;

        public FactorialSpliterator(int start, int end, int minLength) {
            this.start = start;
            this.end = end;
            this.minLength = minLength;
        }

        @Override
        public boolean tryAdvance(Consumer<? super BigInteger> action) {
            if (0 < estimateSize()) {
                action.accept(BigInteger.valueOf(start));
                start++;
            }
            return 0 < estimateSize();
        }

        @Override
        public Spliterator<BigInteger> trySplit() {
            int remaining = (int) estimateSize();
            if (minLength < remaining) {
                int splitLength = remaining / 2;
                int newStart = start;
                int newEnd = start + splitLength;
                start = newEnd;
                return new FactorialSpliterator(newStart, newEnd, minLength);
            } else {
                return null;
            }
        }

        @Override
        public long estimateSize() {
            return end - start;
        }

        @Override
        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL
                    | Spliterator.IMMUTABLE | Spliterator.DISTINCT;
        }
    }

    static class FactorialCompletableFuture {
        private final int minLength;
        private final boolean parallelMultiply;

        public FactorialCompletableFuture(int minLength, boolean parallelMultiply) {
            this.minLength = minLength;
            this.parallelMultiply = parallelMultiply;
        }

        protected BigInteger calculate(int n) {
            return calculate(1, n).join();
        }

        private CompletableFuture<BigInteger> calculate(int start, int end) {
            int length = end - start;
            if (length <= minLength) {
                return completedFuture(IntStream.range(start + 1, end).mapToObj(BigInteger::valueOf)
                        .reduce(BigInteger::multiply).orElse(BigInteger.ONE));
            } else {
                int halfLength = length / 2;
                if(parallelMultiply) {
                    return calculate(start, start + halfLength)
                            .thenCombineAsync(calculate(start + halfLength, end), BigInteger::parallelMultiply);
                } else {
                    return calculate(start, start + halfLength)
                            .thenCombineAsync(calculate(start + halfLength, end), BigInteger::multiply);
                }
            }
        }
    }
}
