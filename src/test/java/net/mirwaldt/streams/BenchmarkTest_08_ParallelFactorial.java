package net.mirwaldt.streams;

import net.mirwaldt.streams.benchmarks.Benchmark_08_ParallelFactorial;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BenchmarkTest_08_ParallelFactorial {

    public static final BigInteger FACTORIAL_OF_50 = new BigInteger("30414093201713378043612608166064768844377641568960512000000000000");

    @Test
    void test_5() {
        Benchmark_08_ParallelFactorial factorial = new Benchmark_08_ParallelFactorial();
        assertEquals(BigInteger.valueOf(120), factorial.factorialParallelStreamSequentialMultiply(5));
        assertEquals(BigInteger.valueOf(120), factorial.factorialParallelStreamParallelMultiply(5));
        assertEquals(BigInteger.valueOf(120), factorial.factorialCompletableFutureSequentialMultiply(5));
        assertEquals(BigInteger.valueOf(120), factorial.factorialCompletableFutureParallelMultiply(5));
        assertEquals(BigInteger.valueOf(120), factorial.factorialParallelInForkJoinPoolSequentialMultiply(5));
        assertEquals(BigInteger.valueOf(120), factorial.factorialParallelInForkJoinPoolParallelMultiply(5));
        assertEquals(BigInteger.valueOf(120), factorial.factorialParallelInForkJoinPoolKaratsubaParallelMultiply(5));
        assertEquals(BigInteger.valueOf(120), factorial.factorialCompletableFutureParallelMultiply(5));
    }

    @Test
    void test_50() {
        Benchmark_08_ParallelFactorial factorial = new Benchmark_08_ParallelFactorial();
        assertEquals(FACTORIAL_OF_50, factorial.factorialParallelStreamSequentialMultiply(50));
        assertEquals(FACTORIAL_OF_50, factorial.factorialParallelStreamParallelMultiply(50));
        assertEquals(FACTORIAL_OF_50, factorial.factorialCompletableFutureSequentialMultiply(50));
        assertEquals(FACTORIAL_OF_50, factorial.factorialCompletableFutureParallelMultiply(50));
        assertEquals(FACTORIAL_OF_50, factorial.factorialParallelInForkJoinPoolSequentialMultiply(50));
        assertEquals(FACTORIAL_OF_50, factorial.factorialParallelInForkJoinPoolKaratsubaParallelMultiply(50));
        assertEquals(FACTORIAL_OF_50, factorial.factorialCompletableFutureParallelMultiply(50));
    }

    @Test
    void test_1000_oneSplit() {
        Benchmark_08_ParallelFactorial factorial = new Benchmark_08_ParallelFactorial();
        assertEquals(factorial.factorialParallelStreamParallelMultiply(1000),
                factorial.factorialCompletableFutureParallelMultiply(1000));
    }

    @Test
    void test_1200_twoSplits() {
        Benchmark_08_ParallelFactorial factorial = new Benchmark_08_ParallelFactorial();
        assertEquals(factorial.factorialParallelStreamParallelMultiply(1200),
                factorial.factorialCompletableFutureParallelMultiply(1200));
    }

    @Test
    void test_10000_twoSplits() {
        Benchmark_08_ParallelFactorial factorial = new Benchmark_08_ParallelFactorial();
        assertEquals(factorial.factorialParallelStreamParallelMultiply(10000),
                factorial.factorialCompletableFutureParallelMultiply(10000));
    }
}
