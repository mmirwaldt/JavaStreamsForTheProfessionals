package net.mirwaldt.streams;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("NewClassNamingConvention")
public class ParallelStream_01_Range_Test {
    @Test
    void test_checksums_with_only_1_digit() {
        assertEquals(0, ParallelStream_01_Range.checkSum(0));
        assertEquals(1, ParallelStream_01_Range.checkSum(1));
        assertEquals(9, ParallelStream_01_Range.checkSum(9));
    }

    @Test
    void test_checksums_with_only_2_digits() {
        assertEquals(1, ParallelStream_01_Range.checkSum(10));
        assertEquals(2, ParallelStream_01_Range.checkSum(11));
        assertEquals(3, ParallelStream_01_Range.checkSum(12));
        assertEquals(10, ParallelStream_01_Range.checkSum(19));
        assertEquals(2, ParallelStream_01_Range.checkSum(20));
        assertEquals(3, ParallelStream_01_Range.checkSum(21));
        assertEquals(11, ParallelStream_01_Range.checkSum(29));
        assertEquals(10, ParallelStream_01_Range.checkSum(91));
        assertEquals(11, ParallelStream_01_Range.checkSum(92));
        assertEquals(17, ParallelStream_01_Range.checkSum(98));
        assertEquals(18, ParallelStream_01_Range.checkSum(99));
    }

    @Test
    void test_checksums_with_only_3_digits() {
        assertEquals(1, ParallelStream_01_Range.checkSum(100));
        assertEquals(2, ParallelStream_01_Range.checkSum(101));
        assertEquals(2, ParallelStream_01_Range.checkSum(110));
        assertEquals(3, ParallelStream_01_Range.checkSum(111));
        assertEquals(3, ParallelStream_01_Range.checkSum(120));
        assertEquals(3, ParallelStream_01_Range.checkSum(102));
        assertEquals(4, ParallelStream_01_Range.checkSum(112));
        assertEquals(4, ParallelStream_01_Range.checkSum(121));
        assertEquals(5, ParallelStream_01_Range.checkSum(122));
        assertEquals(5, ParallelStream_01_Range.checkSum(221));
        assertEquals(5, ParallelStream_01_Range.checkSum(212));
        assertEquals(6, ParallelStream_01_Range.checkSum(222));
        assertEquals(12, ParallelStream_01_Range.checkSum(129));
        assertEquals(12, ParallelStream_01_Range.checkSum(192));
        assertEquals(12, ParallelStream_01_Range.checkSum(912));
        assertEquals(2, ParallelStream_01_Range.checkSum(200));
        assertEquals(3, ParallelStream_01_Range.checkSum(210));
        assertEquals(3, ParallelStream_01_Range.checkSum(201));
        assertEquals(26, ParallelStream_01_Range.checkSum(899));
        assertEquals(26, ParallelStream_01_Range.checkSum(989));
        assertEquals(26, ParallelStream_01_Range.checkSum(998));
        assertEquals(27, ParallelStream_01_Range.checkSum(999));
    }
}
