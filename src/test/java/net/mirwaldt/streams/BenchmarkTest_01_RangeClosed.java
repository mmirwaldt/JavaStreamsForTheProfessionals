package net.mirwaldt.streams;

import net.mirwaldt.streams.benchmarks.Benchmark_01_RangeClosed;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("NewClassNamingConvention")
public class BenchmarkTest_01_RangeClosed {
    @Test
    void testResults() {
        Benchmark_01_RangeClosed benchmark = new Benchmark_01_RangeClosed();
        long expected = benchmark.sequentialLoop();
        assertEquals(expected, benchmark.filterLimitSequentialStream());
        assertEquals(expected, benchmark.filterLimitParallelStream());
        assertEquals(expected, benchmark.rangeClosedSequentialStream());
        assertEquals(expected, benchmark.rangeClosedParallelStream());
    }
}
