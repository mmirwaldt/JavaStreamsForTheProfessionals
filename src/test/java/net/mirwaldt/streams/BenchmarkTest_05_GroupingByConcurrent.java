package net.mirwaldt.streams;

import net.mirwaldt.streams.benchmarks.Benchmark_05_GroupingByConcurrent;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SuppressWarnings("NewClassNamingConvention")
public class BenchmarkTest_05_GroupingByConcurrent {
    @Test
    void testResults() {
        Benchmark_05_GroupingByConcurrent benchmark = new Benchmark_05_GroupingByConcurrent();
        Map<Long, List<Long>> expected = sortedLists(benchmark.groupByInLoop());
        assertEquals(expected, sortedLists(benchmark.groupByInSequentialStream()));
        assertEquals(expected, sortedLists(benchmark.groupByInParallelStream()));
        assertEquals(expected, sortedLists(benchmark.groupByConcurrentInParallelStream()));
    }

    static Map<Long, List<Long>> sortedLists(Map<Long, List<Long>> map) {
        map.values().forEach(Collections::sort);
        return map;
    }
}
