package net.mirwaldt.streams.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollatzUtilTest {
    @Test
    void test_collatz() {
        assertEquals(0, CollatzUtil.collatzSteps(1));
        assertEquals(1, CollatzUtil.collatzSteps(2));
        assertEquals(2, CollatzUtil.collatzSteps(4));

        // 5, 3*5+1=16, 16/2=8, 8/2=4, 4/2=2, 2/2=1
        assertEquals(5, CollatzUtil.collatzSteps(5));

        // 6, 6/2=3, 3*3+1=10, 10/2=5, CollatzUtil.follow(5)
        assertEquals(8, CollatzUtil.collatzSteps(6));

        // 7*3+1=22, 22/2=11, 3*11+1=34, 34/2=17, 17*3+1=52, 52/2=26, 26/2=13,
        // 13*3+1=40. 40/2=20, 20/2=10, 10/2=5, CollatzUtil.follow(5)
        assertEquals(16, CollatzUtil.collatzSteps(7));
    }
}
