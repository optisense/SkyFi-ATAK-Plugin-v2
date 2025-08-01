
package com.atakmap.android.test;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Basic smoke tests to verify test environment is working correctly
 */
public class ExampleTest {
    
    @Test
    public void testEnvironmentSetup() {
        // Verify basic test environment
        assertTrue("Test environment should be functional", true);
        assertNotNull("Should be able to create objects", new Object());
    }
    
    @Test
    public void testBasicArithmetic() {
        assertEquals("Basic math should work", 4, 2 + 2);
        assertEquals("Multiplication should work", 10, 5 * 2);
        assertEquals("Division should work", 2, 10 / 5);
    }
    
    @Test
    public void testStringOperations() {
        String test = "SkyFi ATAK Plugin";
        assertTrue("Should contain SkyFi", test.contains("SkyFi"));
        assertTrue("Should contain ATAK", test.contains("ATAK"));
        assertEquals("Length should be correct", 17, test.length());
    }
}
