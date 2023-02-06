package net.mirwaldt.streams.experimental;

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
public class Benchmark_20_SequentialFactorial {
    /*
        Benchmark                                                                   Mode  Cnt   Score   Error  Units
        Benchmark_20_SequentialFactorial.factorialLoop                              avgt   25  67.787 ± 0.150  ms/op
        Benchmark_20_SequentialFactorial.factorialSequentialStream                  avgt   25  68.871 ± 0.128  ms/op
        Benchmark_20_SequentialFactorial.tomCookKaratsubaFactorialSequentialStream  avgt   25  18.632 ± 0.089  ms/op
     */
    public int N = 20_000;

    @Benchmark
    public BigInteger factorialLoop() {
        return SequentialStream_EfficientMultiplicationStatistics.factorialLoop(N, BigInteger::multiply);
    }

    @Benchmark
    public BigInteger factorialSequentialStream() {
        return SequentialStream_EfficientMultiplicationStatistics.factorialSequentialStream(N, BigInteger::multiply);
    }

    @Benchmark
    public BigInteger tomCookKaratsubaFactorialSequentialStream() {
        return SequentialStream_EfficientMultiplicationStatistics.tomCookKaratsubaFactorialSequentialStream(N, BigInteger::multiply);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_20_SequentialFactorial.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
