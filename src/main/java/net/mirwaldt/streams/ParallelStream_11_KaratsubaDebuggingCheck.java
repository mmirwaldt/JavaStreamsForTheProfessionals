package net.mirwaldt.streams;

import java.math.BigInteger;

public class ParallelStream_11_KaratsubaDebuggingCheck {
    public static void main(String[] args) {
        BigInteger bigInt1 = BigInteger.ONE;
        bigInt1 = bigInt1.shiftLeft(2528);
        BigInteger bigInt2 = BigInteger.ONE;
        bigInt2 = bigInt2.shiftLeft(2528); // ~10^843
        System.out.println(bigInt1.multiply(bigInt2).bitLength());
    }
}
