package net.mirwaldt.streams;

import net.mirwaldt.streams.benchmarks.Benchmark_01_Range_NoIterate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("NewClassNamingConvention")
public class BenchmarkTest_01_Range_NoIterate {
    @Test
    void testResults() {
        Benchmark_01_Range_NoIterate benchmark = new Benchmark_01_Range_NoIterate();
        long expected = benchmark.sequentialLoop();
        assertEquals(expected, benchmark.filterLimitSequentialStream());
        assertEquals(expected, benchmark.filterLimitParallelStream());
        assertEquals(expected, benchmark.rangeClosedParallel());
    }
}
