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
@State(Scope.Thread)
public class Benchmark_04_FindAny {
    /*
        Benchmark                                       Mode  Cnt   Score   Error  Units
        Benchmark_04_FindAny.findAnyParallelStream      avgt   25  28.856 ± 0.554  ms/op
        Benchmark_04_FindAny.findFirstLoop              avgt   25  43.463 ± 0.006  ms/op
        Benchmark_04_FindAny.findFirstParallelStream    avgt   25  38.318 ± 0.760  ms/op
        Benchmark_04_FindAny.findFirstSequentialStream  avgt   25  46.416 ± 0.029  ms/op
     */

    private int N = 100_000_000;

    @Benchmark
    public int findFirstLoop() {
        for (int i = 1; i <= N; i++) {
            if(SumUtil.sumOfDigits(i) == 60) {
                return i;
            }
        }
        return -1;
    }

    @Benchmark
    public int findFirstSequentialStream() {
        return IntStream
                .rangeClosed(1, N)
                .filter(i -> SumUtil.sumOfDigits(i) == 60)
                .findFirst().orElse(-1);
    }

    @Benchmark
    public int findFirstParallelStream() {
        return IntStream
                .rangeClosed(1, N)
                .filter(i -> SumUtil.sumOfDigits(i) == 60)
                .parallel()
                .findFirst().orElse(-1);
    }

    @Benchmark
    public int findAnyParallelStream() {
        return IntStream
                .rangeClosed(1, N)
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
