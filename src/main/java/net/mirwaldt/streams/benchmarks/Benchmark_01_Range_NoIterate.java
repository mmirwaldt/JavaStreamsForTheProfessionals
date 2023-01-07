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
//@Fork(value = 3)
//@Warmup(iterations = 3)
//@Measurement(iterations = 3)
@State(Scope.Thread)
public class Benchmark_01_Range_NoIterate {
    /*
        Benchmark                                                 Mode  Cnt   Score   Error  Units
        Benchmark_01_Range_NoIterate.filterLimitParallelStream    avgt   25  62.828 ± 0.926  ms/op
        Benchmark_01_Range_NoIterate.filterLimitSequentialStream  avgt   25  66.339 ± 0.112  ms/op
        Benchmark_01_Range_NoIterate.rangeClosedParallel          avgt   25  19.058 ± 0.283  ms/op
        Benchmark_01_Range_NoIterate.sequentialLoop               avgt   25  64.237 ± 0.031  ms/op
     */

    @Benchmark
    public long sequentialLoop() {
        long result = 0;
        for (int i = 1; i <= 10_000_000; i++) {
            if(SumUtil.sumOfDigits(i) <= 40) {
                result++;
            }
        }
        return result;
    }

    @Benchmark
    public long filterLimitSequentialStream() {
        return IntStream
                .iterate(1, i -> i + 1)
                .limit(10_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) <= 40)
                .count();
    }

    @Benchmark
    public long filterLimitParallelStream() {
        return IntStream
                .iterate(1, i -> i + 1)
                .limit(10_000_000)
                .filter(i -> SumUtil.sumOfDigits(i) <= 40)
                .parallel()
                .count();
    }

    @Benchmark
    public long rangeClosedParallel() {
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
