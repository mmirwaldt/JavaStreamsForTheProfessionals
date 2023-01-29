package net.mirwaldt.streams.benchmarks;

import net.mirwaldt.streams.util.CollatzUtil;
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
    public static final int N = 1_000_000;
    public static final int M = 400;

    /*
        Benchmark                                       Mode  Cnt    Score   Error  Units
        Benchmark_04_FindAny.findAnyParallelStream      avgt   25   11.742 ± 0.368  ms/op
        Benchmark_04_FindAny.findFirstLoop              avgt   25  270.425 ± 2.283  ms/op
        Benchmark_04_FindAny.findFirstParallelStream    avgt   25   36.867 ± 0.836  ms/op
        Benchmark_04_FindAny.findFirstSequentialStream  avgt   25  169.240 ± 1.698  ms/op
     */

    @Benchmark
    public int findFirstLoop() {
        for (int i = 1; i <= N; i++) {
            if(CollatzUtil.follow(i) == M) {
                return i;
            }
        }
        return -1;
    }

    @Benchmark
    public int findFirstSequentialStream() {
        return IntStream
                .rangeClosed(1, N)
                .filter(i -> CollatzUtil.follow(i) == M)
                .findFirst().orElse(-1);
    }

    @Benchmark
    public int findAnyParallelStream() {
        return IntStream
                .rangeClosed(1, N)
                .filter(i -> CollatzUtil.follow(i) == M)
                .parallel()
                .findAny().orElse(-1);
    }

    @Benchmark
    public int findFirstParallelStream() {
        return IntStream
                .rangeClosed(1, N)
                .filter(i -> CollatzUtil.follow(i) == M)
                .parallel()
                .findFirst().orElse(-1);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_04_FindAny.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
