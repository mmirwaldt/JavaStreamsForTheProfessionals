//package net.mirwaldt.streams;
//
//import java.math.BigInteger;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ForkJoinPool;
//import java.util.concurrent.RecursiveTask;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.stream.IntStream;
//import java.util.stream.LongStream;
//
//public class ParallelStream_12_KaratsubaInBigInteger {
//    private static AtomicLong leftCounter[] = new AtomicLong[10];
//    private static AtomicLong rightCounter[] = new AtomicLong[10];
//    private static AtomicLong bothCounter[] = new AtomicLong[10];
//
//    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        System.out.println("Karatsubas in result:");
//        System.out.println(LongStream.rangeClosed(2, 100_000)
//                .mapToObj(BigInteger::valueOf)
//                .parallel()
//                .reduce(ParallelStream_12_KaratsubaInBigInteger::multiplyAndWatch)
//                .orElse(BigInteger.ONE)
//                .bitLength() / 2528d);
//
//        System.out.println("-".repeat(120));
//
//        System.out.println("ParallelStream");
//        System.out.println("leftCounter : " + leftCounter.get());
//        System.out.println("rightCounter : " + rightCounter.get());
//        System.out.println("bothCounter : " + bothCounter.get());
//
//        System.out.println("-".repeat(120));
//
//        System.out.println("ForkJoinPool");
//        ForkJoinPool.commonPool()
//                .invoke(new FactorialTask(1, 100_000 + 1, 10000));
//        System.out.println("leftCounter2 : " + leftCounter2.get());
//        System.out.println("rightCounter2 : " + rightCounter2.get());
//        System.out.println("bothCounter2 : " + bothCounter2.get());
//
//        System.out.println("-".repeat(120));
//
////        System.out.println("CompletableFuture");
////        int[] splits = calculateSplits(100_000);
////        calculateWithSplits(100_001, splits, ParallelStream_12_KaratsubaInBigInteger::multiplyAndWatch3);
////        System.out.println("leftCounter3 : " + leftCounter3.get());
////        System.out.println("rightCounter3 : " + rightCounter3.get());
////        System.out.println("bothCounter3 : " + bothCounter3.get());
//    }
//
//    public static class FactorialTask extends RecursiveTask<BigInteger> {
//        private final int start;
//        private final int end;
//        private final int minLength;
//
//        public FactorialTask(int start, int end, int minLength) {
//            this.start = start;
//            this.end = end;
//            this.minLength = minLength;
//        }
//
//        @Override
//        protected BigInteger compute() {
//            int length = end - start;
//            if (length <= minLength) {
//                return IntStream.range(start + 1, end).mapToObj(BigInteger::valueOf)
//                        .reduce(ParallelStream_12_KaratsubaInBigInteger::multiplyAndWatch2).orElse(BigInteger.ONE);
//            } else {
//                int halfLength = length / 2;
//                FactorialTask leftTask = new FactorialTask(start, start + halfLength, minLength);
//                leftTask.fork();
//                FactorialTask rightTask = new FactorialTask(start + halfLength, end, minLength);
//                return multiplyAndWatch2(rightTask.compute(), leftTask.join());
//            }
//        }
//    }
//
//    private static BigInteger multiplyAndWatch(BigInteger left, BigInteger right, int i) {
//        if(2528d <= left.bitLength() && 2528d <= right.bitLength()) {
//            bothCounter.incrementAndGet();
//        } else if(2528d <= left.bitLength()) {
//            leftCounter.incrementAndGet();
//        } else if(2528d <= right.bitLength()) {
//            rightCounter.incrementAndGet();
//        }
//        return left.parallelMultiply(right);
//    }
//
//    private static BigInteger multiplyAndWatch2(BigInteger left, BigInteger right) {
//        if(2528d <= left.bitLength() && 2528d <= right.bitLength()) {
//            bothCounter2.incrementAndGet();
//        } else if(2528d <= left.bitLength()) {
//            leftCounter2.incrementAndGet();
//        } else if(2528d <= right.bitLength()) {
//            rightCounter2.incrementAndGet();
//        }
//        return left.parallelMultiply(right);
//    }
//
//    private static BigInteger multiplyAndWatch3(BigInteger left, BigInteger right) {
//        if(2528d <= left.bitLength() && 2528d <= right.bitLength()) {
//            bothCounter3.incrementAndGet();
//        } else if(2528d <= left.bitLength()) {
//            leftCounter3.incrementAndGet();
//        } else if(2528d <= right.bitLength()) {
//            rightCounter3.incrementAndGet();
//        }
//        return left.parallelMultiply(right);
//    }
//}
