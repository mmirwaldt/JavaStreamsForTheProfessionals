package net.mirwaldt.streams.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("NewClassNamingConvention")
public class SumUtilTest {
    @Test
    void test_checksums_with_only_1_digit() {
        assertEquals(0, SumUtil.sumOfDigits(0));
        assertEquals(1, SumUtil.sumOfDigits(1));
        assertEquals(9, SumUtil.sumOfDigits(9));
    }

    @Test
    void test_checksums_with_only_2_digits() {
        assertEquals(1, SumUtil.sumOfDigits(10));
        assertEquals(2, SumUtil.sumOfDigits(11));
        assertEquals(3, SumUtil.sumOfDigits(12));
        assertEquals(10, SumUtil.sumOfDigits(19));
        assertEquals(2, SumUtil.sumOfDigits(20));
        assertEquals(3, SumUtil.sumOfDigits(21));
        assertEquals(11, SumUtil.sumOfDigits(29));
        assertEquals(10, SumUtil.sumOfDigits(91));
        assertEquals(11, SumUtil.sumOfDigits(92));
        assertEquals(17, SumUtil.sumOfDigits(98));
        assertEquals(18, SumUtil.sumOfDigits(99));
    }

    @Test
    void test_checksums_with_only_3_digits() {
        assertEquals(1, SumUtil.sumOfDigits(100));
        assertEquals(2, SumUtil.sumOfDigits(101));
        assertEquals(2, SumUtil.sumOfDigits(110));
        assertEquals(3, SumUtil.sumOfDigits(111));
        assertEquals(3, SumUtil.sumOfDigits(120));
        assertEquals(3, SumUtil.sumOfDigits(102));
        assertEquals(4, SumUtil.sumOfDigits(112));
        assertEquals(4, SumUtil.sumOfDigits(121));
        assertEquals(5, SumUtil.sumOfDigits(122));
        assertEquals(5, SumUtil.sumOfDigits(221));
        assertEquals(5, SumUtil.sumOfDigits(212));
        assertEquals(6, SumUtil.sumOfDigits(222));
        assertEquals(12, SumUtil.sumOfDigits(129));
        assertEquals(12, SumUtil.sumOfDigits(192));
        assertEquals(12, SumUtil.sumOfDigits(912));
        assertEquals(2, SumUtil.sumOfDigits(200));
        assertEquals(3, SumUtil.sumOfDigits(210));
        assertEquals(3, SumUtil.sumOfDigits(201));
        assertEquals(26, SumUtil.sumOfDigits(899));
        assertEquals(26, SumUtil.sumOfDigits(989));
        assertEquals(26, SumUtil.sumOfDigits(998));
        assertEquals(27, SumUtil.sumOfDigits(999));
    }
}
