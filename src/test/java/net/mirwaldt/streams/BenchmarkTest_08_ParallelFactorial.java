package net.mirwaldt.streams;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static net.mirwaldt.streams.benchmarks.Benchmark_08_ParallelFactorial.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BenchmarkTest_08_ParallelFactorial {
    public static final BigInteger FACTORIAL_OF_5 = BigInteger.valueOf(120);
    public static final BigInteger FACTORIAL_OF_50 = new BigInteger("30414093201713378043612608166064768844377641568960512000000000000");

    @Test
    void test_5() {
        assertEquals(FACTORIAL_OF_5, factorialParallelStream(5, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_5, factorialParallelInForkJoinPool(5, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_5, karatsubaFactorialParallelStream2(5, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_5, karatsubaFactorialForkJoinPool(5, BigInteger::multiply));
    }

    @Test
    void test_50() {
        assertEquals(FACTORIAL_OF_50, factorialParallelStream(50, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_50, factorialParallelInForkJoinPool(50, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_50, karatsubaFactorialParallelStream2(50, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_50, karatsubaFactorialForkJoinPool(50, BigInteger::multiply));
    }

    @Test
    void test_1000() {
        BigInteger expected = factorialParallelStream(1000, BigInteger::multiply);
        assertEquals(expected, factorialParallelStream(1000, BigInteger::multiply));
        assertEquals(expected, factorialParallelInForkJoinPool(1000, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialParallelStream2(1000, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialForkJoinPool(1000, BigInteger::multiply));

    }

    @Test
    void test_1200() {
        BigInteger expected = factorialParallelStream(1200, BigInteger::multiply);
        assertEquals(expected, factorialParallelStream(1200, BigInteger::multiply));
        assertEquals(expected, factorialParallelInForkJoinPool(1200, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialParallelStream2(1200, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialForkJoinPool(1200, BigInteger::multiply));
    }

    @Test
    void test_10000_twoSplits() {
        BigInteger expected = factorialParallelStream(10000, BigInteger::multiply);
        assertEquals(expected, factorialParallelInForkJoinPool(10000, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialParallelStream2(10000, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialForkJoinPool(10000, BigInteger::multiply));
    }
}
