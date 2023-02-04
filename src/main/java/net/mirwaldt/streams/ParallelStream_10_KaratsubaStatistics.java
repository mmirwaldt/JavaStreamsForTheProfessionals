package net.mirwaldt.streams;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.*;
import static net.mirwaldt.streams.benchmarks.Benchmark_11_ParallelFactorial.factorialParallelInForkJoinPool;
import static net.mirwaldt.streams.benchmarks.Benchmark_11_ParallelFactorial.factorialParallelStream;

/*
Output:
Multiplications:
	Possible at most:
		Karatsuba multiplications: 591
		TomCook multiplications: 196
	factorialParallelStream:
		Result in bits: 1516705
		Karatsuba multiplications: 63
		TomCook multiplications: 63
	factorialParallelInForkJoinPool:
		Result in bits: 1516705
		Karatsuba multiplications: 488
		TomCook multiplications: 126
	karatsubaFactorialParallelStream2:
		Result in bits: 1516705
		Karatsuba multiplications: 556
		TomCook multiplications: 63
	karatsubaFactorialForkJoinPool:
		Result in bits: 1516705
		Karatsuba multiplications: 516
		TomCook multiplications: 126
	tomCookKaratsubaFactorialParallelStream:
		Result in bits: 1516705
		Karatsuba multiplications: 556
		TomCook multiplications: 125
	tomCookKaratsubaFactorialForkJoinPool:
		Result in bits: 1516705
		Karatsuba multiplications: 572
		TomCook multiplications: 188
 */
public class ParallelStream_10_KaratsubaStatistics {
    // We want to find out how many karatsuba multiplications are possible and happen in all solutions
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        BigInteger result = factorialParallelStream(100_000, BigInteger::multiply);
        System.out.println("Multiplications:");
        System.out.println("\tPossible at most:");
        System.out.println("\t\tKaratsuba multiplications: "
                + (result.bitLength() / KARATSUBA_THRESHOLD_IN_BITS - 1)); // -1 because we count pairs
        System.out.println("\t\tTomCook multiplications: "
                + (result.bitLength() / TOM_COOK_THRESHOLD_IN_BITS - 1)); // -1 because we count pairs

        // Let's count Karatsuba numbers first
        MultiplicationWatcher watcher = new MultiplicationWatcher();
        result = factorialParallelStream(100_000, watcher::multiplyAndWatch);
        System.out.println("\tfactorialParallelStream:");
        System.out.println("\t\tResult in bits: " + result.bitLength());
        System.out.println("\t\tKaratsuba multiplications: " + watcher.karatsubaCount());
        System.out.println("\t\tTomCook multiplications: " + watcher.tomCookCount());

        watcher = new MultiplicationWatcher();
        result = factorialParallelInForkJoinPool(100_000, watcher::multiplyAndWatch);
        System.out.println("\tfactorialParallelInForkJoinPool:");
        System.out.println("\t\tResult in bits: " + result.bitLength());
        System.out.println("\t\tKaratsuba multiplications: " + watcher.karatsubaCount());
        System.out.println("\t\tTomCook multiplications: " + watcher.tomCookCount());

        watcher = new MultiplicationWatcher();
        result = karatsubaFactorialParallelStream2(100_000, watcher::multiplyAndWatch);
        System.out.println("\tkaratsubaFactorialParallelStream2:");
        System.out.println("\t\tResult in bits: " + result.bitLength());
        System.out.println("\t\tKaratsuba multiplications: " + watcher.karatsubaCount());
        System.out.println("\t\tTomCook multiplications: " + watcher.tomCookCount());

        watcher = new MultiplicationWatcher();
        result = karatsubaFactorialForkJoinPool(100_000, watcher::multiplyAndWatch);
        System.out.println("\tkaratsubaFactorialForkJoinPool:");
        System.out.println("\t\tResult in bits: " + result.bitLength());
        System.out.println("\t\tKaratsuba multiplications: " + watcher.karatsubaCount());
        System.out.println("\t\tTomCook multiplications: " + watcher.tomCookCount());

        watcher = new MultiplicationWatcher();
        result = tomCookKaratsubaFactorialParallelStream(100_000, watcher::multiplyAndWatch);
        System.out.println("\ttomCookKaratsubaFactorialParallelStream:");
        System.out.println("\t\tResult in bits: " + result.bitLength());
        System.out.println("\t\tKaratsuba multiplications: " + watcher.karatsubaCount());
        System.out.println("\t\tTomCook multiplications: " + watcher.tomCookCount());

        watcher = new MultiplicationWatcher();
        result = tomCookKaratsubaFactorialForkJoinPool(100_000, watcher::multiplyAndWatch);
        System.out.println("\ttomCookKaratsubaFactorialForkJoinPool:");
        System.out.println("\t\tResult in bits: " + result.bitLength());
        System.out.println("\t\tKaratsuba multiplications: " + watcher.karatsubaCount());
        System.out.println("\t\tTomCook multiplications: " + watcher.tomCookCount());
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