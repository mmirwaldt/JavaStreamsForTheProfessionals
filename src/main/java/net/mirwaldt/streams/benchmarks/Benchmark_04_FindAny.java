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
public class Benchmark_04_FindAny {
    /*
        Benchmark                       Mode  Cnt   Score   Error  Units
        Benchmark_04_FindAny.findAny    avgt    9  56.306 ± 1.790  ms/op
        Benchmark_04_FindAny.findFirst  avgt    9  68.651 ± 1.207  ms/op
     */
    @Benchmark
    public int findFirst() {
        return IntStream
                .rangeClosed(1, 100_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) == 60)
                .parallel()
                .findFirst().orElse(-1);
    }

    @Benchmark
    public int findAny() {
        return IntStream
                .rangeClosed(1, 100_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) == 60)
                .parallel()
                .findAny().orElse(-1);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_04_FindAny.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
