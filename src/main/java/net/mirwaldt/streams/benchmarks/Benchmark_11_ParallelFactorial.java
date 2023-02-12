package net.mirwaldt.streams.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.stream.LongStream;

import static java.math.BigInteger.ONE;
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.tomCookKaratsubaFactorialForkJoinPool;
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.tomCookKaratsubaFactorialParallelStream;

@SuppressWarnings("DuplicatedCode")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_11_ParallelFactorial {
    /*
Benchmark                                                                                 Mode  Cnt   Score   Error  Units
Benchmark_11_ParallelFactorial.factorialParallelInForkJoinPoolParallelMultiply            avgt   25  16.637 ± 0.052  ms/op
Benchmark_11_ParallelFactorial.factorialParallelInForkJoinPoolSequentialMultiply          avgt   25  29.383 ± 0.384  ms/op
Benchmark_11_ParallelFactorial.factorialParallelStreamParallelMultiply                    avgt   25  39.208 ± 0.041  ms/op
Benchmark_11_ParallelFactorial.factorialParallelStreamSequentialMultiply                  avgt   25  53.686 ± 0.148  ms/op
Benchmark_11_ParallelFactorial.tomCookKaratsubaFactorialForkJoinPoolParallelMultiply      avgt   25  17.594 ± 0.030  ms/op
Benchmark_11_ParallelFactorial.tomCookKaratsubaFactorialForkJoinPoolSequentialMultiply    avgt   25  30.662 ± 0.284  ms/op
Benchmark_11_ParallelFactorial.tomCookKaratsubaFactorialParallelStreamParallelMultiply    avgt   25  18.902 ± 0.028  ms/op
Benchmark_11_ParallelFactorial.tomCookKaratsubaFactorialParallelStreamSequentialMultiply  avgt   25  32.859 ± 0.134  ms/op


Saturation can be measured in bash by:
while true; do uptime | cut -d ',' -f 2,4 ; sleep 1; done
Example output:
12:24,  load average: 2.66

Max saturation of CPU I have observed: 7.79 with factorialParallelInForkJoinPoolParallelMultiply
=> Good because FJP has only got 7 threads => max. 7.00
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
    public BigInteger tomCookKaratsubaFactorialParallelStreamSequentialMultiply() {
        return tomCookKaratsubaFactorialParallelStream(N, BigInteger::multiply);
    }

    @Benchmark
    public BigInteger tomCookKaratsubaFactorialParallelStreamParallelMultiply() {
        return tomCookKaratsubaFactorialParallelStream(N, BigInteger::parallelMultiply);
    }

    @Benchmark
    public BigInteger tomCookKaratsubaFactorialForkJoinPoolSequentialMultiply() {
        return tomCookKaratsubaFactorialForkJoinPool(N, BigInteger::multiply);
    }

    @Benchmark
    public BigInteger tomCookKaratsubaFactorialForkJoinPoolParallelMultiply() {
        return tomCookKaratsubaFactorialForkJoinPool(N, BigInteger::parallelMultiply);
    }

    public static BigInteger factorialParallelStream(int n, BinaryOperator<BigInteger> multiply) {
        return LongStream.rangeClosed(2, n)
                .mapToObj(BigInteger::valueOf)
                .parallel()
                .reduce(multiply)
                .orElse(ONE);
    }

    public static BigInteger factorialParallelInForkJoinPool(int n, BinaryOperator<BigInteger> multiply) {
        return ForkJoinPool.commonPool().invoke(new FactorialTask(n, multiply));
    }

    public static class FactorialTask extends RecursiveTask<BigInteger> {
        private final int start;
        private final int end;
        private final BinaryOperator<BigInteger> multiply;

        public FactorialTask(int n, BinaryOperator<BigInteger> multiply) {
            this.start = 1;
            this.end = n + 1;
            this.multiply = multiply;
        }

        public FactorialTask(int start, int end, BinaryOperator<BigInteger> multiply) {
            this.start = start;
            this.end = end;
            this.multiply = multiply;
        }

        @Override
        protected BigInteger compute() {
            int length = end - start;
            if (length == 1) {
                return BigInteger.valueOf(start);
            } else {
                int halfLength = length / 2;
                FactorialTask leftTask = new FactorialTask(start, start + halfLength, multiply);
                leftTask.fork();
                FactorialTask rightTask = new FactorialTask(start + halfLength, end, multiply);
                return multiply.apply(rightTask.compute(), leftTask.join());
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_11_ParallelFactorial.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
