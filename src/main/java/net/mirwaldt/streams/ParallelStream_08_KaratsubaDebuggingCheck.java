package net.mirwaldt.streams;

import java.math.BigInteger;

public class ParallelStream_08_KaratsubaDebuggingCheck {
    // This example enables you to check whether karatsuba is used in debugging more with break points in BigInteger
    public static void main(String[] args) {
        BigInteger bigInt1 = BigInteger.ONE;
        bigInt1 = bigInt1.shiftLeft(2560);
        BigInteger bigInt2 = BigInteger.ONE;
        bigInt2 = bigInt2.shiftLeft(2560); // ~10^843
        System.out.println(bigInt1.multiply(bigInt2).bitLength());
    }
}
