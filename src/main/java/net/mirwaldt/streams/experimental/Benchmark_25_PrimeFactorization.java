package net.mirwaldt.streams.experimental;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

import static java.math.BigInteger.ONE;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.accumulate;
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.combine;

@SuppressWarnings("DuplicatedCode")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_25_PrimeFactorization {

    /*

     */
    int N = 100_000;

    DefaultPrimeSource primeSource = new DefaultPrimeSource();
    SortedSet<Integer> primes;

    int maxLog = maxLog(N, 2);
    FactorialApproximator[] approximators = new FactorialApproximator[maxLog + 1];

//    Map<Integer, Long> primeFactorization;

    @Setup
    public void setup() {
        approximators[0] = (n, prime) -> 1;
        approximators[1] = (n, prime) -> 1;
        for (int i = 2; i <= maxLog; i++) {
            approximators[i] = new NGroupsFactorialApproximator(i);
        }
        primes = IntStream.rangeClosed(2, N)
                .filter(primeSource::isPrime)
                .boxed()
                .collect(toCollection(TreeSet::new));
//        primeFactorization = primes.stream()
//                .parallel()
//                .unordered()
//                .collect(toMap(p -> p, p -> approximate(N, approximators, p)));
//        System.out.println("Setup");
    }

    @Benchmark
    public BigInteger primeFactorization() {
        return primeFactorization(N, primes, approximators, BigInteger::parallelMultiply);
    }

//    @Benchmark
//    public Map<Integer, Long> primeFactorizationAloneParallel() {
//        return primes.stream()
//                .parallel()
//                .unordered()
//                .collect(toMap(p -> p, p -> approximate(N, approximators, p)));
//    }

//    @Benchmark
//    public Map<Integer, Long> primeFactorizationAloneSequential() {
//        return primes.stream()
//                .collect(toMap(p -> p, p -> approximate(N, approximators, p)));
//    }

//    @Benchmark
//    public BigInteger primeFactorizationMultiplication() {
//        System.out.println("primeFactorizationMultiplication");
//        return primeFactorization(primeFactorization, BigInteger::parallelMultiply);
//    }


    public static BigInteger primeFactorization(int n, BinaryOperator<BigInteger> multiply) {
        DefaultPrimeSource primeSource = new DefaultPrimeSource();
        SortedSet<Integer> primes = IntStream.rangeClosed(2, n)
                .filter(primeSource::isPrime)
                .boxed()
                .collect(toCollection(TreeSet::new));

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
            int n, SortedSet<Integer> primes, FactorialApproximator[] approximators, BinaryOperator<BigInteger> multiply) {
        NavigableMap<Integer, Long> primeFactorization = primes.stream()
                .filter(i -> i * 2 <= n)
                .parallel()
                .unordered()
                .collect(toMap(p -> p, p -> approximate(n, approximators, p), (l, r) -> l, TreeMap::new));

        primes.stream()
                .filter(i -> n < 2 * i)
                .forEach(i -> primeFactorization.put(i, 1L));
//        System.out.println(primeFactorization);

        Integer firstPrimeWithExponent1 = primeFactorization.entrySet().stream()
                .filter(entry -> entry.getValue() == 1)
                .map(Map.Entry::getKey)
                .findFirst().orElseThrow(() -> new RuntimeException("No result available"));

        BigInteger power2Result = ONE.shiftLeft(primeFactorization.remove(2).intValue());
        BigInteger[] tailMapResults = primeFactorization
                .tailMap(firstPrimeWithExponent1, true)
                .keySet().stream()
                .collect(() -> new BigInteger[]{power2Result, ONE, ONE}, // may not be parallel with power2Result as initial value
                        (array, prime) -> accumulate(array, BigInteger.valueOf(prime), multiply),
                        (left, right) -> combine(left, right, multiply));

//        System.out.println(primeFactorization.values().size());
//        System.out.println(primeFactorization.values().stream().mapToLong(i -> i).sum());
//        System.out.println(primeFactorization.values().stream().filter(v -> v == 1).count());

        BigInteger[] headMapResults = primeFactorization
                .headMap(firstPrimeWithExponent1)
                .entrySet().stream()
                .parallel()
                .collect(() -> new BigInteger[]{ONE, ONE, ONE},
                        (array, entry) -> power6(entry.getKey(), entry.getValue(), array, multiply),
                        (left, right) -> combine(left, right, multiply));
        combine(headMapResults, tailMapResults, multiply);
        return multiply.apply(headMapResults[0], multiply.apply(headMapResults[1], headMapResults[2]));
    }

    private static long approximate(int n, FactorialApproximator[] approximators, Integer p) {
        return approximators[maxLog(n, p)].approximate(n, p);
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

    private static BigInteger power(long base, long exponent, BinaryOperator<BigInteger> multiply) {
        BigInteger result = ONE;
        for (int i = 0; i < exponent; i++) {
            result = multiply.apply(result, BigInteger.valueOf(base));
        }
        return result;
    }

    private static BigInteger power2(long base, long exponent, BinaryOperator<BigInteger> multiply) {
        BigInteger result = ONE;
        if (0 < exponent) {
            BigInteger baseBigInt = BigInteger.valueOf(base);
            result = baseBigInt;
            int i = 1;
            for (; 2L * i < exponent; i *= 2) {
                result = multiply.apply(result, result);
            }
            long remaining = exponent - i;
            if (2 <= remaining) {
                result = multiply.apply(power2(base, remaining, multiply), result);
            } else if (remaining == 1) {
                result = multiply.apply(result, baseBigInt);
            }
        }
        return result;
    }

    private static void power3(long base, long exponent, BigInteger[] results, BinaryOperator<BigInteger> multiply) {
        if (0 < exponent) {
            BigInteger baseBigInt = BigInteger.valueOf(base);
            BigInteger result = baseBigInt;
            int i = 1;
            for (; 2L * i < exponent; i *= 2) {
                result = multiply.apply(result, result);
            }
            accumulate(results, result, multiply);

            long remaining = exponent - i;
            if (2 <= remaining) {
                power3(base, remaining, results, multiply);
            } else if (remaining == 1) {
                accumulate(results, baseBigInt, multiply);
            }
        }
    }

    private static void power4(long base, long exponent, BigInteger[] results, BinaryOperator<BigInteger> multiply) {
        new PowerMethodObject(results, multiply).power(base, exponent);
    }

    static class PowerMethodObject {
        private BigInteger[] finalResults;
        private BinaryOperator<BigInteger> multiply;
        private BigInteger[] intermediateResults;

        public PowerMethodObject(BigInteger[] finalResults, BinaryOperator<BigInteger> multiply) {
            this.finalResults = finalResults;
            this.multiply = multiply;
        }

        public void power(long base, long exponent) {
            int maxLog = maxLog(exponent, 2) - 1;
            intermediateResults = new BigInteger[maxLog];
            recursivePower(base, exponent);
        }

        public void recursivePower(long base, long exponent) {
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
                recursivePower(base, remaining);
            } else if (remaining == 1) {
                accumulate(finalResults, baseBigInt, multiply);
            }
        }
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

    private static void power6(long base, long exponent, BigInteger[] results, BinaryOperator<BigInteger> multiply) {
        BigInteger baseBigInt = BigInteger.valueOf(base);
        for (int i = 0; i < exponent; i++) {
            accumulate(results, baseBigInt, multiply);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_25_PrimeFactorization.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
