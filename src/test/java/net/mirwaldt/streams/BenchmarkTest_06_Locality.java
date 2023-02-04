package net.mirwaldt.streams;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static net.mirwaldt.streams.benchmarks.Benchmark_06_Locality.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("NewClassNamingConvention")
public class BenchmarkTest_06_Locality {
    @Test
    void test() {
        int N = 100;
        ArrayList<Integer> arrayList = new ArrayList<>(N);
        int[] array = new int[N];
        for (int i = 0; i < N; i++) {
            array[i] = i + 1;
            arrayList.add(i + 1);
        }
        ArrayList<Integer> sortedArrayList = new ArrayList<>(arrayList);

        long expected = N * (N+1) / 2; // Gauss sum formula
        assertEquals(expected, sumArraySequential(array));
        assertEquals(expected, sumArrayParallel(array));
        assertEquals(expected, sumArrayListSequential(arrayList));
        assertEquals(expected, sumArrayListParallel(arrayList));
        assertEquals(expected, sumSortedArrayListSequential(sortedArrayList));
        assertEquals(expected, sumSortedArrayListParallel(sortedArrayList));
    }
}
