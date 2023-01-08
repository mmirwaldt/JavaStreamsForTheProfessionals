package net.mirwaldt.streams;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.LongStream;

public class ParallelStream_10_PrimeNumbers {
    public static void main(String[] args) {
        SortedSet<Long> primeNumbers = LongStream.range(2, 100)
                .boxed()
                .collect(TreeSet::new,
                        ParallelStream_10_PrimeNumbers::addIfPrime,
                        TreeSet::addAll
                );
        System.out.println(primeNumbers);
    }

    static void addIfPrime(SortedSet<Long> primes, long number) {
        if (primes.stream().noneMatch(prime -> number % prime == 0)) {
            primes.add(number);
        }
    }
}
