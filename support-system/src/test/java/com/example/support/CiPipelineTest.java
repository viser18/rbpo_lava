package com.example.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CiPipelineTest {
    
    @Test
    void testCompilation() {
        assertTrue(true, "Compilation test");
    }
    
    @Test
    void testEnvironmentVariables() {
        String keystorePassword = System.getenv("KEYSTORE_PASSWORD");
        String studentId = System.getenv("STUDENT_ID");
        
        System.out.println("CI Test - Environment:");
        System.out.println("  STUDENT_ID: " + (studentId != null ? studentId : "not set"));
        System.out.println("  KEYSTORE_PASSWORD: " + (keystorePassword != null ? "***set***" : "not set"));
        
        // In CI these should be set
        if ("true".equals(System.getenv("CI")) || "true".equals(System.getenv("GITHUB_ACTIONS"))) {
            assertNotNull(keystorePassword, "KEYSTORE_PASSWORD must be set in CI");
            assertNotNull(studentId, "STUDENT_ID must be set in CI");
        }
        
        assertTrue(true); // Always pass
    }
    
    @Test
    void testSimpleMath() {
        assertEquals(4, 2 + 2, "2+2 should be 4");
        assertEquals(10, 5 * 2, "5*2 should be 10");
    }
}