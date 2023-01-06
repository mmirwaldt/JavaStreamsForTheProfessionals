package net.mirwaldt.streams.benchmarks;

import net.mirwaldt.streams.ParallelStream_04_FindAny;
import net.mirwaldt.streams.ParallelStream_05_GroupingByConcurrent;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

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
        return ParallelStream_04_FindAny.findFirst();
    }

    @Benchmark
    public int findAny() {
        return ParallelStream_04_FindAny.findAny();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_04_FindAny.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
