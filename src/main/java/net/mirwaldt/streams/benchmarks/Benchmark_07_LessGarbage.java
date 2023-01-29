package net.mirwaldt.streams.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

import static java.lang.Character.*;
import static net.mirwaldt.streams.util.AlchemicalReduceUtil.*;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_07_LessGarbage {

    /*
Summary:
Benchmark                                                                      Mode  Cnt           Score          Error   Units
Benchmark_07_LessGarbage.reduceLittleGarbage                                   avgt   25           1.248 ±        0.010   ms/op
Benchmark_07_LessGarbage.reduceLittleGarbage:·gc.alloc.rate                    avgt   25         287.838 ±        2.322  MB/sec
Benchmark_07_LessGarbage.reduceMuchGarbage                                     avgt   25         576.079 ±        0.431   ms/op
Benchmark_07_LessGarbage.reduceMuchGarbage:·gc.alloc.rate                      avgt   25       11893.471 ±        8.489  MB/sec

All:
Benchmark                                                                           Mode  Cnt            Score          Error   Units
Benchmark_07_LessGarbage.reduceWithStringBuilders                                   avgt   25            2.201 ±        0.010   ms/op
Benchmark_07_LessGarbage.reduceWithStringBuilders:·gc.alloc.rate                    avgt   25           88.577 ±        0.393  MB/sec
Benchmark_07_LessGarbage.reduceWithStringBuilders:·gc.alloc.rate.norm               avgt   25       214692.184 ±        2.680    B/op
Benchmark_07_LessGarbage.reduceWithStringBuilders:·gc.churn.G1_Eden_Space           avgt   25           85.472 ±       53.354  MB/sec
Benchmark_07_LessGarbage.reduceWithStringBuilders:·gc.churn.G1_Eden_Space.norm      avgt   25       207219.953 ±   129359.142    B/op
Benchmark_07_LessGarbage.reduceWithStringBuilders:·gc.churn.G1_Survivor_Space       avgt   25            0.001 ±        0.002  MB/sec
Benchmark_07_LessGarbage.reduceWithStringBuilders:·gc.churn.G1_Survivor_Space.norm  avgt   25            2.688 ±        4.054    B/op
Benchmark_07_LessGarbage.reduceWithStringBuilders:·gc.count                         avgt   25           15.000                 counts
Benchmark_07_LessGarbage.reduceWithStringBuilders:·gc.time                          avgt   25           19.000                     ms
Benchmark_07_LessGarbage.reduceWithStrings                                          avgt   25          708.792 ±        0.323   ms/op
Benchmark_07_LessGarbage.reduceWithStrings:·gc.alloc.rate                           avgt   25        12863.484 ±        5.584  MB/sec
Benchmark_07_LessGarbage.reduceWithStrings:·gc.alloc.rate.norm                      avgt   25  10010461623.659 ±      522.563    B/op
Benchmark_07_LessGarbage.reduceWithStrings:·gc.churn.G1_Eden_Space                  avgt   25        12941.823 ±       50.687  MB/sec
Benchmark_07_LessGarbage.reduceWithStrings:·gc.churn.G1_Eden_Space.norm             avgt   25  10071429872.875 ± 39878411.627    B/op
Benchmark_07_LessGarbage.reduceWithStrings:·gc.churn.G1_Survivor_Space              avgt   25            0.059 ±        0.001  MB/sec
Benchmark_07_LessGarbage.reduceWithStrings:·gc.churn.G1_Survivor_Space.norm         avgt   25        46261.312 ±      703.638    B/op
Benchmark_07_LessGarbage.reduceWithStrings:·gc.count                                avgt   25         2413.000                 counts
Benchmark_07_LessGarbage.reduceWithStrings:·gc.time                                 avgt   25         1466.000                     ms
Benchmark_07_LessGarbage_2.reduceParallel                                           avgt   25           76.890 ±        0.331   ms/op
Benchmark_07_LessGarbage_2.reduceParallel:·gc.alloc.rate                            avgt   25          839.758 ±        3.569  MB/sec
Benchmark_07_LessGarbage_2.reduceParallel:·gc.alloc.rate.norm                       avgt   25     71079465.746 ±       68.574    B/op
Benchmark_07_LessGarbage_2.reduceParallel:·gc.churn.G1_Eden_Space                   avgt   25          451.047 ±       22.207  MB/sec
Benchmark_07_LessGarbage_2.reduceParallel:·gc.churn.G1_Eden_Space.norm              avgt   25     38176756.281 ±  1856803.638    B/op
Benchmark_07_LessGarbage_2.reduceParallel:·gc.churn.G1_Old_Gen                      avgt   25          642.411 ±       31.688  MB/sec
Benchmark_07_LessGarbage_2.reduceParallel:·gc.churn.G1_Old_Gen.norm                 avgt   25     54373863.787 ±  2649624.102    B/op
Benchmark_07_LessGarbage_2.reduceParallel:·gc.churn.G1_Survivor_Space               avgt   25            0.477 ±        0.144  MB/sec
Benchmark_07_LessGarbage_2.reduceParallel:·gc.churn.G1_Survivor_Space.norm          avgt   25        40411.618 ±    12264.863    B/op
Benchmark_07_LessGarbage_2.reduceParallel:·gc.count                                 avgt   25          155.000                 counts
Benchmark_07_LessGarbage_2.reduceParallel:·gc.time                                  avgt   25          266.000                     ms
Benchmark_07_LessGarbage_2.reduceSequential                                         avgt   25          166.949 ±        0.456   ms/op
Benchmark_07_LessGarbage_2.reduceSequential:·gc.alloc.rate                          avgt   25          129.955 ±        0.369  MB/sec
Benchmark_07_LessGarbage_2.reduceSequential:·gc.alloc.rate.norm                     avgt   25     23878907.797 ±      142.325    B/op
Benchmark_07_LessGarbage_2.reduceSequential:·gc.churn.G1_Eden_Space                 avgt   25           12.592 ±        2.327  MB/sec
Benchmark_07_LessGarbage_2.reduceSequential:·gc.churn.G1_Eden_Space.norm            avgt   25      2313559.751 ±   427109.737    B/op
Benchmark_07_LessGarbage_2.reduceSequential:·gc.churn.G1_Old_Gen                    avgt   25          180.795 ±       33.618  MB/sec
Benchmark_07_LessGarbage_2.reduceSequential:·gc.churn.G1_Old_Gen.norm               avgt   25     33217953.025 ±  6171680.760    B/op
Benchmark_07_LessGarbage_2.reduceSequential:·gc.churn.G1_Survivor_Space             avgt   25            0.049 ±        0.066  MB/sec
Benchmark_07_LessGarbage_2.reduceSequential:·gc.churn.G1_Survivor_Space.norm        avgt   25         8959.083 ±    12100.837    B/op
Benchmark_07_LessGarbage_2.reduceSequential:·gc.count                               avgt   25           44.000                 counts
Benchmark_07_LessGarbage_2.reduceSequential:·gc.time                                avgt   25           55.000                     ms

     */

    String input = createString(200_000);

    @Benchmark
    public String reduceWithStrings() {
        return input.chars()
                .mapToObj(c -> (char) c)
                .reduce("",
                        (result, c) -> reduce(result, c),
                        (left, right) -> combine(left, right));
    }

    @Benchmark
    public String reduceWithStringBuilders() {
        return input.chars()
                .collect(StringBuilder::new,
                        (result, i) -> reduce(result, (char) i),
                        (left, right) -> combine(left, right))
                .toString();
    }

    public static char toOppositeCase(char c) {
        return isLowerCase(c) ? toUpperCase(c) : toLowerCase(c);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_07_LessGarbage.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }
}
