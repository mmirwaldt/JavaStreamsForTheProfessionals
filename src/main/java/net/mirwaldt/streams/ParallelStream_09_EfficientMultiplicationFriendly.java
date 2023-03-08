package net.mirwaldt.streams;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.math.BigInteger.ONE;

public class ParallelStream_09_EfficientMultiplicationFriendly {

    public static final int KARATSUBA_THRESHOLD_IN_BITS = 2560;
    public static final int TOM_COOK_THRESHOLD_IN_BITS = 7680;

    /*
        This is my first sequential solution for multiplying all factors of factorial in a karatsuba-friendly way.
     */
    BigInteger factorial(int n) {
        BigInteger result = ONE;
        List<BigInteger> results = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
            if (KARATSUBA_THRESHOLD_IN_BITS < result.bitLength()) {
                results.add(result);
                result = ONE;
            } else if (i == n) {
                results.add(result);
            }
        }
        return results.stream().reduce(BigInteger::multiply).orElse(ONE);
    }

    /*
        This is my second sequential solution for multiplying all factors of factorial in a karatsuba-friendly way.
        Splitting the interval into half enables parallelization.
    */
    BigInteger factorial2(int n) {
        int half = n / 2;
        BigInteger leftResult = ONE;
        List<BigInteger> leftResults = new ArrayList<>();
        for (int i = 2; i < half; i++) {
            leftResult = leftResult.multiply(BigInteger.valueOf(i));
            if (KARATSUBA_THRESHOLD_IN_BITS < leftResult.bitLength()) {
                leftResults.add(leftResult);
                leftResult = ONE;
            } else if (i == half - 1) {
                leftResults.add(leftResult);
            }
        }
        BigInteger rightResult = ONE;
        List<BigInteger> rightResults = new ArrayList<>();
        for (int i = half; i < n; i++) {
            rightResult = rightResult.multiply(BigInteger.valueOf(i));
            if (KARATSUBA_THRESHOLD_IN_BITS < rightResult.bitLength()) {
                rightResults.add(rightResult);
                rightResult = ONE;
            } else if (i == n - 1) {
                rightResults.add(rightResult);
            }
        }
        return Stream.of(leftResults.stream(), rightResults.stream())
                .flatMap(s -> s)
                .reduce(BigInteger::multiply)
                .orElse(ONE);
    }

    /*
        First solution with a parallel stream to calculate factorial in a karatsuba-friendly way.
     */
    public static BigInteger karatsubaFactorialParallelStream(int n) {
        List<BigInteger> results =
                IntStream.rangeClosed(2, n)
                        .mapToObj(BigInteger::valueOf)
                        .parallel()
                        .collect(() -> new ArrayList<BigInteger>(),
                                (list, i) -> accumulate(list, i),
                                (left, right) -> combine(left, right)
                        );
        return results.get(0);
    }

    static void combine(List<BigInteger> left, List<BigInteger> right) {
        BigInteger result =
                Stream.of(left.stream(), right.stream())
                        .flatMap(s -> s)
                        .parallel()
                        .reduce(BigInteger::parallelMultiply)
                        .orElse(ONE);
        left.clear();
        left.add(result);
    }

    static void accumulate(List<BigInteger> list, BigInteger i) {
        if (list.isEmpty() || KARATSUBA_THRESHOLD_IN_BITS < list.get(list.size() - 1).bitLength()) {
            list.add(i);
        } else {
            list.set(list.size() - 1, list.get(list.size() - 1).multiply(i));
        }
    }

    /*
        Second solution with a parallel stream to calculate factorial in a karatsuba-friendly way.
        The list was replaced by an array.
     */
    public static BigInteger karatsubaFactorialParallelStream2(int n, BinaryOperator<BigInteger> multiply) {
        BigInteger[] results = IntStream.rangeClosed(2, n)
                .mapToObj(BigInteger::valueOf)
                .parallel()
                .collect(() -> new BigInteger[]{ONE, ONE},
                        (array, i) -> accumulate2(array, i, multiply),
                        (left, right) -> combine2(left, right, multiply)
                );
        return results[0].multiply(results[1]);
    }

    static void accumulate2(BigInteger[] result, BigInteger i, BinaryOperator<BigInteger> multiply) {
        result[1] = result[1].multiply(i);
        if (KARATSUBA_THRESHOLD_IN_BITS <= result[1].bitLength()) {
            result[0] = multiply.apply(result[0], result[1]);
            result[1] = ONE;
        }
    }

    static void combine2(BigInteger[] left, BigInteger[] right, BinaryOperator<BigInteger> multiply) {
        left[0] = Stream.of(left[0], left[1], right[0], right[1])
                .parallel()
                .reduce(multiply)
                .orElse(ONE);
        left[1] = ONE;
    }

    /*
        This is the application of the karatsuba-friendly solution from parallel stream to a ForkJoinPool solution.
        First results show me the optimization has no effect.
        The CPU appears to be fully saturated so that no further optimization has got any effect.
     */
    public static class KaratsubaFactorialTask extends RecursiveTask<BigInteger> {
        private final int start;
        private final int end;
        private final int minLength;
        private final BinaryOperator<BigInteger> multiply;

        public KaratsubaFactorialTask(int start, int end, int minLength, BinaryOperator<BigInteger> multiply) {
            this.start = start;
            this.end = end;
            this.minLength = minLength;
            this.multiply = multiply;
        }

        @Override
        protected BigInteger compute() {
            int length = end - start;
            if (length <= minLength) {
                BigInteger[] result = new BigInteger[]{ONE, ONE};
                for (int i = start; i < end; i++) {
                    result[1] = result[1].multiply(BigInteger.valueOf(i));
                    if (KARATSUBA_THRESHOLD_IN_BITS <= result[1].bitLength()) {
                        result[0] = multiply.apply(result[0], result[1]);
                        result[1] = ONE;
                    }
                }
                return result[0].multiply(result[1]);
            } else {
                int halfLength = length / 2;
                KaratsubaFactorialTask leftTask =
                        new KaratsubaFactorialTask(start, start + halfLength, minLength, multiply);
                leftTask.fork();
                KaratsubaFactorialTask rightTask =
                        new KaratsubaFactorialTask(start + halfLength, end, minLength, multiply);
                return multiply.apply(rightTask.compute(), leftTask.join());
            }
        }
    }

    public static BigInteger karatsubaFactorialForkJoinPool(int n, BinaryOperator<BigInteger> multiply) {
        return ForkJoinPool.commonPool()
                .invoke(new KaratsubaFactorialTask(1, n + 1, 1000, multiply));
    }

    /*
        Let's improve it to make the solution Tom-Cook-multiplication friendly
     */
    public static BigInteger tomCookKaratsubaFactorialParallelStream(int n, BinaryOperator<BigInteger> multiply) {
        BigInteger[] results = IntStream.rangeClosed(2, n)
                .mapToObj(BigInteger::valueOf)
                .parallel()
                .collect(() -> new BigInteger[]{ONE, ONE, ONE},
                        (array, i) -> accumulate(array, i, multiply),
                        (left, right) -> combine(left, right, multiply)
                );
        return multiply.apply(results[0], multiply.apply(results[1], results[2]));
    }

    public static void accumulate(BigInteger[] result, BigInteger i, BinaryOperator<BigInteger> multiply) {
        result[2] = multiply.apply(result[2], i);
        if (KARATSUBA_THRESHOLD_IN_BITS <= result[2].bitLength()) {
            result[1] = multiply.apply(result[1], result[2]);
            result[2] = ONE;
        }
        if (TOM_COOK_THRESHOLD_IN_BITS <= result[1].bitLength()) {
            result[0] = multiply.apply(result[0], result[1]);
            result[1] = ONE;
        }
    }

    public static void combine(BigInteger[] left, BigInteger[] right, BinaryOperator<BigInteger> multiply) {
        left[0] = Stream.of(Stream.of(left), Stream.of(right))
                .flatMap(s -> s)
                .parallel()
                .reduce(multiply)
                .orElse(ONE);
        left[1] = ONE;
        left[2] = ONE;
    }

    public static void combine4(BigInteger[] left, BigInteger[] right, BinaryOperator<BigInteger> multiply) {
        left[0] = Stream.of(left[0], left[1], left[2], right[0], right[1], right[2])
                .parallel()
                .reduce(multiply)
                .orElse(ONE);
        left[1] = ONE;
        left[2] = ONE;
    }

    public static void combine3(BigInteger[] leftResult, BigInteger[] rightResult, BinaryOperator<BigInteger> multiply) {
        leftResult[2] = multiply.apply(leftResult[2], rightResult[2]);
        if (KARATSUBA_THRESHOLD_IN_BITS <= leftResult[2].bitLength()) {
            leftResult[1] = multiply.apply(leftResult[1], leftResult[2]);
            leftResult[2] = ONE;
        }
        leftResult[1] = multiply.apply(leftResult[1], rightResult[1]);
        if (TOM_COOK_THRESHOLD_IN_BITS <= leftResult[1].bitLength()) {
            leftResult[0] = multiply.apply(leftResult[0], leftResult[1]);
            leftResult[1] = ONE;
        }
        leftResult[0] = multiply.apply(leftResult[0], rightResult[0]);
    }

    /*
        This is the application of the karatsuba-friendly solution from parallel stream to a ForkJoinPool solution.
        First results show me the optimization has no effect.
        The CPU appears to be fully saturated so that no further optimization has got any effect.
     */
    public static class TomCookKaratsubaFactorialTask extends RecursiveTask<BigInteger> {
        private final int start;
        private final int end;
        private final int minLength;
        private final BinaryOperator<BigInteger> multiply;

        public TomCookKaratsubaFactorialTask(int start, int end, int minLength, BinaryOperator<BigInteger> multiply) {
            this.start = start;
            this.end = end;
            this.minLength = minLength;
            this.multiply = multiply;
        }

        @Override
        protected BigInteger compute() {
            int length = end - start;
            if (length <= minLength) {
                BigInteger[] result = new BigInteger[]{ONE, ONE, ONE};
                for (int i = start; i < end; i++) {
                    accumulate(result, BigInteger.valueOf(i), multiply);
                }
                return result[0].multiply(result[1]).multiply(result[2]);
            } else {
                int halfLength = length / 2;
                TomCookKaratsubaFactorialTask leftTask =
                        new TomCookKaratsubaFactorialTask(start, start + halfLength, minLength, multiply);
                leftTask.fork();
                TomCookKaratsubaFactorialTask rightTask =
                        new TomCookKaratsubaFactorialTask(start + halfLength, end, minLength, multiply);
                return multiply.apply(rightTask.compute(), leftTask.join());
            }
        }
    }

    public static BigInteger tomCookKaratsubaFactorialForkJoinPool(int n, BinaryOperator<BigInteger> multiply) {
        // leads to 512 Karatsuba multiplications and 188 Tom-Cook multiplications
        // this is 572/591=97% Karatsuba multiplications and 188/196=96% Tom-Cook multiplications
        return tomCookKaratsubaFactorialForkJoinPool(n, multiply, 7000);
    }

    public static BigInteger tomCookKaratsubaFactorialForkJoinPool(int n, BinaryOperator<BigInteger> multiply, int minLength) {
        return ForkJoinPool.commonPool()
                .invoke(new TomCookKaratsubaFactorialTask(1, n + 1, minLength, multiply));
    }
}
