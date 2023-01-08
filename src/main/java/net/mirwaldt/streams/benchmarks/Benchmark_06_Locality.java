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
@State(Scope.Thread)
public class Benchmark_06_Locality {
    /*
        Benchmark                                           Mode  Cnt    Score   Error  Units
        Benchmark_06_Locality.sumArrayListParallel          avgt   25   14.803 ± 1.425  ms/op
        Benchmark_06_Locality.sumArrayListSequential        avgt   25   23.921 ± 0.034  ms/op
        Benchmark_06_Locality.sumArrayParallel              avgt   25    2.612 ± 0.004  ms/op
        Benchmark_06_Locality.sumArraySequential            avgt   25    6.605 ± 0.007  ms/op
        Benchmark_06_Locality.sumSortedArrayListParallel    avgt   25   65.907 ± 0.071  ms/op
        Benchmark_06_Locality.sumSortedArrayListSequential  avgt   25  205.647 ± 0.391  ms/op
     */

    final int N = 25_000_000;

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
                .include(".*" + Benchmark_06_Locality.class.getSimpleName() + ".*")
                .build();

        new Runner(opt).run();
    }
}
