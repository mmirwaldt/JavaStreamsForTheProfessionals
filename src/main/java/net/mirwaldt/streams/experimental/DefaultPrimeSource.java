package net.mirwaldt.streams.experimental;

public class DefaultPrimeSource implements PrimeSource {
    @Override
    public boolean isPrime(long n) {
        if (n < 2) {
            return false;
        } else if (n == 2) {
            return true;
        } else if (n % 2 == 0) {
            return false;
        } else {
            for (long i = 3; i * i <= n; i += 2) {
                if(n % i == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}
