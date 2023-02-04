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
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.*;

@SuppressWarnings("DuplicatedCode")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_11_ParallelFactorial {
    /*
Benchmark                                                                          Mode  Cnt   Score   Error  Units
Benchmark_11_ParallelFactorial.factorialParallelInForkJoinPoolParallelMultiply     avgt   25  16.631 ± 0.009  ms/op
Benchmark_11_ParallelFactorial.factorialParallelInForkJoinPoolSequentialMultiply   avgt   25  29.983 ± 0.456  ms/op
Benchmark_11_ParallelFactorial.factorialParallelStreamParallelMultiply             avgt   25  39.139 ± 0.041  ms/op
Benchmark_11_ParallelFactorial.factorialParallelStreamSequentialMultiply           avgt   25  53.641 ± 0.168  ms/op
Benchmark_11_ParallelFactorial.karatsubaFactorialForkJoinPoolParallelMultiply      avgt   25  17.191 ± 0.015  ms/op
Benchmark_11_ParallelFactorial.karatsubaFactorialForkJoinPoolSequentialMultiply    avgt   25  30.619 ± 0.446  ms/op
Benchmark_11_ParallelFactorial.karatsubaFactorialParallelStreamParallelMultiply    avgt   25  21.382 ± 0.050  ms/op
Benchmark_11_ParallelFactorial.karatsubaFactorialParallelStreamSequentialMultiply  avgt   25  34.700 ± 0.148  ms/op

Benchmark                                                                                 Mode  Cnt   Score   Error  Units
Benchmark_11_ParallelFactorial.factorialParallelInForkJoinPoolParallelMultiply            avgt   25  16.600 ± 0.037  ms/op
Benchmark_11_ParallelFactorial.factorialParallelInForkJoinPoolSequentialMultiply          avgt   25  29.848 ± 0.321  ms/op
Benchmark_11_ParallelFactorial.factorialParallelStreamParallelMultiply                    avgt   25  39.127 ± 0.048  ms/op
Benchmark_11_ParallelFactorial.factorialParallelStreamSequentialMultiply                  avgt   25  53.736 ± 0.161  ms/op
Benchmark_11_ParallelFactorial.karatsubaFactorialForkJoinPoolParallelMultiply             avgt   25  17.352 ± 0.017  ms/op
Benchmark_11_ParallelFactorial.karatsubaFactorialForkJoinPoolSequentialMultiply           avgt   25  30.755 ± 0.519  ms/op
Benchmark_11_ParallelFactorial.karatsubaFactorialParallelStreamParallelMultiply           avgt   25  21.687 ± 0.052  ms/op
Benchmark_11_ParallelFactorial.karatsubaFactorialParallelStreamSequentialMultiply         avgt   25  35.087 ± 0.178  ms/op
Benchmark_11_ParallelFactorial.tomCookKaratsubaFactorialForkJoinPoolParallelMultiply      avgt   25  23.098 ± 0.020  ms/op
Benchmark_11_ParallelFactorial.tomCookKaratsubaFactorialForkJoinPoolSequentialMultiply    avgt   25  36.205 ± 0.339  ms/op
Benchmark_11_ParallelFactorial.tomCookKaratsubaFactorialParallelStreamParallelMultiply    avgt   25  18.366 ± 0.028  ms/op
Benchmark_11_ParallelFactorial.tomCookKaratsubaFactorialParallelStreamSequentialMultiply  avgt   25  32.031 ± 0.135  ms/op
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
        return ForkJoinPool.commonPool().invoke(new FactorialTask(1, n + 1, 100, multiply));
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
                .include(Benchmark_11_ParallelFactorial.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
