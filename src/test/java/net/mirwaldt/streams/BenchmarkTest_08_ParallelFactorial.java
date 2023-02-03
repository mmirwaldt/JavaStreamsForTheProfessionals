package net.mirwaldt.streams;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import static net.mirwaldt.streams.benchmarks.Benchmark_08_ParallelFactorial.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BenchmarkTest_08_ParallelFactorial {
    public static final BigInteger FACTORIAL_OF_5 = BigInteger.valueOf(120);
    public static final BigInteger FACTORIAL_OF_50 = new BigInteger("30414093201713378043612608166064768844377641568960512000000000000");

    @Test
    void test_5() {
        assertEquals(FACTORIAL_OF_5, factorialParallelStreamSequentialMultiply(5));
        assertEquals(FACTORIAL_OF_5, factorialParallelStreamParallelMultiply(5));
        assertEquals(FACTORIAL_OF_5, factorialParallelInForkJoinPoolSequentialMultiply(5));
        assertEquals(FACTORIAL_OF_5, factorialParallelInForkJoinPoolParallelMultiply(5));
        assertEquals(FACTORIAL_OF_5, karatsubaFactorialParallelStream(5, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_5, karatsubaFactorialParallelStream(5, BigInteger::parallelMultiply));
        assertEquals(FACTORIAL_OF_5, karatsubaFactorialForkJoinPoolSequentialMultiply(5));
        assertEquals(FACTORIAL_OF_5, karatsubaFactorialForkJoinPoolParallelMultiply(5));
    }

    @Test
    void test_50() {
        assertEquals(FACTORIAL_OF_50, factorialParallelStreamSequentialMultiply(50));
        assertEquals(FACTORIAL_OF_50, factorialParallelStreamParallelMultiply(50));
        assertEquals(FACTORIAL_OF_50, factorialParallelInForkJoinPoolSequentialMultiply(50));
        assertEquals(FACTORIAL_OF_50, factorialParallelInForkJoinPoolParallelMultiply(50));
        assertEquals(FACTORIAL_OF_50, karatsubaFactorialParallelStream(50, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_50, karatsubaFactorialParallelStream(50, BigInteger::parallelMultiply));
        assertEquals(FACTORIAL_OF_50, karatsubaFactorialForkJoinPoolSequentialMultiply(50));
        assertEquals(FACTORIAL_OF_50, karatsubaFactorialForkJoinPoolParallelMultiply(50));
    }

    @Test
    void test_1000() {
        BigInteger expected = factorialParallelStreamParallelMultiply(1000);
        assertEquals(expected, factorialParallelStreamSequentialMultiply(1000));
        assertEquals(expected, factorialParallelInForkJoinPoolSequentialMultiply(1000));
        assertEquals(expected, factorialParallelInForkJoinPoolParallelMultiply(1000));
        assertEquals(expected, karatsubaFactorialParallelStream(1000, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialParallelStream(1000, BigInteger::parallelMultiply));
        assertEquals(expected, karatsubaFactorialForkJoinPoolSequentialMultiply(1000));
        assertEquals(expected, karatsubaFactorialForkJoinPoolParallelMultiply(1000));

    }

    @Test
    void test_1200() {
        BigInteger expected = factorialParallelStreamParallelMultiply(1200);
        assertEquals(expected, factorialParallelStreamSequentialMultiply(1200));
        assertEquals(expected, factorialParallelInForkJoinPoolSequentialMultiply(1200));
        assertEquals(expected, factorialParallelInForkJoinPoolParallelMultiply(1200));
        assertEquals(expected, karatsubaFactorialParallelStream(1200, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialParallelStream(1200, BigInteger::parallelMultiply));
        assertEquals(expected, karatsubaFactorialForkJoinPoolSequentialMultiply(1200));
        assertEquals(expected, karatsubaFactorialForkJoinPoolParallelMultiply(1200));
    }

    @Test
    void test_10000_twoSplits() {
        BigInteger expected = factorialParallelStreamParallelMultiply(10000);
        assertEquals(expected, factorialParallelStreamSequentialMultiply(10000));
        assertEquals(expected, factorialParallelInForkJoinPoolSequentialMultiply(10000));
        assertEquals(expected, factorialParallelInForkJoinPoolParallelMultiply(10000));
        assertEquals(expected, karatsubaFactorialParallelStream(10000, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialParallelStream(10000, BigInteger::parallelMultiply));
        assertEquals(expected, karatsubaFactorialForkJoinPoolSequentialMultiply(10000));
        assertEquals(expected, karatsubaFactorialForkJoinPoolParallelMultiply(10000));
    }
}
