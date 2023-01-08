package net.mirwaldt.streams.benchmarks;

import net.mirwaldt.streams.util.SumUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.groupingByConcurrent;
import static net.mirwaldt.streams.util.SumUtil.sumOfDigits;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_05_GroupingByConcurrent {
    /*
        Benchmark                                                            Mode  Cnt    Score   Error  Units
        Benchmark_05_GroupingByConcurrent.groupByConcurrentInParallelStream  avgt   25   39.712 ± 0.709  ms/op
        Benchmark_05_GroupingByConcurrent.groupByInLoop                      avgt   25  192.925 ± 4.112  ms/op
        Benchmark_05_GroupingByConcurrent.groupByInParallelStream            avgt   25   48.541 ± 0.133  ms/op
        Benchmark_05_GroupingByConcurrent.groupByInSequentialStream          avgt   25   90.329 ± 0.560  ms/op
     */

    private final int N = 5_000_000;
    private final int M = 30;
    private final int P = 10000;

    @Benchmark
    public Map<Long, List<Long>> groupByInLoop() {
        Map<Long, List<Long>> numbersByLastDigits = new TreeMap<>();
        for (long i = 1; i <= N; i++) {
            if(SumUtil.sumOfDigits(i) <= M) {
                long key = i % P;
                numbersByLastDigits
                        .computeIfAbsent(key, (k) -> new ArrayList<>())
                        .add(i);
            }
        }
        return numbersByLastDigits;
    }

    @Benchmark
    public Map<Long, List<Long>> groupByInSequentialStream() {
        return LongStream.rangeClosed(1, N)
                .filter(i -> sumOfDigits(i) <= M)
                .boxed()
                .collect(groupingBy(i -> i % P));
    }

    @Benchmark
    public Map<Long, List<Long>> groupByInParallelStream() {
        return LongStream.rangeClosed(1, N)
                .filter(i -> sumOfDigits(i) <= M)
                .boxed()
                .parallel()
                .collect(groupingBy(i -> i % P));
    }

    @Benchmark
    public ConcurrentMap<Long, List<Long>> groupByConcurrentInParallelStream() {
        return LongStream.rangeClosed(1, N)
                .filter(i -> sumOfDigits(i) <= M)
                .boxed()
                .parallel()
                .collect(groupingByConcurrent(i -> i % P));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_05_GroupingByConcurrent.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
