package net.mirwaldt.streams.experimental;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.math.BigInteger.ONE;
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.*;
import static net.mirwaldt.streams.benchmarks.Benchmark_11_ParallelFactorial.factorialParallelStream;

/*
Output:
Multiplications:
	Possible at most:
		Karatsuba multiplications: 99
		TomCook multiplications: 32
	factorialLoop:
		Result in bits: 256909
		Karatsuba multiplications: 0
		TomCook multiplications: 0
	factorialStream:
		Result in bits: 256909
		Karatsuba multiplications: 0
		TomCook multiplications: 0
	tomCookKaratsubaFactorialSequentialStream:
		Result in bits: 256909
		Karatsuba multiplications: 98
		TomCook multiplications: 32
 */
public class SequentialStream_EfficientMultiplicationStatistics {

    public static final int N = 20_000;

    // We want to find out how many karatsuba and tom-cook multiplications are possible and happen in all solutions
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        BigInteger result = factorialParallelStream(N, BigInteger::multiply);
        System.out.println("Multiplications:");
        System.out.println("\tPossible at most:");
        System.out.println("\t\tKaratsuba multiplications: "
                + (result.bitLength() / KARATSUBA_THRESHOLD_IN_BITS - 1)); // -1 because we count pairs
        System.out.println("\t\tTomCook multiplications: "
                + (result.bitLength() / TOM_COOK_THRESHOLD_IN_BITS - 1)); // -1 because we count pairs

        MultiplicationWatcher watcher = new MultiplicationWatcher();
        result = factorialLoop(N, watcher::multiplyAndWatch);
        System.out.println("\tfactorialLoop:");
        System.out.println("\t\tResult in bits: " + result.bitLength());
        System.out.println("\t\tKaratsuba multiplications: " + watcher.karatsubaCount());
        System.out.println("\t\tTomCook multiplications: " + watcher.tomCookCount());

        watcher = new MultiplicationWatcher();
        result = factorialSequentialStream(N, watcher::multiplyAndWatch);
        System.out.println("\tfactorialStream:");
        System.out.println("\t\tResult in bits: " + result.bitLength());
        System.out.println("\t\tKaratsuba multiplications: " + watcher.karatsubaCount());
        System.out.println("\t\tTomCook multiplications: " + watcher.tomCookCount());

        watcher = new MultiplicationWatcher();
        result = tomCookKaratsubaFactorialSequentialStream(N, watcher::multiplyAndWatch);
        System.out.println("\ttomCookKaratsubaFactorialSequentialStream:");
        System.out.println("\t\tResult in bits: " + result.bitLength());
        System.out.println("\t\tKaratsuba multiplications: " + watcher.karatsubaCount());
        System.out.println("\t\tTomCook multiplications: " + watcher.tomCookCount());
    }

    public static BigInteger factorialLoop(int n, BinaryOperator<BigInteger> multiply) {
        BigInteger result = ONE;
        for (int i = 2; i <= n; i++) {
            result = multiply.apply(result, BigInteger.valueOf(i));
        }
        return result;
    }

    public static BigInteger factorialSequentialStream(int n, BinaryOperator<BigInteger> multiply) {
        return LongStream.rangeClosed(2, n)
                .mapToObj(BigInteger::valueOf)
                .reduce(multiply)
                .orElse(ONE);
    }

    public static BigInteger tomCookKaratsubaFactorialSequentialStream(int n, BinaryOperator<BigInteger> multiply) {
        BigInteger[] results = IntStream.rangeClosed(2, n)
                .mapToObj(BigInteger::valueOf)
                .collect(() -> new BigInteger[]{ONE, ONE, ONE},
                        (array, i) -> accumulate(array, i, multiply),
                        (left, right) -> combine(left, right, multiply)
                );
        return results[0].multiply(results[1]).multiply(results[2]);
    }

    static class MultiplicationWatcher {
        private final AtomicLong karatsubaCounter = new AtomicLong();
        private final AtomicLong tomCookCounter = new AtomicLong();

        public BigInteger multiplyAndWatch(BigInteger left, BigInteger right) {
            if(KARATSUBA_THRESHOLD_IN_BITS <= left.bitLength() && KARATSUBA_THRESHOLD_IN_BITS <= right.bitLength()) {
                karatsubaCounter.incrementAndGet();
            }
            if(TOM_COOK_THRESHOLD_IN_BITS <= left.bitLength() && TOM_COOK_THRESHOLD_IN_BITS <= right.bitLength()) {
                tomCookCounter.incrementAndGet();
            }
            return left.multiply(right);
        }

        public long karatsubaCount() {
            return karatsubaCounter.get();
        }
        public long tomCookCount() {
            return tomCookCounter.get();
        }
    }
}
