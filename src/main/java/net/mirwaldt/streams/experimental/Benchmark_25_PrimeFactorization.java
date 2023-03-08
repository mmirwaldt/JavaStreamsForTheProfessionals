package net.mirwaldt.streams.experimental;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.math.BigInteger.ONE;
import static java.util.stream.Collectors.toCollection;
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.accumulate;
import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.combine;

@SuppressWarnings("DuplicatedCode")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_25_PrimeFactorization {

    /*
# Warmup Iteration   1: 48.362 ms/op

     */
    int N = 100_000;

    DefaultPrimeSource primeSource = new DefaultPrimeSource();

    List<Integer> primes;

    int maxLog = maxLog(N, 2);
    FactorialApproximator[] approximators = new FactorialApproximator[maxLog + 1];

//    Map<Integer, Long> primeFactorization;

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
//        primes = IntStream.rangeClosed(2, N)
//                .filter(primeSource::isPrime)
//                .boxed()
//                .collect(toCollection(TreeSet::new));
//        primeFactorization = primes.stream()
//                .parallel()
//                .unordered()
//                .collect(toMap(p -> p, p -> approximate(N, approximators, p)));
//        System.out.println("Setup");
    }

//    @Benchmark
//    public BigInteger primeFactorization() {
//        return primeFactorization(N, primes, approximators, BigInteger::parallelMultiply);
//    }

    @Benchmark
    public BigInteger primeFactorizationInFJP() {
        return primeFactorizationInFJP(new PrimeFactorizationFactorialTask(N, primes, approximators, BigInteger::parallelMultiply));
    }

    public static BigInteger primeFactorizationInFJP(int n, BinaryOperator<BigInteger> multiply) {
        DefaultPrimeSource primeSource = new DefaultPrimeSource();
        int maxLog = maxLog(n, 2);
        FactorialApproximator[] approximators = new FactorialApproximator[maxLog + 1];
        approximators[0] = (m, prime) -> 0;
        approximators[1] = (m, prime) -> 1;
        for (int i = 2; i <= maxLog; i++) {
            approximators[i] = new NGroupsFactorialApproximator(i);
        }
        List<Integer> primes = IntStream.rangeClosed(2, n)
                .filter(primeSource::isPrime)
                .boxed()
                .toList();

//        long start = System.currentTimeMillis();
        BigInteger result = primeFactorizationInFJP(new PrimeFactorizationFactorialTask(n, primes, approximators, multiply));
//        long end = System.currentTimeMillis();
//        System.out.println(n + " : " + result.bitLength() + " after " + (end - start) + "ms");
        return result;
    }

    public static BigInteger primeFactorizationInFJP(PrimeFactorizationFactorialTask factorizationFactorialTask) {
        return ForkJoinPool.commonPool().invoke(factorizationFactorialTask);
    }

//    @Benchmark
//    public Map<Integer, Long> primeFactorizationParallel1() {
//        return primes.stream()
//                .filter(i -> i * 2 <= N)
//                .parallel()
//                .collect(toMap(p -> p, p -> approximate(N, approximators, p), (l, r) -> l, TreeMap::new));
//    }
//
//    @Benchmark
//    public Map<Integer, Long> primeFactorizationSequential1() {
//        return primes.stream()
//                .filter(i -> i * 2 <= N)
//                .collect(toMap(p -> p, p -> approximate(N, approximators, p), (l, r) -> l, TreeMap::new));
//    }
//
//
//    @Benchmark
//    public Map<Integer, Long> primeFactorizationParallel2() {
//        NavigableMap<Integer, Long> primeFactorization = primes.stream()
//                .filter(i -> i * 2 <= N)
//                .parallel()
//                .collect(toMap(p -> p, p -> approximate(N, approximators, p), (l, r) -> l, TreeMap::new));
//
//        primes.stream()
//                .filter(i -> N < 2 * i)
//                .forEach(i -> primeFactorization.put(i, 1L));
//        return primeFactorization;
//    }
//
//    @Benchmark
//    public Map<Integer, Long> primeFactorizationAloneSequential2() {
//        NavigableMap<Integer, Long> primeFactorization = primes.stream()
//                .filter(i -> i * 2 <= N)
//                .collect(toMap(p -> p, p -> approximate(N, approximators, p), (l, r) -> l, TreeMap::new));
//
//        primes.stream()
//                .filter(i -> N < 2 * i)
//                .forEach(i -> primeFactorization.put(i, 1L));
//        return primeFactorization;
//    }

//    @Benchmark
//    public BigInteger primeFactorizationMultiplication() {
//        System.out.println("primeFactorizationMultiplication");
//        return primeFactorization(primeFactorization, BigInteger::parallelMultiply);
//    }


    public static BigInteger primeFactorization(int n, BinaryOperator<BigInteger> multiply) {
        DefaultPrimeSource primeSource = new DefaultPrimeSource();
        NavigableSet<Integer> primes = IntStream.rangeClosed(3, n)
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
            int n, NavigableSet<Integer> primes, FactorialApproximator[] approximators, BinaryOperator<BigInteger> multiply) {
//        long start = System.currentTimeMillis();
        BigInteger power2Result = ONE.shiftLeft((int) approximate(n, approximators, 2));
        BigInteger[] results = primes.parallelStream()
                .collect(() -> new BigInteger[]{ONE, ONE, ONE},
                        (array, prime) -> power5(prime, approximate(n, approximators, prime), array, multiply),
                        (left, right) -> combine(left, right, multiply));
        accumulate(results, power2Result, multiply);

        BigInteger result = multiply.apply(results[0], multiply.apply(results[1], results[2]));
//        long end = System.currentTimeMillis();
//        System.out.println(n + " : " + result.bitLength() + " after " + (end - start) + "ms");
        return result;
    }

    private static long approximate(int n, FactorialApproximator[] approximators, Integer p) {
        if (p <= n / 2) {
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

    public static class PrimeFactorizationFactorialTask extends RecursiveTask<BigInteger> {
        private FactorialApproximator[] approximators;

        private final int n;
        private final int startPrime;
        private final int endPrime;

        private BigInteger initialValue = ONE;
        private long remainingExponents = -1;

        private final List<Integer> primes;
        private final BinaryOperator<BigInteger> multiply;
        private BigInteger prime;

        private volatile AtomicReferenceArray<BigInteger> powers;
        private volatile long[] exponents;

        public PrimeFactorizationFactorialTask(
                int n, List<Integer> primes, FactorialApproximator[] approximators, BinaryOperator<BigInteger> multiply) {
            this.n = n;
            this.primes = primes;
            this.approximators = approximators;
            this.initialValue = ONE.shiftLeft((int) approximate(n, approximators, 2));
            this.startPrime = 0;
            this.endPrime = primes.size();
            this.multiply = multiply;
        }

        public PrimeFactorizationFactorialTask(
                int n,
                int startPrime,
                int endPrime,
                List<Integer> primes,
                FactorialApproximator[] approximators,
                BinaryOperator<BigInteger> multiply) {
            this.n = n;
            this.startPrime = startPrime;
            this.endPrime = endPrime;
            this.primes = primes;
            this.approximators = approximators;
            this.multiply = multiply;
        }

        public PrimeFactorizationFactorialTask(
                int n,
                int startPrime,
                List<Integer> primes,
                BigInteger prime,
                FactorialApproximator[] approximators,
                long remainingExponents,
                BinaryOperator<BigInteger> multiply,
                AtomicReferenceArray<BigInteger> powers,
                long[] exponents) {
            this.n = n;
            this.startPrime = startPrime;
            this.endPrime = startPrime + 1;
            this.remainingExponents = remainingExponents;
            this.primes = primes;
            this.prime = prime;
            this.approximators = approximators;
            this.multiply = multiply;
            this.powers = powers;
            this.exponents = exponents;
        }

        @Override
        protected BigInteger compute() {
            int length = endPrime - startPrime;
            if (length == 1) {
                if (remainingExponents == -1) {
                    int primeAsInt = primes.get(startPrime);
                    prime = BigInteger.valueOf(primeAsInt);
                    remainingExponents = approximate(n, approximators, primeAsInt);
                }
                if (remainingExponents == 1) {
//                    return multiply.apply(result, result);
                    return prime;
                } else if (remainingExponents % 3 == 0) {
                    return calculateForRemaining( 0);
                } else if (remainingExponents % 3 == 1) {
                    return calculateForRemaining( 1);
                } else if (5 <= remainingExponents && remainingExponents % 3 == 2) {
                    return calculateForRemaining( 2);
                } else {
                    PrimeFactorizationFactorialTask leftTask = new PrimeFactorizationFactorialTask(
                            n, startPrime, primes, prime, approximators, remainingExponents / 2, multiply, powers, exponents);
                    remainingExponents -= remainingExponents / 2;
                    leftTask.fork();
                    PrimeFactorizationFactorialTask rightTask = new PrimeFactorizationFactorialTask(
                            n, startPrime, primes, prime, approximators, remainingExponents, multiply, powers, exponents);
                    BigInteger result = multiply.apply(rightTask.compute(), leftTask.join());
//                        System.out.println(this + " : " + result);
                    return result;
                }
            } else {
                int halfLength = length / 2;
                PrimeFactorizationFactorialTask leftTask = new PrimeFactorizationFactorialTask(
                        n, startPrime, startPrime + halfLength, primes, approximators, multiply);
                leftTask.fork();
                PrimeFactorizationFactorialTask rightTask = new PrimeFactorizationFactorialTask(
                        n, startPrime + halfLength, endPrime, primes, approximators, multiply);
                BigInteger result = multiply.apply(rightTask.compute(), leftTask.join());
//                System.out.println(this + " : " + result);
                return result;
            }
        }

        private BigInteger calculateForRemaining(int remainder) {
            PrimeFactorizationFactorialTask leftTask = new PrimeFactorizationFactorialTask(
                    n, startPrime, primes, prime, approximators, (remainingExponents - remainder) / 3, multiply, powers, exponents);
            remainingExponents -= 2 * (remainingExponents - remainder) / 3;
            leftTask.fork();
            if (2 <= remainingExponents) {
                PrimeFactorizationFactorialTask rightTask = new PrimeFactorizationFactorialTask(
                        n, startPrime, primes, prime, approximators, remainingExponents / 2, multiply, powers, exponents);
                BigInteger rightResult = rightTask.compute();
                BigInteger leftResult = leftTask.join();
                BigInteger result = multiply.apply(rightResult, leftResult);
                result = multiply.apply(result, result);
                if(remainingExponents % 2 == 1) {
                    result = multiply.apply(result, prime);
                }
//                        System.out.println(this + " : " + result);
                return result;
            } else {
                PrimeFactorizationFactorialTask rightTask = new PrimeFactorizationFactorialTask(
                        n, startPrime, primes, prime, approximators, remainingExponents, multiply, powers, exponents);
                BigInteger rightResult = rightTask.compute();
                BigInteger leftResult = leftTask.join();
                BigInteger result = multiply.apply(leftResult, leftResult);
                result = multiply.apply(rightResult, result);
//                        System.out.println(this + " : " + result);
                return result;
            }
        }

//        @Override
//        protected BigInteger compute() {
//            int length = endPrime - startPrime;
//            if (length == 1) {
//                if (startExponent == -1) {
//                    startExponent = 0;
//                    endExponent = approximate(n, approximators, primes.get(startPrime));
//                }
//                long exponentLength = endExponent - startExponent;
//                if (exponentLength == 1) {
////                    System.out.println(this +  " : " + primes.get(startPrime));
//                    return BigInteger.valueOf(primes.get(startPrime));
//                } else  {
//                    long halfLength = exponentLength / 2;
//                    PrimeFactorizationFactorialTask leftTask = new PrimeFactorizationFactorialTask(
//                            n, startPrime, startExponent, startExponent + halfLength, primes, approximators, multiply);
//                    leftTask.fork();
//                    PrimeFactorizationFactorialTask rightTask = new PrimeFactorizationFactorialTask(
//                            n, startPrime, startExponent + halfLength, endExponent, primes, approximators, multiply);
//                    BigInteger result = multiply.apply(rightTask.compute(), leftTask.join());
////                    System.out.println(this +  " : " + result);
//                    return result;
//                }
//            } else {
//                int halfLength = length / 2;
//                PrimeFactorizationFactorialTask leftTask = new PrimeFactorizationFactorialTask(
//                        n, startPrime, startPrime + halfLength, primes, approximators, multiply);
//                leftTask.fork();
//                PrimeFactorizationFactorialTask rightTask = new PrimeFactorizationFactorialTask(
//                        n, startPrime + halfLength, endPrime, primes, approximators, multiply);
//                BigInteger result = multiply.apply(rightTask.compute(), leftTask.join());
////                System.out.println(this +  " : " + result);
//                return result;
//            }
//        }

        @Override
        public String toString() {
            return "PrimeFactorizationFactorialTask{" +
                    "n=" + n +
                    ", startPrime=" + startPrime +
                    ", endPrime=" + endPrime +
                    ", initialValue=" + initialValue +
                    ", remainingExponents=" + remainingExponents +
                    ", primes=" + primes +
                    '}';
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_25_PrimeFactorization.class.getSimpleName())
//                .forks(1)
//                .warmupIterations(3)
//                .measurementIterations(3)
                .build();
        new Runner(opt).run();
    }
}
