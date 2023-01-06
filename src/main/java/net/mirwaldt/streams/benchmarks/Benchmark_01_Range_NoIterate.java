package net.mirwaldt.streams.benchmarks;

import net.mirwaldt.streams.ParallelStream_01_Range_NoIterate;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

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
        return ParallelStream_01_Range_NoIterate.flawedParallelStream();
    }

    @Benchmark
    public long betterParallelStream() {
        return ParallelStream_01_Range_NoIterate.betterParallelStream();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_01_Range_NoIterate.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
