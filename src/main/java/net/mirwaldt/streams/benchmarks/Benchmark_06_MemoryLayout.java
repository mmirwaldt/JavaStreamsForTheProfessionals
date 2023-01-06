package net.mirwaldt.streams.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 3)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@State(Scope.Thread)
public class Benchmark_06_MemoryLayout {
    /*
        Benchmark                                               Mode  Cnt    Score   Error  Units
        Benchmark_06_MemoryLayout.sumArrayListParallel          avgt    9   22.753 ± 3.476  ms/op
        Benchmark_06_MemoryLayout.sumArrayListSequential        avgt    9   18.862 ± 0.038  ms/op
        Benchmark_06_MemoryLayout.sumArrayParallel              avgt    9    2.072 ± 0.008  ms/op
        Benchmark_06_MemoryLayout.sumArraySequential            avgt    9    5.145 ± 0.019  ms/op
        Benchmark_06_MemoryLayout.sumSortedArrayListParallel    avgt    9   52.343 ± 0.140  ms/op
        Benchmark_06_MemoryLayout.sumSortedArrayListSequential  avgt    9  158.809 ± 0.769  ms/op
     */

    final int N = 20_000_000;

    int[] array;
    private ArrayList<Integer> arrayList;
    private ArrayList<Integer> sortedArrayList;

    @Setup
    public void setup() {
        Random random = new Random(1234); // We always want the same random numbers
        arrayList = new ArrayList<>(N);
        array = new int[N];
        for (int i = 0; i < N; i++) {
            int n = random.nextInt(1_000_000);
            arrayList.add(n);
        }
        sortedArrayList = new ArrayList<>(arrayList);
        Collections.sort(sortedArrayList);
    }

    @Benchmark
    public long sumArrayListSequential() {
        return arrayList.stream()
                .mapToLong(i -> i)
                .sum();
    }

    @Benchmark
    public long sumArrayListParallel() {
        return arrayList.stream()
                .mapToLong(i -> i)
                .parallel()
                .sum();
    }

    @Benchmark
    public long sumArraySequential() {
        return Arrays.stream(array)
                .mapToLong(i -> i)
                .sum();
    }

    @Benchmark
    public long sumArrayParallel() {
        return Arrays.stream(array)
                .mapToLong(i -> i)
                .parallel()
                .sum();
    }

    @Benchmark
    public long sumSortedArrayListSequential() {
        return sortedArrayList.stream()
                .mapToLong(i -> i)
                .sum();
    }

    @Benchmark
    public long sumSortedArrayListParallel() {
        return sortedArrayList.stream()
                .mapToLong(i -> i)
                .parallel()
                .sum();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + Benchmark_06_MemoryLayout.class.getSimpleName() + ".*")
                .build();

        new Runner(opt).run();
    }
}
