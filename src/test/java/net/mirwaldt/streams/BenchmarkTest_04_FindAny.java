package net.mirwaldt.streams;

import net.mirwaldt.streams.benchmarks.Benchmark_04_FindAny;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SuppressWarnings("NewClassNamingConvention")
public class BenchmarkTest_04_FindAny {
    @Test
    void testResults() {
        Benchmark_04_FindAny benchmark = new Benchmark_04_FindAny();
        long expected = benchmark.findFirstLoop();
        assertEquals(expected, benchmark.findFirstSequentialStream());
        assertEquals(expected, benchmark.findFirstParallelStream());
        assertEquals(expected, benchmark.findAnyParallelStream());
    }
}
