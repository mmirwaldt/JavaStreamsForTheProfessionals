package net.mirwaldt.streams;

import java.math.BigInteger;

import static net.mirwaldt.streams.ParallelStream_09_EfficientMultiplicationFriendly.KARATSUBA_THRESHOLD_IN_BITS;

public class ParallelStream_08_KaratsubaDebuggingCheck {
    // This example enables you to check whether karatsuba is used in debugging more with break points in BigInteger
    public static void main(String[] args) {
        BigInteger bigInt1 = BigInteger.ONE;
        bigInt1 = bigInt1.shiftLeft(KARATSUBA_THRESHOLD_IN_BITS);
        BigInteger bigInt2 = BigInteger.ONE;
        bigInt2 = bigInt2.shiftLeft(KARATSUBA_THRESHOLD_IN_BITS);
        System.out.println(bigInt1.multiply(bigInt2).bitLength());
    }
}
