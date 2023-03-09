package net.mirwaldt.streams.experimental;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.math.BigInteger.ONE;
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.accumulate;
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.combine;

@SuppressWarnings("DuplicatedCode")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_25_PrimeFactorization {
    /*
        Benchmark                                                Mode  Cnt    Score   Error  Units
        Benchmark_25_PrimeFactorization.primeFactorization       avgt   25  137.871 ± 1.535  ms/op
        Benchmark_25_PrimeFactorization.primeFactorizationInFJP  avgt   25   45.620 ± 0.043  ms/op
     */

    int N = 100_000;

    DefaultPrimeSource primeSource = new DefaultPrimeSource();

    List<Integer> primes;

    int maxLog = maxLog(N, 2);
    FactorialApproximator[] approximators = new FactorialApproximator[maxLog + 1];

    @Setup
    public void setup() {
        approximators[0] = (n, prime) -> 0;
        approximators[1] = (n, prime) -> 1;
        for (int i = 2; i <= maxLog; i++) {
            approximators[i] = new NGroupsFactorialApproximator(i);
        }
        primes = IntStream.rangeClosed(2, N)
                .filter(primeSource::isPrime)
                .boxed()
                .toList();
    }

//    @Benchmark
//    public BigInteger primeFactorization() {
//        return primeFactorization(N, primes, approximators, BigInteger::parallelMultiply);
//    }


    @Benchmark
    public BigInteger primeFactorizationInFJP() {
        return primeFactorizationInFJP(new PrimeFactorizationFactorialTask(N, primes, approximators, BigInteger::parallelMultiply));
    }

    /*
        Approximating appears to be fast enough:
        # Run progress: 0.00% complete, ETA 00:08:20
        # Fork: 1 of 5
        # Warmup Iteration   1: 0.079 ms/op
        # Warmup Iteration   2: 0.078 ms/op
        # Warmup Iteration   3: 0.078 ms/op
     */
//    @Benchmark
//    public long primeFactorization() {
//        return primes.parallelStream()
//                .mapToLong(prime -> approximate(N, approximators, prime))
//                .sum();
//    }

    public static BigInteger primeFactorizationInFJP(int n, BinaryOperator<BigInteger> multiply) {
        DefaultPrimeSource primeSource = new DefaultPrimeSource();
        int maxLog = maxLog(n, 2);
        FactorialApproximator[] approximators = new FactorialApproximator[maxLog + 1];
        approximators[0] = (m, prime) -> 0;
        approximators[1] = (m, prime) -> 1;
        for (int i = 2; i <= maxLog; i++) {
            approximators[i] = new NGroupsFactorialApproximator(i);
        }
        List<Integer> primes = IntStream.rangeClosed(3, n)
                .filter(primeSource::isPrime)
                .boxed()
                .toList();
        return primeFactorizationInFJP(new PrimeFactorizationFactorialTask(n, primes, approximators, multiply));
    }

    public static BigInteger primeFactorizationInFJP(PrimeFactorizationFactorialTask factorizationFactorialTask) {
        return ForkJoinPool.commonPool().invoke(factorizationFactorialTask);
    }

    public static BigInteger primeFactorization(int n, BinaryOperator<BigInteger> multiply) {
        DefaultPrimeSource primeSource = new DefaultPrimeSource();
        List<Integer> primes = IntStream.rangeClosed(3, n)
                .filter(primeSource::isPrime)
                .boxed()
                .toList();

        int maxLog = maxLog(n, 2);
        FactorialApproximator[] approximators = new FactorialApproximator[maxLog + 1];
        approximators[0] = (m, prime) -> 1;
        approximators[1] = (m, prime) -> 1;
        for (int i = 2; i <= maxLog; i++) {
            approximators[i] = new NGroupsFactorialApproximator(i);
        }
        return primeFactorization(n, primes, approximators, multiply);
    }

    public static BigInteger primeFactorization(
            int n, List<Integer> primes, FactorialApproximator[] approximators, BinaryOperator<BigInteger> multiply) {
        int squaredN = n * n;
        BigInteger power2Result = ONE.shiftLeft((int) approximate(n, squaredN, approximators, 2));
        BigInteger[] results = primes.parallelStream()
                .collect(() -> new BigInteger[]{ONE, ONE, ONE},
                        (array, prime) -> power5(prime, approximate(n, squaredN, approximators, prime), array, multiply),
                        (left, right) -> combine(left, right, multiply));
        accumulate(results, power2Result, multiply);
        return multiply.apply(results[0], multiply.apply(results[1], results[2]));
    }

    private static long approximate(int n, int squaredN, FactorialApproximator[] approximators, Integer p) {
        if (p <= squaredN) {
            return approximators[max(2, maxLog(n, p))].approximate(n, p);
        } else {
            return 1;
        }
    }

    private static int maxLog(long n, long prime) {
        int result = 0;
        long remaining = n;
        while (0 < remaining) {
            remaining = remaining / prime;
            result++;
        }
        return result;
    }

    public static void power5(long base, long exponent, BigInteger[] finalResults, BinaryOperator<BigInteger> multiply) {
        int maxLog = maxLog(exponent, 2) - 1;
        BigInteger[] intermediateResults = new BigInteger[maxLog];
        recursivePower5(base, exponent, finalResults, multiply, intermediateResults);
    }

    public static void recursivePower5(long base, long exponent, BigInteger[] finalResults,
                                       BinaryOperator<BigInteger> multiply, BigInteger[] intermediateResults) {
        BigInteger baseBigInt = BigInteger.valueOf(base);
        BigInteger result = baseBigInt;
        int i = 1;
        int j = 0;
        for (; 2L * i < exponent; i *= 2) {
            if (intermediateResults[j] == null) {
                intermediateResults[j] = multiply.apply(result, result);
            }
            result = intermediateResults[j];
            j++;
        }
        accumulate(finalResults, result, multiply);

        long remaining = exponent - i;
        if (2 <= remaining) {
            recursivePower5(base, remaining, finalResults, multiply, intermediateResults);
        } else if (remaining == 1) {
            accumulate(finalResults, baseBigInt, multiply);
        }
    }

    public static class PrimeFactorizationFactorialTask extends RecursiveTask<BigInteger> {
        private final FactorialApproximator[] approximators;

        private final int n;
        private final int squaredN;

        private final PrimeRange primeRange;

        private PowerRange powerRange; // No, no power rangers :-D

        private BigInteger initialValue = ONE;
        private final BinaryOperator<BigInteger> multiply;
        private BigInteger prime;

        public PrimeFactorizationFactorialTask(
                int n, List<Integer> primes, FactorialApproximator[] approximators, BinaryOperator<BigInteger> multiply) {
            this.n = n;
            this.squaredN = n * n;
            this.primeRange = new PrimeRange(0, primes.size(), primes);
            this.approximators = approximators;
            this.initialValue = ONE.shiftLeft((int) approximate(n, squaredN, approximators, 2));
            this.multiply = multiply;
        }

        public PrimeFactorizationFactorialTask(
                int n,
                int squaredN,
                PrimeRange primeRange,
                FactorialApproximator[] approximators,
                BinaryOperator<BigInteger> multiply) {
            this.n = n;
            this.squaredN = squaredN;
            this.primeRange = primeRange;
            this.approximators = approximators;
            this.multiply = multiply;
        }

        public PrimeFactorizationFactorialTask(
                int n,
                int squaredN,
                PrimeRange primeRange,
                BigInteger prime,
                PowerRange powerRange,
                FactorialApproximator[] approximators,
                BinaryOperator<BigInteger> multiply) {
            this.n = n;
            this.squaredN = squaredN;
            this.primeRange = primeRange;
            this.prime = prime;
            this.approximators = approximators;
            this.multiply = multiply;
            this.powerRange = powerRange;
        }

        @Override
        protected BigInteger compute() {
            int length = primeRange.length();
            if (length == 1) {
                if (powerRange == null) {
                    initPowerRange();
                }
                if (powerRange.length() == 1) {
                    return prime;
                } else if (powerRange.length() == 2) {
                    return powerRange.power(2, prime, multiply);
                } else {
                    return splitByPowers();
                }
            } else {
                return splitByPrimes(length);
            }
        }

        private BigInteger splitByPowers() {
            long unsquaredExponent = (powerRange.length() - 1) / 2;
            PowerRange leftPowerRange = new PowerRange(
                    powerRange.startExponent(), powerRange.startExponent() + unsquaredExponent, powerRange.prime(), powerRange.powers());
            PrimeFactorizationFactorialTask leftTask = new PrimeFactorizationFactorialTask(
                    n, squaredN, primeRange, prime, leftPowerRange, approximators, multiply);
            long squaredExponent = 2 * unsquaredExponent;
            long remaining = powerRange.length() - squaredExponent;
            assert 0 < remaining;
            leftTask.fork();
            PowerRange rightPowerRange = new PowerRange(
                    powerRange.endExponent() - remaining, powerRange.endExponent(), powerRange.prime(), powerRange.powers());
            PrimeFactorizationFactorialTask rightTask = new PrimeFactorizationFactorialTask(
                    n, squaredN, primeRange, prime, rightPowerRange, approximators, multiply);
            BigInteger rightResult = rightTask.compute();
            BigInteger leftResult = leftTask.join();
            BigInteger squaredLeftResult = powerRange.power(squaredExponent, leftResult, multiply);
            return multiply.apply(squaredLeftResult, rightResult);
        }

        private BigInteger splitByPrimes(int length) {
            int halfLength = length / 2;
            assert 0 < halfLength;
            PrimeRange leftRange = new PrimeRange(
                    primeRange.startPrime(), primeRange.startPrime() + halfLength, primeRange.primes());
            PrimeFactorizationFactorialTask leftTask = new PrimeFactorizationFactorialTask(
                    n, squaredN, leftRange, approximators, multiply);
            leftTask.fork();
            PrimeRange rightRange = new PrimeRange(
                    primeRange.startPrime() + halfLength, primeRange.endPrime(), primeRange.primes());
            PrimeFactorizationFactorialTask rightTask = new PrimeFactorizationFactorialTask(
                    n, squaredN, rightRange, approximators, multiply);
            BigInteger result = multiply.apply(rightTask.compute(), leftTask.join());
            return (initialValue.equals(ONE)) ? result : multiply.apply(initialValue, result);
        }

        private void initPowerRange() {
            int primeAsInt = primeRange.firstPrime();
            prime = BigInteger.valueOf(primeAsInt);
            long exponent = approximate(n, squaredN, approximators, primeAsInt);
            ConcurrentMap<Long, BigInteger> map = null;
            if (1 < exponent) {
                map = new ConcurrentHashMap<>();
            }
            powerRange = new PowerRange(0, exponent, primeAsInt, map);
        }

        record PrimeRange(int startPrime, int endPrime, List<Integer> primes) {

            public PrimeRange(int startPrime, int endPrime, List<Integer> primes) {
                this.startPrime = startPrime;
                this.endPrime = endPrime;
                this.primes = List.copyOf(primes);
            }

            int length() {
                return endPrime - startPrime;
            }

            int firstPrime() {
                return primes.get(startPrime);
            }

            int lastPrime() {
                return primes.get(endPrime - 1);
            }
        }


        record PowerRange(long startExponent, long endExponent, long prime, ConcurrentMap<Long, BigInteger> powers) {
            long length() {
                return endExponent - startExponent;
            }

            BigInteger power(long exponent, BigInteger unsquared, BinaryOperator<BigInteger> multiply) {
                // powers of 2 are bad hash codes because they lead to many hash collisions because of using only the lower bits
                return powers.computeIfAbsent((exponent * 31) / 7, (e) -> multiply.apply(unsquared, unsquared));
            }
        }

        @Override
        public String toString() {
            return "PrimeFactorizationFactorialTask{" +
                    "n=" + n +
                    ", primeRange=" + primeRange +
                    ", powerRange=" + powerRange +
                    ", initialValue=" + initialValue +
                    '}';
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_25_PrimeFactorization.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
