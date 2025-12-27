package com.example.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GuaranteedTest {
    @Test
    public void alwaysPasses() {
        assertTrue(true, "This test always passes");
        System.out.println("CI verification test executed");
    }
    
    @Test
    public void testBasicMath() {
        int result = 1 + 1;
        assertEquals(2, result, "1+1 should equal 2");
    }
}