package net.mirwaldt.streams;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.*;
import static net.mirwaldt.streams.benchmarks.Benchmark_11_ParallelFactorial.factorialParallelInForkJoinPool;
import static net.mirwaldt.streams.benchmarks.Benchmark_11_ParallelFactorial.factorialParallelStream;
import static net.mirwaldt.streams.experimental.Benchmark_22_ForkJoinPoolFactorial.factorialParallelInForkJoinPoolMinLength;
import static net.mirwaldt.streams.experimental.Benchmark_23_BestSplitStrategy.factorialCompletableFutureSequentialMultiply;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BenchmarkTest_11_ParallelFactorial {
    public static final BigInteger FACTORIAL_OF_5 = BigInteger.valueOf(120);
    public static final BigInteger FACTORIAL_OF_50 =
            new BigInteger("30414093201713378043612608166064768844377641568960512000000000000");

    @Test
    void test_5() {
        assertEquals(FACTORIAL_OF_5, factorialParallelStream(5, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_5, factorialParallelInForkJoinPool(5, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_5, factorialParallelInForkJoinPoolMinLength(5, BigInteger::multiply, 100));
        assertEquals(FACTORIAL_OF_5, karatsubaFactorialParallelStream2(5, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_5, karatsubaFactorialForkJoinPool(5, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_5, tomCookKaratsubaFactorialParallelStream(5, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_5, tomCookKaratsubaFactorialForkJoinPool(5, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_5, factorialCompletableFutureSequentialMultiply(5, BigInteger::multiply));
    }

    @Test
    void test_50() {
        assertEquals(FACTORIAL_OF_50, factorialParallelStream(50, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_50, factorialParallelInForkJoinPool(50, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_50, factorialParallelInForkJoinPoolMinLength(50, BigInteger::multiply, 100));
        assertEquals(FACTORIAL_OF_50, karatsubaFactorialParallelStream2(50, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_50, karatsubaFactorialForkJoinPool(50, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_50, tomCookKaratsubaFactorialParallelStream(50, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_50, tomCookKaratsubaFactorialForkJoinPool(50, BigInteger::multiply));
        assertEquals(FACTORIAL_OF_50, factorialCompletableFutureSequentialMultiply(50, BigInteger::multiply));
    }

    @Test
    void test_1000() {
        BigInteger expected = factorialParallelStream(1000, BigInteger::multiply);
        assertEquals(expected, factorialParallelStream(1000, BigInteger::multiply));
        assertEquals(expected, factorialParallelInForkJoinPool(1000, BigInteger::multiply));
        assertEquals(expected, factorialParallelInForkJoinPoolMinLength(1000, BigInteger::multiply, 100));
        assertEquals(expected, karatsubaFactorialParallelStream2(1000, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialForkJoinPool(1000, BigInteger::multiply));
        assertEquals(expected, tomCookKaratsubaFactorialParallelStream(1000, BigInteger::multiply));
        assertEquals(expected, tomCookKaratsubaFactorialForkJoinPool(1000, BigInteger::multiply));
        assertEquals(expected, factorialCompletableFutureSequentialMultiply(1000, BigInteger::multiply));

    }

    @Test
    void test_1200() {
        BigInteger expected = factorialParallelStream(1200, BigInteger::multiply);
        assertEquals(expected, factorialParallelStream(1200, BigInteger::multiply));
        assertEquals(expected, factorialParallelInForkJoinPool(1200, BigInteger::multiply));
        assertEquals(expected, factorialParallelInForkJoinPoolMinLength(1200, BigInteger::multiply, 100));
        assertEquals(expected, karatsubaFactorialParallelStream2(1200, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialForkJoinPool(1200, BigInteger::multiply));
        assertEquals(expected, tomCookKaratsubaFactorialParallelStream(1200, BigInteger::multiply));
        assertEquals(expected, tomCookKaratsubaFactorialForkJoinPool(1200, BigInteger::multiply));
        assertEquals(expected, factorialCompletableFutureSequentialMultiply(1200, BigInteger::multiply));
    }

    @Test
    void test_10000() {
        BigInteger expected = factorialParallelStream(10000, BigInteger::multiply);
        assertEquals(expected, factorialParallelInForkJoinPool(10000, BigInteger::multiply));
        assertEquals(expected, factorialParallelInForkJoinPoolMinLength(10000, BigInteger::multiply, 100));
        assertEquals(expected, karatsubaFactorialParallelStream2(10000, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialForkJoinPool(10000, BigInteger::multiply));
        assertEquals(expected, tomCookKaratsubaFactorialParallelStream(10000, BigInteger::multiply));
        assertEquals(expected, tomCookKaratsubaFactorialForkJoinPool(10000, BigInteger::multiply));
        assertEquals(expected, factorialCompletableFutureSequentialMultiply(10000, BigInteger::multiply));
    }

    @Test
    void test_100000() {
        BigInteger expected = factorialParallelStream(100000, BigInteger::multiply);
        assertEquals(expected, factorialParallelInForkJoinPool(100000, BigInteger::multiply));
        assertEquals(expected, factorialParallelInForkJoinPoolMinLength(100000, BigInteger::multiply, 100));
        assertEquals(expected, karatsubaFactorialParallelStream2(100000, BigInteger::multiply));
        assertEquals(expected, karatsubaFactorialForkJoinPool(100000, BigInteger::multiply));
        assertEquals(expected, tomCookKaratsubaFactorialParallelStream(100000, BigInteger::multiply));
        assertEquals(expected, tomCookKaratsubaFactorialForkJoinPool(100000, BigInteger::multiply));
        assertEquals(expected, factorialCompletableFutureSequentialMultiply(100000, BigInteger::multiply));
    }
}
