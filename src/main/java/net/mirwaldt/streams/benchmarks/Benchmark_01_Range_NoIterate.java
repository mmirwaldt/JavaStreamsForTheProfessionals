package net.mirwaldt.streams.benchmarks;

import net.mirwaldt.streams.util.SumUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 3)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@State(Scope.Thread)
public class Benchmark_01_Range_NoIterate {
    /*
        Benchmark                                          Mode  Cnt   Score   Error  Units
        Benchmark_01_Range_NoIterate.betterParallelStream  avgt    9  32.116 ± 0.685  ms/op
        Benchmark_01_Range_NoIterate.flawedParallelStream  avgt    9  74.987 ± 0.891  ms/op
     */

    @Benchmark
    public long flawedParallelStream() {
        return IntStream
                .iterate(1, i -> i + 1)
                .limit(10_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) <= 40)
                .parallel()
                .count();
    }

    @Benchmark
    public long betterParallelStream() {
        return IntStream
                .rangeClosed(1, 10_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) <= 40)
                .parallel()
                .count();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_01_Range_NoIterate.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
