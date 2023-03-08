package net.mirwaldt.streams.experimental;

public class NGroupsFactorialApproximator implements FactorialApproximator {
    private final int n;

    private final int LAST;
    private final int NEXT_TO_LAST;

    public NGroupsFactorialApproximator(int n) {
        this.n = n;
        this.LAST = n - 1;
        this.NEXT_TO_LAST = LAST - 1;
    }

    @Override
    public long approximate(long factorial, long prime) {
        long[] powers = powers(prime);
        final long groupSize = powers[LAST];
        final long multiplesOfPrimes = factorial / prime;
        final long numberOfGroups = powers[NEXT_TO_LAST];
        final long multiplesOfPrimesPerGroup = multiplesOfPrimes / numberOfGroups;
        final long factorialRemainder = factorial % groupSize;
        long[] additionals = additionals(powers, factorialRemainder);
        long[] factors = factors(prime);
        long[] totals = totals(multiplesOfPrimesPerGroup, additionals, factors);
        return sum(totals);
    }

    private long sum(long[] totals) {
        long total = 0;
        for (int i = 0; i <= LAST; i++) {
            total += (i+1) * totals[i];
        }
        return total;
    }

    private long[] totals(long multiplesOfPrimesPerGroup, long[] additionals, long[] factors) {
        long[] totals = new long[n];
        for (int i = 0; i <= LAST; i++) {
            totals[i] = factors[i] * multiplesOfPrimesPerGroup + additionals[i];
        }
        return totals;
    }

    private long[] factors(long prime) {
        long[] factors = new long[n];
        factors[LAST] = 1;
        factors[NEXT_TO_LAST] = prime - 1;
        for (int i = NEXT_TO_LAST - 1; 0 <= i; i--) {
            for (int j = NEXT_TO_LAST; i < j; j--) {
                factors[i] += factors[j];
            }
            factors[i]++;
            factors[i] *= (prime - 1);
        }
        return factors;
    }

    private long[] additionals(long[] powers, long factorialRemainder) {
        long[] additionals = new long[n];
        for (int i = NEXT_TO_LAST; 0 <= i; i--) {
            additionals[i] = factorialRemainder / powers[i];
            for (int j = i + 1; j <= NEXT_TO_LAST; j++) {
                additionals[i] -= additionals[j];
            }
        }
        return additionals;
    }

    private long[] powers(long prime) {
        long[] powers = new long[n];
        powers[0] = prime;
        for (int i = 1; i <= LAST; i++) {
            powers[i] = powers[i - 1] * prime;
        }
        return powers;
    }
}
