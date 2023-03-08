package net.mirwaldt.streams.experimental;

public interface FactorialApproximator {
    /**
     * approximates factorial by approximating the exponent for the passed prime
     * @param factorial the factorial
     * @param prime a prime number like 2, 3 or 5
     * @return the approximated exponent of the prime power for the factorial
     */
    long approximate(long factorial, long prime);
}
