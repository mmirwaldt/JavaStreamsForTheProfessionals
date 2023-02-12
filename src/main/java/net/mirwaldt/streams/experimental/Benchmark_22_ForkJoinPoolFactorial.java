package net.mirwaldt.streams.experimental;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

@SuppressWarnings("DuplicatedCode")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Benchmark_22_ForkJoinPoolFactorial {
    /*
Benchmark                                                                                 Mode  Cnt   Score   Error  Units
Benchmark_22_ForkJoinPoolFactorial.factorialParallelInForkJoinPoolParallelMultiply_1      avgt   25  16.661 ± 0.045  ms/op
Benchmark_22_ForkJoinPoolFactorial.factorialParallelInForkJoinPoolParallelMultiply_100    avgt   25  16.679 ± 0.028  ms/op
Benchmark_22_ForkJoinPoolFactorial.factorialParallelInForkJoinPoolSequentialMultiply_1    avgt   25  29.657 ± 0.388  ms/op
Benchmark_22_ForkJoinPoolFactorial.factorialParallelInForkJoinPoolSequentialMultiply_100  avgt   25  29.795 ± 0.342  ms/op
     */
    public int N = 100_000;

    @Benchmark
    public BigInteger factorialParallelInForkJoinPoolSequentialMultiply_1() {
        return factorialParallelInForkJoinPoolMinLength(N, BigInteger::multiply, 1);
    }

    @Benchmark
    public BigInteger factorialParallelInForkJoinPoolParallelMultiply_1() {
        return factorialParallelInForkJoinPoolMinLength(N, BigInteger::parallelMultiply, 1);
    }

    @Benchmark
    public BigInteger factorialParallelInForkJoinPoolSequentialMultiply_100() {
        return factorialParallelInForkJoinPoolMinLength(N, BigInteger::multiply, 100);
    }

    @Benchmark
    public BigInteger factorialParallelInForkJoinPoolParallelMultiply_100() {
        return factorialParallelInForkJoinPoolMinLength(N, BigInteger::parallelMultiply, 100);
    }

    public static BigInteger factorialParallelInForkJoinPoolMinLength(int n, BinaryOperator<BigInteger> multiply, int minLength) {
        return ForkJoinPool.commonPool().invoke(new MinLengthFactorialTask(1, n + 1, minLength, multiply));
    }

    public static class MinLengthFactorialTask extends RecursiveTask<BigInteger> {
        private final int start;
        private final int end;
        private final int minLength;
        private final BinaryOperator<BigInteger> multiply;

        public MinLengthFactorialTask(int start, int end, int minLength, BinaryOperator<BigInteger> multiply) {
            this.start = start;
            this.end = end;
            this.minLength = minLength;
            this.multiply = multiply;
        }

        @Override
        protected BigInteger compute() {
            int length = end - start;
            if (length <= minLength) {
                BigInteger result = BigInteger.valueOf(start);
                for (int i = start + 1; i < end; i++) { result = result.multiply(BigInteger.valueOf(i)); }
                return result;
            } else {
                int halfLength = length / 2;
                MinLengthFactorialTask leftTask =
                        new MinLengthFactorialTask(start, start + halfLength, minLength, multiply);
                leftTask.fork();
                MinLengthFactorialTask rightTask =
                        new MinLengthFactorialTask(start + halfLength, end, minLength, multiply);
                return multiply.apply(rightTask.compute(), leftTask.join());
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark_22_ForkJoinPoolFactorial.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
