package net.mirwaldt.streams.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.groupingByConcurrent;
import static net.mirwaldt.streams.util.SumUtil.sumOfDigits;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 3)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@State(Scope.Thread)
public class Benchmark_05_GroupingByConcurrent {
    /*
        Benchmark                                                           (n)  Mode  Cnt    Score   Error  Units
        Benchmark_05_GroupingByConcurrent.groupByConcurrentInParallel  10000000  avgt    9   65.499 ± 2.230  ms/op
        Benchmark_05_GroupingByConcurrent.groupByInParallel            10000000  avgt    9   86.027 ± 0.307  ms/op
        Benchmark_05_GroupingByConcurrent.groupByInSequential          10000000  avgt    9  159.592 ± 2.795  ms/op
     */

    private final int n = 10000000;

    @Benchmark
    public Map<Long, List<Long>> groupByInSequential() {
        return LongStream.rangeClosed(1, n)
                .filter(i -> sumOfDigits(i) <= 30)
                .boxed()
                .collect(groupingBy(i -> i % 10000));
    }

    @Benchmark
    public Map<Long, List<Long>> groupByInParallel() {
        return LongStream.rangeClosed(1, n)
                .filter(i -> sumOfDigits(i) <= 30)
                .boxed()
                .parallel()
                .collect(groupingBy(i -> i % 10000));
    }

    @Benchmark
    public ConcurrentMap<Long, List<Long>> groupByConcurrentInParallel() {
        return LongStream.rangeClosed(1, n)
                .filter(i -> sumOfDigits(i) <= 30)
                .boxed()
                .parallel()
                .collect(groupingByConcurrent(i -> i % 10000));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_05_GroupingByConcurrent.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
