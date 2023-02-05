package net.mirwaldt.streams.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static net.mirwaldt.streams.util.SumUtil.sumOfDigits;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_01_RangeClosed {
    public static final int N = 10_000_000;
    public static final int S = 40;

    /*
Benchmark                                             Mode  Cnt   Score   Error  Units
Benchmark_01_RangeClosed.filterLimitParallelStream    avgt   25  62.764 ± 0.303  ms/op
Benchmark_01_RangeClosed.filterLimitSequentialStream  avgt   25  66.239 ± 0.022  ms/op
Benchmark_01_RangeClosed.rangeClosedParallelStream    avgt   25  19.067 ± 0.152  ms/op
Benchmark_01_RangeClosed.rangeClosedSequentialStream  avgt   25  64.839 ± 0.011  ms/op
Benchmark_01_RangeClosed.sequentialLoop               avgt   25  64.198 ± 0.019  ms/op
     */

    @Benchmark
    public long sequentialLoop() {
        long result = 0;
        for (int i = 1; i <= N; i++) {
            if(sumOfDigits(i) <= S) {
                result++;
            }
        }
        return result;
    }

    @Benchmark
    public long filterLimitSequentialStream() {
        return IntStream
                .iterate(1, i -> i + 1)
                .limit(N)
                .filter(i -> sumOfDigits(i) <= S)
                .count();
    }

    @Benchmark
    public long filterLimitParallelStream() {
        return IntStream
                .iterate(1, i -> i + 1)
                .limit(N)
                .filter(i -> sumOfDigits(i) <= S)
                .parallel()
                .count();
    }

    @Benchmark
    public long rangeClosedSequentialStream() {
        return IntStream
                .rangeClosed(1, N)
                .filter(i -> sumOfDigits(i) <= S)
                .count();
    }

    @Benchmark
    public long rangeClosedParallelStream() {
        return IntStream
                .rangeClosed(1, N)
                .filter(i -> sumOfDigits(i) <= S)
                .parallel()
                .count();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_01_RangeClosed.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
