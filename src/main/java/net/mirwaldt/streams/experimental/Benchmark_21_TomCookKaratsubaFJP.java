package net.mirwaldt.streams.experimental;


import net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("DuplicatedCode")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_21_TomCookKaratsubaFJP {

    /*
Benchmark                                                                                 (minLength)  Mode  Cnt   Score   Error  Units
Benchmark_21_TomCookKaratsubaFJP.tomCookKaratsubaFactorialForkJoinPoolParallelMultiply           1000  avgt   25  30.485 ± 0.362  ms/op
Benchmark_21_TomCookKaratsubaFJP.tomCookKaratsubaFactorialForkJoinPoolParallelMultiply           2000  avgt   25  30.806 ± 0.410  ms/op
Benchmark_21_TomCookKaratsubaFJP.tomCookKaratsubaFactorialForkJoinPoolParallelMultiply           3000  avgt   25  30.623 ± 0.316  ms/op
Benchmark_21_TomCookKaratsubaFJP.tomCookKaratsubaFactorialForkJoinPoolParallelMultiply           5000  avgt   25  31.873 ± 0.288  ms/op
Benchmark_21_TomCookKaratsubaFJP.tomCookKaratsubaFactorialForkJoinPoolParallelMultiply           6000  avgt   25  31.758 ± 0.331  ms/op
Benchmark_21_TomCookKaratsubaFJP.tomCookKaratsubaFactorialForkJoinPoolParallelMultiply           7000  avgt   25  36.148 ± 0.355  ms/op
Benchmark_21_TomCookKaratsubaFJP.tomCookKaratsubaFactorialForkJoinPoolSequentialMultiply         2000  avgt   25  17.576 ± 0.020  ms/op
Benchmark_21_TomCookKaratsubaFJP.tomCookKaratsubaFactorialForkJoinPoolSequentialMultiply         3000  avgt   25  17.594 ± 0.027  ms/op
Benchmark_21_TomCookKaratsubaFJP.tomCookKaratsubaFactorialForkJoinPoolSequentialMultiply         5000  avgt   25  18.785 ± 0.011  ms/op
Benchmark_21_TomCookKaratsubaFJP.tomCookKaratsubaFactorialForkJoinPoolSequentialMultiply         6000  avgt   25  18.799 ± 0.041  ms/op
Benchmark_21_TomCookKaratsubaFJP.tomCookKaratsubaFactorialForkJoinPoolSequentialMultiply         7000  avgt   25  23.109 ± 0.020  ms/op
Benchmark_21_TomCookKaratsubaFJP.tomCookKaratsubaFactorialForkJoinPoolSequentialMultiply        10000  avgt   25  23.098 ± 0.028  ms/op
     */


    private int N = 100_000;

    @Param({"1000", "2000", "3000", "5000", "6000", "7000", "10000"})
    private int minLength;

    @Benchmark
    public BigInteger tomCookKaratsubaFactorialForkJoinPoolParallelMultiply() {
        return ParallelStream_09_EfficientMultiplicationFriendly.tomCookKaratsubaFactorialForkJoinPool(N, BigInteger::multiply, minLength);
    }

    @Benchmark
    public BigInteger tomCookKaratsubaFactorialForkJoinPoolSequentialMultiply() {
        return ParallelStream_09_EfficientMultiplicationFriendly.tomCookKaratsubaFactorialForkJoinPool(N, BigInteger::parallelMultiply, minLength);
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_21_TomCookKaratsubaFJP.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
